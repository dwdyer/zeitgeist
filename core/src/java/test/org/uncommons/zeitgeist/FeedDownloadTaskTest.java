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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Callable;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.uncommons.zeitgeist.filters.ArticleFilter;
import org.uncommons.zeitgeist.filters.DateFilter;

/**
 * Unit test for the {@link FeedDownloadTask} class.
 * @author Daniel Dyer
 */
public class FeedDownloadTaskTest
{
    @Test
    public void testArticleExtraction() throws Exception
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("newadventuresinsoftware.rss");
        Reporter.log(rssURL.toString());

        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(), rssURL, false);
        List<Article> articles = task.call();
        assert articles.size() == 10 : "Should be 10 articles, is " + articles.size();
    }


    @Test
    public void testCutOffDate() throws Exception
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("newadventuresinsoftware.rss");
        Reporter.log(rssURL.toString());

        Date cutOffDate = new GregorianCalendar(2010, Calendar.JANUARY, 1).getTime();
        ArticleFilter filter = new DateFilter(cutOffDate);
        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(),
                                                            rssURL,
                                                            Arrays.asList(filter),
                                                            false);
        List<Article> articles = task.call();
        // There are 10 articles in the feed, but only 3 from 2010.
        assert articles.size() == 3 : "Should be 3 articles, is " + articles.size();
    }


    @Test
    public void testExtractImagesFromEnclosures() throws Exception
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("telegraph.rss");
        Reporter.log(rssURL.toString());

        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(), rssURL, false);
        List<Article> articles = task.call();
        assert articles.size() == 1 : "Should be 1 article, is " + articles.size();
        List<Image> images = articles.get(0).getImages();
        assert images.size() == 1 : "Should be 1 image, is " + images.size();
    }


    @Test
    public void testExtractImagesFromYahooMediaTags() throws Exception
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("guardian.rss");
        Reporter.log(rssURL.toString());

        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(), rssURL, false);
        List<Article> articles = task.call();
        assert articles.size() == 1 : "Should be 1 article, is " + articles.size();
        List<Image> images = articles.get(0).getImages();
        assert images.size() == 2 : "Should be 2 images, is " + images.size();

        // Images should be sorted in descending order of size.
        assert images.get(0).getWidth() == 460 : "Wrong width: " + images.get(0).getWidth();
        assert images.get(1).getWidth() == 140 : "Wrong width: " + images.get(1).getWidth();
    }


    /**
     * Test extraction of images from mark-up in the feed entry body text.
     */
    @Test
    public void testExtractEmbeddedImages() throws Exception
    {
        URL rssURL = FeedDownloadTaskTest.class.getResource("express.rss");
        Reporter.log(rssURL.toString());

        Callable<List<Article>> task = new FeedDownloadTask(new FileURLFeedFetcher(), rssURL, true);
        List<Article> articles = task.call();
        assert articles.size() == 1 : "Should be 1 article, is " + articles.size();
        List<Image> images = articles.get(0).getImages();
        assert images.size() == 1 : "Should be 1 image, is " + images.size();
    }
}
