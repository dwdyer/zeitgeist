package org.uncommons.zeitgeist;

import java.util.Comparator;
import java.io.Serializable;

/**
 * Comparator used for sorting themes in order of the number of articles that make up
 * the theme.
 * @author Daniel Dyer
 */
class ThemeArticleCountComparator implements Comparator<Theme>, Serializable
{
    public int compare(Theme o1, Theme o2)
    {
        return o1.getArticles().size() - o2.getArticles().size();
    }
}
