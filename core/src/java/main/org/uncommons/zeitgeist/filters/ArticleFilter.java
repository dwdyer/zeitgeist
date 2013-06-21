package org.uncommons.zeitgeist.filters;

import org.uncommons.zeitgeist.Article;

/**
 * @author Daniel Dyer
 */
public interface ArticleFilter
{
    boolean keepArticle(Article article);
}
