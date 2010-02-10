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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Topic;
import org.uncommons.zeitgeist.Zeitgeist;

/**
 * Simple HTML publisher for a set of topics.
 * @author Daniel Dyer
 */
public class Publisher
{
    private static final SimpleLogger LOG = new SimpleLogger(Publisher.class);
    private static final int CUTOFF_TIME_MS = 129600000; // 36 hours ago.


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
            Zeitgeist zeitgeist = new Zeitgeist(feeds, cutoffDate);
            List<Topic> topics = zeitgeist.getTopics();
            LOG.info(topics.size() + " topics identified.");
            new HTMLPublisher().publish(topics, args[1], zeitgeist.getFeedCount(), zeitgeist.getArticleCount());
        }
        finally
        {
            feedListReader.close();
        }
    }
}
