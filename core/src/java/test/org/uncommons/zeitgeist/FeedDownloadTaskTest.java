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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link FeedDownloadTask} class.
 * @author Daniel Dyer
 */
public class FeedDownloadTaskTest
{
    private static final URL SAMPLE_RSS_URL = FeedDownloadTaskTest.class.getClassLoader().getResource("org/uncommons/zeitgeist/test.rss");


    @Test
    public void testArticleExtraction() throws Exception
    {
        Reporter.log(SAMPLE_RSS_URL.toString());
        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(),
                                                            SAMPLE_RSS_URL,
                                                            new Date(0),
                                                            false);
        List<Article> articles = task.call();
        assert articles.size() == 10 : "Should be 10 articles, is " + articles.size();
    }


    @Test
    public void testCutOffDate() throws Exception
    {
        Reporter.log(SAMPLE_RSS_URL.toString());
        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(),
                                                            SAMPLE_RSS_URL,
                                                            new GregorianCalendar(2010,
                                                                                  Calendar.JANUARY,
                                                                                  1).getTime(),
                                                            false);
        List<Article> articles = task.call();
        // There are 10 articles in the feed, but only 3 from 2010.
        assert articles.size() == 3 : "Should be 3 articles, is " + articles.size();
    }
}
