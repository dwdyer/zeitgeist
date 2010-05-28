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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.ArticleFetcher;
import org.uncommons.zeitgeist.Image;
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


    /**
     * @param topics A list of topics identified.
     * @param title Title for generated pages.
     * @param feedCount The number of feeds used.
     * @param articleCount The number of articles analysed.
     * @param minutesToExpiry How many minutes after it is generated should the page(s) be cached for?
     * @throws IOException If something goes wrong.
     */
    public void publish(List<Topic> topics,
                        String title,
                        int feedCount,
                        int articleCount,
                        int minutesToExpiry,
                        File outputDir) throws IOException
    {
        group.registerRenderer(Date.class, new DateRenderer());
        group.registerRenderer(String.class, new XMLStringRenderer());

        cacheImages(topics, outputDir);

        // Publish HTML.
        StringTemplate htmlTemplate = group.getInstanceOf("news");
        publishTemplate(topics, title, feedCount, articleCount, minutesToExpiry, htmlTemplate, new File("index.html"));
        if (group.getRootDir() != null)
        {
            copyFile(outputDir, "zeitgeist.css", "zeitgeist.css");
        }
        else
        {

            copyClasspathResource(outputDir, "zeitgeist.css", "zeitgeist.css");
        }
    }


    private void publishTemplate(List<Topic> topics,
                                 String title,
                                 int feedCount,
                                 int articleCount,
                                 int minutesToExpiry,
                                 StringTemplate template,
                                 File outputFile) throws IOException
    {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendar.add(Calendar.MINUTE, minutesToExpiry);
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


    private void cacheImages(List<Topic> topics, File cacheDir)
    {
        // We only use the first image from each topic, so only download that.
        for (Topic topic : topics)
        {
            List<Image> images = topic.getImages();
            if (!images.isEmpty())
            {
                Image image = images.get(0);
                File cachedFile = new File(cacheDir, image.getCachedFileName());
                if (cachedFile.exists())
                {
                    // If the file exists, touch it to show it is still relevant.
                    cachedFile.setLastModified(System.currentTimeMillis());
                    LOG.debug("Image found in cache: " + image.getImageURL());
                }
                else // Only download images that are not already cached.
                {
                    try
                    {
                        copyStream(cacheDir,
                                   image.getImageURL().openConnection().getInputStream(),
                                   image.getCachedFileName());
                        LOG.debug("Downloaded image: " + image.getImageURL());
                    }
                    catch (IOException ex)
                    {
                        LOG.error("Failed downloading image " + image.getImageURL() + ", " + ex.getMessage());
                    }
                    try
                    {
                        scaleImage(cachedFile, 200);
                    }
                    catch (IOException ex)
                    {
                        LOG.error("Failed resizing image " + image.getImageURL() + ", " + ex.getMessage());
                    }
                }
            }
        }
    }


    /**
     * Resize the specified image file, maintaining its aspect ratio, so that is no wider
     * that the specified width.  If the image is smaller than the specified width, it is
     * left unchanged.
     * @param imageFile The file to (maybe) resize.
     * @param maxWidth The target width.
     * @throws IOException If there is a problem reading or writing the image.
     */
    private void scaleImage(File imageFile, int maxWidth) throws IOException
    {
        BufferedImage image = ImageIO.read(imageFile);
        if (image.getWidth() > maxWidth)
        {
            float ratio = ((float) maxWidth) / image.getWidth();
            int height = Math.round(ratio * image.getHeight());
            BufferedImage scaledImage = new BufferedImage(maxWidth,
                                                          height,
                                                          BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = scaledImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                      RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, maxWidth, height, null);
            ImageIO.write(scaledImage, "jpeg", imageFile);
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
        FileInputStream inputStream = new FileInputStream(new File(group.getRootDir(), filePath));
        try
        {
            copyStream(outputDirectory, inputStream, targetFileName);
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
        BufferedInputStream input = new BufferedInputStream(stream);
        try
        {
            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(resourceFile));
            try
            {
                int i = input.read();
                while (i != -1)
                {
                    output.write(i);
                    i = input.read();
                }
                output.flush();
            }
            finally
            {
                output.close();
            }
        }
        finally
        {
            input.close();
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
            publisher.publish(topics, args[1], feeds.size(), articles.size(), 30, new File("."));
        }
        finally
        {
            feedListReader.close();
        }
    }
}
