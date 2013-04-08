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
package org.uncommons.zeitgeist;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.grlea.log.SimpleLogger;
import org.jdom.Element;

/**
 * Callable for downloading a feed.
 * @author Daniel Dyer
 */
class FeedDownloadTask implements Callable<List<Article>>
{
    private static final SimpleLogger LOG = new SimpleLogger(FeedDownloadTask.class);

    // Processing HTML with regexes is seldom sensible, but since this junk is encoded in so many
    // different ways (plain HTML, entity-encoded elements, CDATA, some combination of all of those),
    // using a proper parser won't help much either.  This is a best efforts attempt to extract images
    // embedded within feed content (as opposed to nicely marked up in the XML of the feed).  If it
    // misses some it doesn't really matter.
    private static final String QUOTE_RE = "(?:\"|'|\\Q&quot;\\E)";
    private static final String SRC_RE = "src=" + QUOTE_RE + "(\\S+?)" + QUOTE_RE;
    private static final String WIDTH_RE = "(?:width=" + QUOTE_RE + "(\\d+)" + QUOTE_RE + ")?";
    // The width attribute may be before the src attribute, after it, or absent entirely.
    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile("img.+?" + WIDTH_RE + ".*?" + SRC_RE + ".*?" + WIDTH_RE,
                                                                     Pattern.CASE_INSENSITIVE);

    private final FeedFetcher fetcher;
    private final URL feedURL;
    private final Date cutOffDate;
    private final boolean includeInlineImages;


    FeedDownloadTask(FeedFetcher fetcher,
                     URL feedURL,
                     Date cutOffDate,
                     boolean includeInlineImages)
    {
        this.fetcher = fetcher;
        this.feedURL = feedURL;
        this.cutOffDate = cutOffDate;
        this.includeInlineImages = includeInlineImages;
    }


    @SuppressWarnings("unchecked")
    public List<Article> call() throws Exception
    {
        List<Article> feedArticles = new LinkedList<Article>();
        try
        {
            SyndFeed feed = fetcher.retrieveFeed(feedURL);
            LOG.debug("Fetched " + feedURL);

            Image feedLogo = getFeedLogo(feed);
            Image feedIcon = getFeedIcon(feed);
            
            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry : entries)
            {
                URL articleURL = extractArticleURL(entry);
                Date articleDate = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();

                // If we don't know when the article was published then we don't know if it is relevant,
                // so we omit it.
                // This can be caused by the feed wrongly reporting that it is RSS version 0.91 and ROME
                // therefore not even bothering to look for item dates.
                if (articleDate == null)
                {
                    LOG.warn("Article has no publication date: " + articleURL);
                }
                // Don't include articles that were published before the cut-off date.
                else if (!articleDate.before(cutOffDate))
                {
                    feedArticles.add(new Article(FeedUtils.expandEntities(entry.getTitle().trim()),
                                                 extractContent(entry),
                                                 articleURL,
                                                 articleDate,
                                                 extractImages(entry, articleURL),
                                                 feed.getTitle(),
                                                 feedLogo,
                                                 feedIcon));
                }
            }
        }
        catch (FetcherException ex)
        {
            LOG.error("Failed fetching " + feedURL + ", " + ex.getMessage());
        }
        catch (UnknownHostException ex)
        {
            LOG.error("Failed fetching " + feedURL + ", unknown host.");
        }
        catch (IllegalArgumentException ex)
        {
            LOG.error("Failed fetching " + feedURL + ", " + ex.getMessage());
        }
        return feedArticles;
    }


    /**
     * Get the URL for the article.  Usually that's straightforward but FeedBurner feeds may have redirect
     * URLs, in which case we need to find the original link.
     */
    @SuppressWarnings("unchecked")
    private URL extractArticleURL(SyndEntry entry) throws MalformedURLException
    {
        String articleLink = entry.getLink();
        // This might be a FeedBurner redirected link.
        if (entry.getForeignMarkup() instanceof List)
        {
            List<Element> foreignElements = (List<Element>) entry.getForeignMarkup();
            for (Element element : foreignElements)
            {
                if (element.getNamespacePrefix().equals("feedburner") && element.getName().equals("origLink"))
                {
                    articleLink = element.getValue();
                    break;
                }
            }
        }
        return new URL(feedURL, articleLink);
    }


    /**
     * Determines the location of the large feed logo image for a given feed.
     * @param feed The feed for which to retrieve a logo.
     * @return The feed logo image information, or null if there isn't a logo.
     */
    private Image getFeedLogo(SyndFeed feed) throws MalformedURLException
    {
        if (feed.getImage() == null)
        {
            return null;
        }
        else
        {
            return new Image(new URL(feedURL, feed.getImage().getUrl()),
                             new URL(feedURL, feed.getImage().getLink() == null ? feed.getLink() : feed.getImage().getLink()),
                             null);
        }
    }


    /**
     * Determines the location of the 16x16 icon (favicon) for a given feed.
     * @param feed The feed for which to retrieve a favicon.
     * @return The feed icon image information.
     */
    private Image getFeedIcon(SyndFeed feed) throws MalformedURLException
    {
        // Most sites have a favicon.ico file at the root.  Some specify another location
        // using a link tag, but we don't support that at the moment as it would require
        // downloading and parsing the site home page.
        URL feedLink = feedURL;
        if (feed.getLink() != null)
        {
            try
            {
                feedLink = new URL(feedURL, feed.getLink());
            }
            catch (MalformedURLException ex)
            {
                // Sometimes a feed contains an invalid URL.
                LOG.warnException(ex);
            }
        }
        return new Image(new URL(feedLink, "/favicon.ico"), feedLink, 16);
    }


    /**
     * Locates any images associated with a given article.  These may have been
     * embedded in several different ways.  This method looks in the most likely
     * places and returns a list of any images found.
     */
    @SuppressWarnings("unchecked")
    private List<Image> extractImages(SyndEntry entry, URL articleURL) throws MalformedURLException
    {
        // Sometimes the same image is embedded using more than one method.  We need
        // to keep track of that and avoid adding duplicates.  We don't add an image
        // to the map if it already contains an image with the same URL.
        Map<String, Image> images = new LinkedHashMap<String, Image>();

        // The most likely/best place for an image is in an enclosure.
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        for (SyndEnclosure enclosure : enclosures)
        {
            String enclosureType = enclosure.getType();
            String enclosureUrl = enclosure.getUrl();
            boolean imageMimeType = enclosureType != null && (enclosureType.equalsIgnoreCase("image/jpeg")
                                                              || enclosureType.equalsIgnoreCase("image/png")
                                                              || enclosureType.equalsIgnoreCase("image/gif"));
            if (imageMimeType || (enclosureUrl != null && enclosureUrl.endsWith(".jpg")))
            {
                if (!images.containsKey(enclosureUrl))
                {
                    images.put(enclosureUrl,
                               new Image(new URL(feedURL, enclosureUrl), articleURL, null));
                }
            }
        }

        // Images may also be embedded via Yahoo! media RSS tags.
        if (entry.getForeignMarkup() instanceof List)
        {
            List<Element> foreignElements = (List<Element>) entry.getForeignMarkup();
            for (Element element : foreignElements)
            {
                if (element.getNamespacePrefix().equals("media"))
                {
                    String type = element.getAttributeValue("type");
                    if ((element.getName().equals("content") && type != null && type.equals("image/jpeg"))
                        || element.getName().equals("thumbnail"))
                    {
                        String imageLink = element.getAttributeValue("url");
                        String widthString = element.getAttributeValue("width");
                        Integer width = widthString == null ? null : Integer.parseInt(widthString);
                        if (!images.containsKey(imageLink))
                        {
                            images.put(imageLink,
                                       new Image(new URL(feedURL, imageLink), articleURL, width));
                        }
                    }
                }
            }
        }

        // Sometimes images are embedded directly in the article using HTML <img> tags.
        if (includeInlineImages && entry.getDescription() != null)
        {
            Matcher matcher = IMAGE_TAG_PATTERN.matcher(entry.getDescription().getValue());
            while (matcher.find())
            {
                String imageLink = matcher.group(2);
                // We only use inline JPG images because others are more likely to be
                // not related to the story (e.g. icons and adverts).
                if (imageLink.toLowerCase().contains(".jpg") && !images.containsKey(imageLink))
                {
                    String widthString = matcher.group(1);
                    if (widthString == null) // If the width attribute is not before the src, it might be after.
                    {
                        widthString = matcher.group(3);
                    }
                    Integer width = widthString == null ? null : new Integer(widthString);
                    images.put(imageLink,
                               new Image(new URL(feedURL, imageLink), articleURL, width));
                }
            }
        }

        List<Image> imageList = new ArrayList<Image>(images.values());
        Collections.sort(imageList);  // Prioritise larger images.
        return imageList;
    }


    /**
     * Extract the article content from a feed entry.  This content may be in the description
     * element or it may be elsewhere.
     * @return The article text.
     */
    @SuppressWarnings("unchecked")
    private String extractContent(SyndEntry entry)
    {
        StringBuilder textBuffer = new StringBuilder();
        if (entry.getDescription() != null)
        {
            textBuffer.append(entry.getDescription().getValue());
        }
        List<SyndContent> contents = entry.getContents();
        for (SyndContent content : contents)
        {
            textBuffer.append(' ');
            textBuffer.append(content.getValue());
        }
        return textBuffer.toString();
    }
}
