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
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Callable for downloading a feed.
 * @author Daniel Dyer
 */
class FeedDownloadTask implements Callable<List<Article>>
{
    private static final FeedFetcher FETCHER = new HttpURLFeedFetcher(HashMapFeedInfoCache.getInstance());

    private final URL feedURL;


    FeedDownloadTask(URL feedURL)
    {
        this.feedURL = feedURL;
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
            List<SyndEnclosure> enclosures = entry.getEnclosures();
            List<Image> images = new ArrayList<Image>(enclosures.size());
            for (SyndEnclosure enclosure : enclosures)
            {
                if (enclosure.getType().equalsIgnoreCase("image/jpeg")
                    || enclosure.getType().equalsIgnoreCase("image/png")
                    || enclosure.getType().equalsIgnoreCase("image/gif"))
                {
                    images.add(new Image(new URL(enclosure.getUrl()), new URL(entry.getLink())));
                }
            }
            feedArticles.add(new Article(entry.getTitle(),
                                         text,
                                         new URL(entry.getLink()),
                                         articleDate,
                                         images));
        }
        return feedArticles;
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
            textBuffer.append('\n');
            textBuffer.append(content.getValue());
        }
        return stripMarkUp(textBuffer.toString());
    }


    /**
     * Naive and inefficient conversion of HTML to plain text.
     */
    private String stripMarkUp(String text)
    {
        // Remove all tags.
        String result = text.replaceAll("<.*?>"," ");
        // Expand entities.
        result = result.replaceAll("&#39;", "'");
        result = result.replaceAll("&#160;", " ");
        result = result.replaceAll("&amp;", "&");
        result = result.replaceAll("&#163;", "£");
        result = result.replaceAll("&pound;", "£");
        result = result.replaceAll("&euro;", "€");
        result = result.replaceAll("&ccedil;", "ç");
        result = result.replaceAll("&lt;", "<");
        result = result.replaceAll("&gt;", ">");
        return result.trim();
    }
}
