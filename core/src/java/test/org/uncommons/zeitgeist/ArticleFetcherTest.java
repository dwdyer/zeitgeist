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

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.uncommons.zeitgeist.filters.ArticleFilter;

/**
 * Unit test for the {@link ArticleFetcher} class.
 * @author Daniel Dyer
 */
public class ArticleFetcherTest
{
    @Test
    public void testArticleFetching()
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("newadventuresinsoftware.rss");
        Reporter.log(rssURL.toString());

        ArticleFetcher fetcher = new ArticleFetcher(new FileURLFeedFetcher());
        List<Article> articles = fetcher.getArticles(Arrays.asList(rssURL),
                                                     Collections.<ArticleFilter>emptyList());
        assert articles.size() == 10 : "Should be 10 articles, is " + articles.size();
    }
}
