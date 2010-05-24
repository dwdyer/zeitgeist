// ============================================================================
//   Copyright 2009-2010 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
package org.uncommons.zeitgeist.publisher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.ArticleFetcher;
import org.uncommons.zeitgeist.Topic;
import org.uncommons.zeitgeist.Zeitgeist;

/**
 * Simple HTML publisher for a set of topics.
 * @author Daniel Dyer
 */
public class Publisher
{
    private static final SimpleLogger LOG = new SimpleLogger(Publisher.class);
    private static final String ENCODING = "UTF-8";

    private static final int CUTOFF_TIME_MS = 129600000; // 36 hours ago.

    private final StringTemplateGroup group;


    /**
     * Create a publisher that loads templates from the classpath.  This is typically
     * used to load the default templates that are bundled in the publisher JAR.
     */
    public Publisher()
    {
        this.group = new StringTemplateGroup("zeitgeist");
    }


    /**
     * Create a publisher that loads templates from the specified directory.
     * @param templateDir The root directory of the StringTemplate templates.
     */
    public Publisher(File templateDir)
    {
        if (!templateDir.isDirectory())
        {
            throw new IllegalArgumentException("Template path must be a directory.");
        }
        this.group = new StringTemplateGroup("zeitgeist", templateDir.getAbsolutePath());
    }


    public void publish(List<Topic> topics,
                        String title,
                        int feedCount,
                        int articleCount,
                        boolean rss) throws IOException
    {
        group.registerRenderer(Date.class, new DateRenderer());
        group.registerRenderer(String.class, new XMLStringRenderer());

        // Publish HTML.
        StringTemplate htmlTemplate = group.getInstanceOf("news");
        publishTemplate(topics, title, feedCount, articleCount, htmlTemplate, new File("index.html"));
        if (group.getRootDir() != null)
        {
            copyFile(new File("."), "style.css", "style.css");
        }
        else
        {
            copyClasspathResource(new File("."), "style.css", "style.css");
        }

        // Publish RSS.
        if (rss)
        {
            StringTemplate feedTemplate = group.getInstanceOf("feed");
            publishTemplate(topics, title, feedCount, articleCount, feedTemplate, new File("rss.xml"));
        }
    }


    private void publishTemplate(List<Topic> topics,
                                 String title,
                                 int feedCount,
                                 int articleCount,
                                 StringTemplate template,
                                 File outputFile) throws IOException
    {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.MINUTE, 15);
        template.setAttribute("topics", topics);
        template.setAttribute("title", title);
        template.setAttribute("dateTime", date);
        template.setAttribute("expires", calendar.getTime());
        template.setAttribute("feedCount", feedCount);
        template.setAttribute("articleCount", articleCount);
        Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), ENCODING);
        try
        {
            writer.write(template.toString());
            writer.flush();
        }
        finally
        {
            writer.close();
        }
    }


    /**
     * Copy a single named resource from the classpath to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param resourcePath The path of the resource.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws IOException If the resource cannot be copied.
     */
    private void copyClasspathResource(File outputDirectory,
                                       String resourcePath,
                                       String targetFileName) throws IOException
    {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourcePath);
        copyStream(outputDirectory, resourceStream, targetFileName);
    }


    /**
     * Copy a single named file to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param filePath The path of the file.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws IOException If the file cannot be copied.
     */
    private void copyFile(File outputDirectory,
                          String filePath,
                          String targetFileName) throws IOException
    {
        FileInputStream inputStream = new FileInputStream(new File(group.getRootDir(), "style.css"));
        try
        {
            copyStream(new File("."), inputStream, "style.css");
        }
        finally
        {
            inputStream.close();
        }
    }


    /**
     * Helper method to copy the contents of a stream to a file.
     * @param outputDirectory The directory in which the new file is created.
     * @param stream The stream to copy.
     * @param targetFileName The file to write the stream contents to.
     * @throws IOException If the stream cannot be copied.
     */
    private void copyStream(File outputDirectory,
                            InputStream stream,
                            String targetFileName) throws IOException
    {
        File resourceFile = new File(outputDirectory, targetFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, ENCODING));
        try
        {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resourceFile), ENCODING));
            try
            {
                String line = reader.readLine();
                while (line != null)
                {
                    writer.write(line);
                    writer.write('\n');
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
    }


    /**
     * Entry point for the publisher application.  Takes two arguments - the path to a file containing a list
     * of feeds, and the title to use for the generated output.
     */
    public static void main(String[] args) throws IOException
    {
        BufferedReader feedListReader = new BufferedReader(new FileReader(args[0]));
        try
        {
            List<URL> feeds = new LinkedList<URL>();
            for (String line = feedListReader.readLine(); line != null; line = feedListReader.readLine())
            {
                String url = line.trim();
                if (url.length() > 0)
                {
                    feeds.add(new URL(url));
                }
            }
            Date cutoffDate = new Date(System.currentTimeMillis() - CUTOFF_TIME_MS);
            List<Article> articles = new ArticleFetcher().getArticles(feeds, cutoffDate);
            List<Topic> topics = new Zeitgeist(articles).getTopics();
            LOG.info(topics.size() + " topics identified.");
            Publisher publisher = args.length > 2 ? new Publisher(new File(args[2])) : new Publisher();
            publisher.publish(topics, args[1], feeds.size(), articles.size(), false);
        }
        finally
        {
            feedListReader.close();
        }
    }
}
