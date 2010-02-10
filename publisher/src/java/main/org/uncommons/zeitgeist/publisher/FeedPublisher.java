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

import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.Topic;

/**
 * Generate an RSS feed for Zeitgeist topics.
 * @author Daniel Dyer
 */
class FeedPublisher implements FilePublisher
{
    private final String channelLink;

    FeedPublisher(String channelLink)
    {
        this.channelLink = channelLink;
    }

    public void publish(List<Topic> topics,
                        String title,
                        int feedCount,
                        int articleCount) throws IOException
    {
        Channel channel = new Channel("rss_2.0");
        List<Item> items = new ArrayList<Item>(topics.size());
        for (Topic topic : topics)
        {
            Article topArticle = topic.getArticles().get(0).getItem();
            Item item = new Item();
            item.setTitle(topArticle.getHeadline());
            item.setLink(topArticle.getArticleURL().toString());
            items.add(item);
        }
        channel.setItems(items);
        channel.setTitle(title);
        channel.setLink(channelLink);
        channel.setDescription("This feed was constructed by automated analysis of "
                               + articleCount + " articles from " + feedCount + " news sources.  "
                               + "No humans were involved in the selection and classification of these headlines.");
        channel.setGenerator("Zeitgeist (http://zeitgeist.uncommons.org)");
        channel.setLastBuildDate(new Date());
        try
        {
            new WireFeedOutput().output(channel, new File("rss.xml"));
        }
        catch (FeedException ex)
        {
            throw new IOException("Failed generating feed.", ex);
        }
    }
}
