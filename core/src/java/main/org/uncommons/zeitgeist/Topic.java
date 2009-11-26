// ============================================================================
//   Copyright 2009 Daniel W. Dyer
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A topic is some unifying theme that links multiple individual articles.
 * @author Daniel Dyer
 */
public class Topic
{
    private final List<WeightedItem<Article>> articles = new ArrayList<WeightedItem<Article>>();


    public Topic(List<WeightedItem<Article>> articles)
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


    /**
     * Counts how many distinct feeds are represented by the articles that
     * make up this topic.
     * @return The number of distinct sources.
     */
    public int countDistinctSources()
    {
        Set<String> sources = new HashSet<String>();
        for (WeightedItem<Article> article : articles)
        {
            sources.add(article.getItem().getArticleURL().getHost());
        }
        return sources.size();
    }
}
