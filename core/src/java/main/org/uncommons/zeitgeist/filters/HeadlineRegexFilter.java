package org.uncommons.zeitgeist.filters;

import java.util.regex.Pattern;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Article;

/**
 * Filter that excludes articles with headlines that match a given regular expression.
 * @author Daniel Dyer
 */
public class HeadlineRegexFilter implements ArticleFilter
{
    private static final SimpleLogger LOG = new SimpleLogger(HeadlineRegexFilter.class);

    private final Pattern pattern;

    public HeadlineRegexFilter(String pattern)
    {
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }


    @Override
    public boolean keepArticle(Article article)
    {
        if (pattern.matcher(article.getHeadline()).matches())
        {
            LOG.info("Headline blocked by filter: " + article.getHeadline() + '|' + article.getFeedTitle());
            return false;
        }
        return true;
    }
}
