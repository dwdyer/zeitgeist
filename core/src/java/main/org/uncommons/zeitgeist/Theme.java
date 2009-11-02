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
