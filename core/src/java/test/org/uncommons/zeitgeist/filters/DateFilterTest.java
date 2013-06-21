package org.uncommons.zeitgeist.filters;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import org.testng.annotations.Test;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.Image;

/**
 * Unit test for the {@link DateFilter} class.
 * @author Daniel Dyer
 */
public class DateFilterTest
{
    @Test
    public void testTooOld() throws MalformedURLException
    {
        Date cutOffDate = new GregorianCalendar(2013, 5, 21, 12, 0).getTime();
        DateFilter filter = new DateFilter(cutOffDate);

        Date articleDate = new GregorianCalendar(2013, 5, 20, 12, 0).getTime();
        Article article = new Article("", "", null, articleDate, Collections.<Image>emptyList(), "", null, null);
        assert !filter.keepArticle(article) : "Article before cut-off date should be excluded by filter.";
    }


    @Test
    public void testAfterCutOffDate() throws MalformedURLException
    {
        Date cutOffDate = new GregorianCalendar(2013, 5, 21, 12, 0).getTime();
        DateFilter filter = new DateFilter(cutOffDate);

        Date articleDate = new GregorianCalendar(2013, 5, 21, 18, 0).getTime();
        Article article = new Article("", "", null, articleDate, Collections.<Image>emptyList(), "", null, null);
        assert filter.keepArticle(article) : "Article after cut-off date should be included by filter.";
    }


    @Test
    public void testEqualsCutOffDate() throws MalformedURLException
    {
        Date cutOffDate = new GregorianCalendar(2013, 5, 21, 12, 0, 0).getTime();
        DateFilter filter = new DateFilter(cutOffDate);

        Article article = new Article("", "", null, cutOffDate, Collections.<Image>emptyList(), "", null, null);
        assert filter.keepArticle(article) : "Article on cut-off date should be included by filter.";
    }


    @Test
    public void testMissingArticleDate() throws MalformedURLException
    {
        Date cutOffDate = new GregorianCalendar(2013, 5, 21, 12, 0).getTime();
        DateFilter filter = new DateFilter(cutOffDate);

        Article article = new Article("", "", null, null, Collections.<Image>emptyList(), "", null, null);
        assert !filter.keepArticle(article) : "Article without date should be excluded by filter.";
    }
}
