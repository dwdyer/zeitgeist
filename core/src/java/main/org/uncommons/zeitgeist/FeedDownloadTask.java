package org.uncommons.zeitgeist;

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
            String text = stripMarkUp(entry.getDescription().getValue());
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
