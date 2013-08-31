package org.uncommons.zeitgeist.filters;

import java.util.Collections;
import org.testng.annotations.Test;
import org.uncommons.zeitgeist.Article;
import org.uncommons.zeitgeist.Image;

/**
 * Unit test for the {@link HeadlineRegexFilter} class.
 * @author Daniel Dyer
 */
public class HeadlineRegexFilterTest
{
    @Test
    public void testRegex()
    {
        ArticleFilter filter = new HeadlineRegexFilter("Hello");
        Article article1 = new Article("Hello", "", null, null, Collections.<Image>emptyList(), "", null, null);
        assert !filter.keepArticle(article1) : "Matching headline should be excluded.";
        Article article2 = new Article("Goodbye", "", null, null, Collections.<Image>emptyList(), "", null, null);
        assert filter.keepArticle(article2) : "Non-matching headline should be included.";
    }
}
