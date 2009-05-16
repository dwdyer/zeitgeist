package org.uncommons.zeitgeist;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Dyer
 */
public class Theme
{
    private final List<WeightedItem<Article>> articles = new ArrayList<WeightedItem<Article>>();


    public Theme(List<WeightedItem<Article>> articles)
    {
        this.articles.addAll(articles);
    }


    public List<WeightedItem<Article>> getArticles()
    {
        return articles;
    }


    public List<Image> getImages()
    {
        List<Image> images = new ArrayList<Image>();
        for (WeightedItem<Article> article : articles)
        {
            images.addAll(article.getItem().getImages());
        }
        return images;
    }
}
