package org.uncommons.zeitgeist.filters;

import org.uncommons.zeitgeist.Article;

/**
 * Filter for determining which fetched articles should be discarded.
 * @author Daniel Dyer
 */
public interface ArticleFilter
{
    boolean keepArticle(Article article);
}
