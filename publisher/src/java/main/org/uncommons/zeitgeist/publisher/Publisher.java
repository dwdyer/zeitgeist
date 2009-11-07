package org.uncommons.zeitgeist.publisher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.uncommons.zeitgeist.Theme;
import org.uncommons.zeitgeist.Zeitgeist;

/**
 * Simple HTML publisher for a set of themes.
 * @author Daniel Dyer
 */
public class Publisher
{
    private static final String ENCODING = "UTF-8";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE d MMMM yyyy / HH:mm z");


    public static void publish(List<Theme> themes,
                               String title) throws IOException
    {
        StringTemplateGroup group = new StringTemplateGroup("group");
        StringTemplate template = group.getInstanceOf("news");
        template.setAttribute("themes", themes);
        template.setAttribute("title", title);
        template.setAttribute("datetime", DATE_FORMAT.format(new Date()));
        Writer writer = new OutputStreamWriter(new FileOutputStream("news.html"), ENCODING);
        try
        {
            writer.write(template.toString());
            writer.flush();
        }
        finally
        {
            writer.close();
        }
        copyClasspathResource(new File("."), "style.css", "style.css");
    }


    /**
     * Copy a single named resource from the classpath to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param resourcePath The path of the resource.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws IOException If the resource cannot be copied.
     */
    private static void copyClasspathResource(File outputDirectory,
                                              String resourcePath,
                                              String targetFileName) throws IOException
    {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourcePath);
        copyStream(outputDirectory, resourceStream, targetFileName);
    }


    /**
     * Helper method to copy the contents of a stream to a file.
     * @param outputDirectory The directory in which the new file is created.
     * @param stream The stream to copy.
     * @param targetFileName The file to write the stream contents to.
     * @throws IOException If the stream cannot be copied.
     */
    private static void copyStream(File outputDirectory,
                                   InputStream stream,
                                   String targetFileName) throws IOException
    {
        File resourceFile = new File(outputDirectory, targetFileName);
        BufferedReader reader = null;
        Writer writer = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(stream, ENCODING));
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resourceFile), ENCODING));

            String line = reader.readLine();
            while (line != null)
            {
                writer.write(line);
                writer.write('\n');
                line = reader.readLine();
            }
            writer.flush();
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
            if (writer != null)
            {
                writer.close();
            }
        }
    }



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
            Date cutoffDate = new Date(System.currentTimeMillis() - 172800000); // 2 days ago.
            Zeitgeist zeitgeist = new Zeitgeist(feeds, cutoffDate);
            List<Theme> themes = zeitgeist.getThemes();
            System.out.println(themes.size() + " themes identified.");
            publish(themes, args[1]);
        }
        finally
        {
            feedListReader.close();
        }
    }
}
