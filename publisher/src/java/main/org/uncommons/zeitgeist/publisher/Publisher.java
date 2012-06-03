// ============================================================================
//   Copyright 2009-2012 Daniel W. Dyer
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.ArticleFetcher;
import org.uncommons.zeitgeist.Image;
import org.uncommons.zeitgeist.Topic;
import org.uncommons.zeitgeist.WeightedItem;
import org.uncommons.zeitgeist.Zeitgeist;

/**
 * Simple HTML publisher for a set of topics.
 * @author Daniel Dyer
 */
public class Publisher
{
    private static final SimpleLogger LOG = new SimpleLogger(Publisher.class);
    private static final String ENCODING = "UTF-8";

    private static final Pattern FAVICON_PATTERN = Pattern.compile("link.+?rel=\"shortcut icon\".+?href=\"(\\S+?)\"",
                                                                   Pattern.CASE_INSENSITIVE);

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
        cacheIcons(topics, outputDir);

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

        if (group.isDefined("snippet"))
        {
            List<Topic> snippetTopics = topics.subList(0, Math.min(5, topics.size())); // Include no more than 5 topics.
            StringTemplate syndicateTemplate = group.getInstanceOf("snippet");
            publishTemplate(snippetTopics,
                            title,
                            feedCount,
                            articleCount,
                            minutesToExpiry,
                            syndicateTemplate,
                            new File("snippet.html"));
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


    /**
     * Download and cache images referenced by the topics.  If the images are larger than
     * necessary, scale them down.
     * @param topics A list of topics.
     * @param cacheDir Where to store the local favicons.
     */
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
                        copyStream(image.getImageURL().openConnection().getInputStream(),
                                   new FileOutputStream(new File(cacheDir, image.getCachedFileName())));
                        LOG.debug("Downloaded image: " + image.getImageURL());
                        scaleImage(cachedFile, 200);
                    }
                    catch (Exception ex)
                    {
                        LOG.error("Failed downloading image " + image.getImageURL() + ", " + ex.getMessage());
                    }
                }
            }
        }
    }


    /**
     * Download and cache favicons for all feeds referenced by the topics.
     * @param topics A list of topics.
     * @param cacheDir Where to store the local favicons. 
     */
    private void cacheIcons(List<Topic> topics, File cacheDir)
    {
        // Create a set of all required icons, eliminating duplicates so that we don't attempt to download any
        // more than once.
        Set<Image> favicons = new HashSet<Image>();
        for (Topic topic : topics)
        {
            List<WeightedItem<Article>> articles = topic.getArticles();
            for (WeightedItem<Article> article : articles)
            {
                favicons.add(article.getItem().getFeedIcon());
            }
        }

        for (Image icon : favicons)
        {
            File cachedFile = new File(cacheDir, icon.getCachedFileName());
            if (!cachedFile.exists()) // Don't fetch icons we already have.
            {
                try
                {
                    copyStream(icon.getImageURL().openConnection().getInputStream(),
                               new FileOutputStream(cachedFile));
                    LOG.debug("Downloaded favicon: " + icon.getImageURL());
                }
                catch (IOException ex)
                {
                    LOG.debug("Failed downloading favicon from default location: " + icon.getImageURL());
                    // If we can't find the favicon in the default location, fetch the web page and
                    // look for a "shortcut icon" link tag.  This is expensive but it is a one-off.
                    // Subsequent runs will pick up the cached version of the icon.
                    try
                    {
                        String page = fetchPage(icon.getArticleURL());
                        Matcher matcher = FAVICON_PATTERN.matcher(page);
                        if (matcher.find())
                        {
                            URL url = new URL(icon.getArticleURL(), matcher.group(1));
                            copyStream(url.openConnection().getInputStream(),
                                       new FileOutputStream(cachedFile));
                            LOG.debug("Downloaded favicon via web page: " + url.toString());
                        }
                        else
                        {
                            LOG.info("No favicon for: " + icon.getArticleURL());
                        }
                    }
                    catch (IOException ex2)
                    {
                        cachedFile.delete();
                        LOG.warn("Failed downloading home page for favicon: " + icon.getArticleURL());
                    }
                }
            }
        }
    }


    /**
     * Fetch a web page and return it as a string.
     * @param pageURL The page to fetch.
     * @return The contents of the page (HTML).
     * @throws IOException If there is a problem downloading the page.
     */
    private String fetchPage(URL pageURL) throws IOException
    {
        URLConnection urlConnection = pageURL.openConnection();
        InputStream inputStream = urlConnection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        copyStream(inputStream, buffer);
        String encoding = urlConnection.getContentEncoding();
        return new String(buffer.toByteArray(), encoding == null ? ENCODING : encoding);
    }


    /**
     * Resize the specified image file, maintaining its aspect ratio, so that it is no wider
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
        copyStream(resourceStream, new FileOutputStream(new File(outputDirectory, targetFileName)));
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
            copyStream(inputStream, new FileOutputStream(new File(outputDirectory, targetFileName)));
        }
        finally
        {
            inputStream.close();
        }
    }


    /**
     * Helper method to copy the contents of a stream to a file.
     * @param stream The stream to copy.
     * @param target The target stream to write the stream contents to.
     * @throws IOException If the stream cannot be copied.
     */
    private void copyStream(InputStream stream,
                            OutputStream target) throws IOException
    {
        BufferedInputStream input = new BufferedInputStream(stream);
        try
        {
            BufferedOutputStream output = new BufferedOutputStream(target);
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
        if (args.length < 3 || args.length > 4)
        {
            printUsage();
        }
        else
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
                double maxAgeHours = Double.parseDouble(args[2]);
                Date cutoffDate = new Date(System.currentTimeMillis() - Math.round(maxAgeHours * 3600000));
                List<Article> articles = new ArticleFetcher().getArticles(feeds, cutoffDate);
                List<Topic> topics = new Zeitgeist(articles).getTopics();
                LOG.info(topics.size() + " topics identified.");
                Publisher publisher = args.length > 3 ? new Publisher(new File(args[3])) : new Publisher();
                publisher.publish(topics, args[1], feeds.size(), articles.size(), 30, new File("."));
            }
            finally
            {
                feedListReader.close();
            }
        }
    }


    private static void printUsage()
    {
        System.err.println("java -jar zeitgeist-publisher.jar <feedlist> <title> <maxage> [templatedir]");
        System.err.println();
        System.err.println("  <feedlist>    - Path to a file listing RSS/Atom feeds, one per line.");
        System.err.println("  <title>       - A title passed to the templates.");
        System.err.println("  <maxage>      - The maximum age (in hours) of included articles.");
        System.err.println("  [templatedir] - Path to alternate templates to use in place of the defaults.");
        System.err.println();
        System.err.println("If no template directory is specified, default templates from the classpath are used.");
    }
}
