package org.uncommons.zeitgeist.filters;

import java.util.Date;
import org.grlea.log.SimpleLogger;
import org.uncommons.zeitgeist.Article;

/**
 * Filter for excluding articles that are too old or have an indeterminate date.
 * @author Daniel Dyer
 */
public class DateFilter implements ArticleFilter
{
    private static final SimpleLogger LOG = new SimpleLogger(DateFilter.class);

    private final Date cutOffDate;

    public DateFilter(Date cutOffDate)
    {
        this.cutOffDate = cutOffDate;
    }


    @Override
    public boolean keepArticle(Article article)
    {
        // If we don't know when the article was published then we don't know if it is relevant,
        // so we omit it.
        // This can be caused by the feed wrongly reporting that it is RSS version 0.91 and ROME
        // therefore not even bothering to look for item dates.
        if (article.getDate() == null)
        {
            LOG.warn("Article has no publication date: " + article.getArticleURL());
            return false;
        }
        else
        {
            return !article.getDate().before(cutOffDate);
        }
    }
}
