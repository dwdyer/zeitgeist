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
package org.uncommons.zeitgeist;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
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
    private static final FeedFetcher FETCHER = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());
    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile("img.+?src=\"(\\S+?)\"");

    private final URL feedURL;
    private final boolean includeInlineImages;


    FeedDownloadTask(URL feedURL,
                     boolean includeInlineImages)
    {
        this.feedURL = feedURL;
        this.includeInlineImages = includeInlineImages;
    }


    @SuppressWarnings("unchecked")
    public List<Article> call() throws Exception
    {
        List<Article> feedArticles = new LinkedList<Article>();
        try
        {
            SyndFeed feed = FETCHER.retrieveFeed(feedURL);
            LOG.debug("Fetched " + feedURL);

            Image feedLogo = getFeedLogo(feed);
            Image feedIcon = getFeedIcon(feed);
            
            List<SyndEntry> entries = feed.getEntries();
            for (SyndEntry entry : entries)
            {
                Date articleDate = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();

                feedArticles.add(new Article(entry.getTitle().trim(),
                                             extractContent(entry),
                                             new URL(feedURL, entry.getLink()),
                                             articleDate,
                                             extractImages(entry),
                                             feed.getTitle(),
                                             feedLogo,
                                             feedIcon));
            }
            return feedArticles;
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
            LOG.error("Failed fetching " + feedURL + ", invalid document.");
        }
        return Collections.emptyList();
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
                             new URL(feedURL, feed.getImage().getLink() == null ? feed.getLink() : feed.getImage().getLink()));
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
        URL feedLink = feed.getLink() != null ? new URL(feedURL, feed.getLink()) : feedURL;
        return new Image(new URL(feedLink, "/favicon.ico"), feedLink);
    }


    /**
     * Locates any images associated with a given article.  These may have been
     * embedded in several different ways.  This method looks in the most likely
     * places and returns a list of any images found.
     */
    @SuppressWarnings("unchecked")
    private List<Image> extractImages(SyndEntry entry) throws MalformedURLException
    {
        // Sometimes the same image is embedded using more than one method.  We need
        // to keep track of that and avoid adding duplicates.  We don't add an image
        // to the map if it already contains an image with the same URL.
        Map<String, Image> images = new LinkedHashMap<String, Image>();

        // The most likely/best place for an image is in an enclosure.
        List<SyndEnclosure> enclosures = entry.getEnclosures();
        for (SyndEnclosure enclosure : enclosures)
        {
            if ((enclosure.getType().equalsIgnoreCase("image/jpeg")
                 || enclosure.getType().equalsIgnoreCase("image/png")
                 || enclosure.getType().equalsIgnoreCase("image/gif"))
                || enclosure.getUrl().endsWith(".jpg"))
            {
                if (!images.containsKey(enclosure.getUrl()))
                {
                    images.put(enclosure.getUrl(),
                               new Image(new URL(feedURL, enclosure.getUrl()),
                                         new URL(feedURL, entry.getLink())));
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
                        if (!images.containsKey(imageLink))
                        {
                            images.put(imageLink,
                                       new Image(new URL(feedURL, imageLink),
                                                 new URL(feedURL, entry.getLink())));
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
                String imageLink = matcher.group(1);
                // We only use inline JPG images because others are more likely to be
                // not related to the story (e.g. icons and adverts).
                if (imageLink.toLowerCase().endsWith(".jpg") && !images.containsKey(imageLink))
                {
                    images.put(imageLink,
                               new Image(new URL(feedURL, imageLink),
                                         new URL(feedURL, entry.getLink())));
                }
            }
        }

        return new ArrayList<Image>(images.values());
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
