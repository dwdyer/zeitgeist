package org.uncommons.zeitgeist;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link Zeitgeist} class.
 * @author Daniel Dyer
 */
public class ZeitgeistTest
{
    private Article createTestArticle() throws MalformedURLException
    {
        return new Article("Test Article",
                           "Brief excerpt from a story about something interesting (or possibly mildly boring).",
                           new URL("http://localhost/article"),
                           new Date(),
                           Collections.<Image>emptyList(),
                           "Feed Title",
                           null,
                           null);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMaxArticlesLessThanMinArticles()
    {
        // Should throw IllegalArgumentException because minimum of 2 articles is incompatible with maximum of 1.
        new Zeitgeist(Collections.<Article>emptyList(), 2, 1, 1, 1);
    }


    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMaxArticlesLessThanMinSources()
    {
        // Should throw IllegalArgumentException because minimum of 2 articles is incompatible with maximum of 1.
        new Zeitgeist(Collections.<Article>emptyList(), 1, 1, 2, 1);
    }


    /**
     * Testing the analysis is difficult because the results are generally not reproducible.
     * However, we can at least test the simplest possible case (a single article with no constraints
     * on the composition of topics).
     */
    @Test
    public void testSingleArticleAnalysis() throws MalformedURLException
    {
        // With no other constraints (e.g. minimum relevance > 0 or minimum number of articles or sources > 1),
        // a single article should result in a single topic.
        Zeitgeist zeitgeist = new Zeitgeist(Arrays.asList(createTestArticle()), 1, 1, 1, 0);
        List<Topic> topics = zeitgeist.getTopics();
        assert topics.size() == 1 : "Wrong number of topics: " + topics.size();
    }


    @Test(dependsOnMethods = "testSingleArticleAnalysis")
    public void testMinimumArticleRelevance() throws MalformedURLException
    {
        // This test is identical to the previous one except it requires an unattainable relevance
        // score, so there should be no topics in the results.
        Zeitgeist zeitgeist = new Zeitgeist(Arrays.asList(createTestArticle()), 1, 1, 1, Integer.MAX_VALUE);
        List<Topic> topics = zeitgeist.getTopics();
        assert topics.isEmpty() : "Wrong number of topics: " + topics.size();
    }


    @Test(dependsOnMethods = "testSingleArticleAnalysis")
    public void testMinimumArticlesPerTopic() throws MalformedURLException
    {
        // This test is identical to the first one except it requires more articles than are available,
        // so there should be no topics in the results.
        Zeitgeist zeitgeist = new Zeitgeist(Arrays.asList(createTestArticle()), 2, 2, 1, 0);
        List<Topic> topics = zeitgeist.getTopics();
        assert topics.isEmpty() : "Wrong number of topics: " + topics.size();
    }


    @Test(dependsOnMethods = "testSingleArticleAnalysis")
    public void testMinimumSourcesPerTopic() throws MalformedURLException
    {
        // This test is identical to the first one except it requires more sources than are available,
        // so there should be no topics in the results.
        Zeitgeist zeitgeist = new Zeitgeist(Arrays.asList(createTestArticle()), 2, 2, 2, 0);
        List<Topic> topics = zeitgeist.getTopics();
        assert topics.isEmpty() : "Wrong number of topics: " + topics.size();
    }
}
