// ============================================================================
//   Copyright 2009 Daniel W. Dyer
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
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jdom.Element;

/**
 * Callable for downloading a feed.
 * @author Daniel Dyer
 */
class FeedDownloadTask implements Callable<List<Article>>
{
    private static final FeedFetcher FETCHER = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());

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
        SyndFeed feed = FETCHER.retrieveFeed(feedURL);

        List<SyndEntry> entries = feed.getEntries();
        for (SyndEntry entry : entries)
        {
            String text = extractContent(entry);
            
            Date articleDate = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();
            List<Image> images = extractImages(entry);

            feedArticles.add(new Article(entry.getTitle(),
                                         text,
                                         new URL(entry.getLink()),
                                         articleDate,
                                         images));
        }
        return feedArticles;
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
                 || enclosure.getType().equalsIgnoreCase("image/gif")))
            {
                if (!images.containsKey(enclosure.getUrl()))
                {
                    images.put(enclosure.getUrl(),
                               new Image(new URL(enclosure.getUrl()), new URL(entry.getLink())));
                }
            }
        }

        // Images may also be embedded via Yahoo! media RSS tags.
        if (entry.getForeignMarkup() instanceof List)
        {
            List<Element> foreignElements = (List<Element>) entry.getForeignMarkup();
            for (Element element : foreignElements)
            {
                if (element.getNamespacePrefix().equals("media")
                    && element.getName().equals("thumbnail"))
                {
                    String imageLink = element.getAttributeValue("url");
                    if (!images.containsKey(imageLink))
                    {
                        images.put(imageLink,
                                   new Image(new URL(imageLink), new URL(entry.getLink())));
                    }
                }
            }
        }

        // Sometimes images are embedded directly in the article using HTML <img> tags.
        if (includeInlineImages && entry.getDescription() != null)
        {
            Pattern imageTagPattern = Pattern.compile("<img.+?src=\"(\\S+?)\".*?>");
            Matcher matcher = imageTagPattern.matcher(entry.getDescription().getValue());
            while (matcher.find())
            {
                String imageLink = matcher.group(1);
                // We only use JPG images because others are more likely to be
                // not related to the story (e.g. icons and adverts).
                if (imageLink.toLowerCase().endsWith(".jpg") && !images.containsKey(imageLink))
                {
                    images.put(imageLink,
                               new Image(new URL(imageLink), new URL(entry.getLink())));
                }
            }
        }

        return new ArrayList<Image>(images.values());
    }


    /**
     * Extract the article content from a feed entry.  This content may be in the description
     * element or it may be elsewhere.
     * @return The article text, stripped of its mark-up.
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
