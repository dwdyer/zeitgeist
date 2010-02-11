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

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.net.URL;
import org.grlea.log.SimpleLogger;

/**
 * Class for downloading articles from RSS/Atom feeds.
 * @author Daniel Dyer
 */
public class ArticleFetcher
{
    private static final SimpleLogger LOG = new SimpleLogger(ArticleFetcher.class);

    /**
     * Download the specified feeds and extract the articles.  If any of the feeds cannot be retrieved
     * or parsed, an error will be logged but there will be no exception and the other feeds will be processed
     * as normal.
     * @param feeds A list of URLs of RSS/Atom feeds to download.
     * @return A list of articles extracted from the specified feed.  The articles will be grouped
     * by feed, in the order that the feeds were specified.
     */
    public List<Article> getArticles(List<URL> feeds)
    {
        List<Article> articles = new LinkedList<Article>();
        try
        {
            // Download the feeds in parallel so that it completes quicker.
            ExecutorService executor = Executors.newFixedThreadPool(feeds.size());
            List<Callable<List<Article>>> tasks = new ArrayList<Callable<List<Article>>>(feeds.size());
            for (final URL feedURL : feeds)
            {
                tasks.add(new FeedDownloadTask(feedURL, true));
            }

            List<Future<List<Article>>> results = executor.invokeAll(tasks);
            for (Future<List<Article>> result : results)
            {
                try
                {
                    articles.addAll(result.get());
                }
                catch (ExecutionException ex)
                {
                    // Log the failure for this feed, but carry on with other feeds.
                    LOG.errorException(ex.getCause());
                }
            }
            executor.shutdown();
        }
        catch (InterruptedException ex)
        {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
            ex.printStackTrace();
        }
        return articles;
    }
}
