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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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
import org.uncommons.zeitgeist.filters.DateFilter;

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
    private static final int TIMEOUT = 30000;

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
     *
     * @param topics A list of topics identified.
     * @param title Title for generated pages.
     * @param feedCount The number of feeds used.
     * @param articleCount The number of articles analysed.
     * @throws IOException If something goes wrong.
     */
    public void publish(List<Topic> topics,
                        String title,
                        int feedCount,
                        int articleCount,
                        File outputDir) throws IOException
    {
        group.registerRenderer(Date.class, new DateRenderer());
        group.registerRenderer(URL.class, new URLRenderer());
        group.registerRenderer(String.class, new XMLStringRenderer());

        cacheImages(topics, outputDir);
        cacheIcons(topics, outputDir);

        // Publish HTML.
        StringTemplate htmlTemplate = group.getInstanceOf("news");
        publishTemplate(topics, title, feedCount, articleCount, htmlTemplate, new File("index.html"));
        if (group.getRootDir() != null)
        {
            StreamUtils.copyFile(outputDir, new File(group.getRootDir(), "zeitgeist.css"), "zeitgeist.css");
        }
        else
        {

            StreamUtils.copyClasspathResource(outputDir, "zeitgeist.css", "zeitgeist.css");
        }

        if (group.isDefined("snippet"))
        {
            List<Topic> snippetTopics = topics.subList(0, Math.min(5, topics.size())); // Include no more than 5 topics.
            StringTemplate syndicateTemplate = group.getInstanceOf("snippet");
            publishTemplate(snippetTopics,
                            title,
                            feedCount,
                            articleCount,
                            syndicateTemplate,
                            new File("snippet.html"));
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
        template.setAttribute("topics", topics);
        template.setAttribute("title", title);
        template.setAttribute("dateTime", date);
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
                        StreamUtils.copyStreamToFile(openConnection(image.getImageURL()).getInputStream(),
                                                     new File(cacheDir, image.getCachedFileName()));
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
                    StreamUtils.copyStreamToFile(openConnection(icon.getImageURL()).getInputStream(), cachedFile);
                    // Some sites will serve up a zero-byte file for the default location
                    // but still have a valid icon elsewhere.
                    if (cachedFile.length() == 0)
                    {
                        cachedFile.delete();
                        extractFaviconFromHTML(icon, cachedFile);
                    }
                    else
                    {
                        LOG.debug("Downloaded favicon: " + icon.getImageURL());
                    }
                }
                catch (IOException ex)
                {
                    LOG.debug("Failed downloading favicon from default location: " + icon.getImageURL());
                    extractFaviconFromHTML(icon, cachedFile);
                }
            }
        }
    }


    /**
     * If we can't find the favicon in the default location, fetch the web page and
     * look for a "shortcut icon" link tag.  This is expensive but it is a one-off.
     * Subsequent runs will pick up the cached version of the icon.
     */
    private void extractFaviconFromHTML(Image icon, File cachedFile)
    {
        try
        {
            String page = fetchPage(icon.getArticleURL());
            Matcher matcher = FAVICON_PATTERN.matcher(page);
            if (matcher.find())
            {
                URL url = new URL(icon.getArticleURL(), matcher.group(1));
                StreamUtils.copyStreamToFile(openConnection(url).getInputStream(), cachedFile);
                LOG.debug("Downloaded favicon via web page: " + url.toString());
            }
            else
            {
                LOG.info("No favicon for: " + icon.getArticleURL());
            }
        }
        catch (IOException ex)
        {
            cachedFile.delete();
            LOG.warn("Failed downloading home page for favicon: " + icon.getArticleURL());
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
        URLConnection urlConnection = openConnection(pageURL);
        InputStream inputStream = urlConnection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        StreamUtils.copyStream(inputStream, buffer);
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
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(image, 0, 0, maxWidth, height, null);
            ImageIO.write(scaledImage, "jpeg", imageFile);
        }
    }


    /**
     * Open a URL connection and set the timeouts appropriately.
     */
    private URLConnection openConnection(URL url) throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        return connection;
    }


    /**
     * Entry point for the publisher application.  Accepts a single optional argument, which is the
     * path to the properties file (if not specified defaults to "zeitgeist.properties" in the current
     * working directory).
     */
    public static void main(String[] args) throws IOException
    {
        File propertiesFile = new File(args.length > 0 ? args[0] : "zeitgeist.properties");
        Properties properties = loadProperties(propertiesFile);

        List<URL> feeds = parseFeedList(properties.getProperty("zeitgeist.feedList"));

        long maxAgeHours = Long.parseLong(properties.getProperty("zeitgeist.maxArticleAgeHours"));
        Date cutoffDate = new Date(System.currentTimeMillis() - Math.round(maxAgeHours * 3600000));
        List<Article> articles = new ArticleFetcher().getArticles(feeds, Arrays.asList(new DateFilter(cutoffDate)));
        List<Topic> topics = new Zeitgeist(articles,
                                           Integer.parseInt(properties.getProperty("zeitgeist.minArticlesPerTopic")),
                                           Integer.parseInt(properties.getProperty("zeitgeist.minSourcesPerTopic")),
                                           Integer.parseInt(properties.getProperty("zeitgeist.minArticleRelevance"))).getTopics();
        LOG.info(topics.size() + " topics identified.");
        String templatesDir = properties.getProperty("zeitgeist.templatesDir");
        Publisher publisher = templatesDir != null ? new Publisher(new File(templatesDir)) : new Publisher();
        publisher.publish(topics,
                          properties.getProperty("zeitgeist.title"),
                          feeds.size(),
                          articles.size(),
                          new File("."));
    }


    /**
     * Load properties from the specified file.
     * @param propertiesFile The file from which to load property values.
     */
    private static Properties loadProperties(File propertiesFile) throws IOException
    {
        Properties properties = new Properties();
        InputStream propertiesStream = new FileInputStream(propertiesFile);
        try
        {
            properties.load(propertiesStream);
        }
        finally
        {
            propertiesStream.close();
        }
        return properties;
    }


    private static List<URL> parseFeedList(String arg) throws IOException
    {
        List<URL> feeds = new LinkedList<URL>();
        BufferedReader feedListReader = new BufferedReader(new FileReader(arg));
        try
        {
            for (String line = feedListReader.readLine(); line != null; line = feedListReader.readLine())
            {
                String url = line.trim();
                // Lines beginning with a hash are considered to be comments.
                if (!url.startsWith("#") && !url.isEmpty())
                {
                    feeds.add(new URL(url));
                }
            }
        }
        finally
        {
            feedListReader.close();
        }
        return feeds;
    }
}
