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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Identifies common themes across multiple articles from multiple RSS feeds.
 * Based on the non-negative matrix factorisation example in the book Programming
 * Collective Intelligence by Toby Segaran. 
 * @author Daniel Dyer
 */
public class Zeitgeist
{
    private static final int MINIMUM_ARTICLES_PER_THEME = 4;
    private static final double MINIMUM_ARTICLE_RELEVANCE = 8;

    private final List<URL> feeds;
    private final Date cutoffDate;
    // Which words appear in which articles and how many times.
    private final Map<Article, Map<String, Integer>> articleWordCounts = new HashMap<Article, Map<String, Integer>>();
    // How many articles does each word appear in.
    private final Map<String, Integer> globalWordCounts = new TreeMap<String, Integer>();


    /**
     * @param feeds A list of feeds to include in the analysis.
     * @param cutOffDate Exclude any articles before this date.
     */
    public Zeitgeist(List<URL> feeds,
                     Date cutOffDate)
    {
        this.feeds = feeds;
        this.cutoffDate = cutOffDate;
    }


    /**
     * @param feeds A list of feeds to include in the analysis.
     */
    public Zeitgeist(List<URL> feeds)
    {
        this(feeds, new Date(0)); // 1st January 1970.
    }


    public List<Theme> getThemes()
    {
        List<Article> articles = downloadArticles();

        // Eliminate any articles that are too old.
        Iterator<Article> iterator = articles.iterator();
        while (iterator.hasNext())
        {
            Article article = iterator.next();
            if (article.getDate() != null && article.getDate().before(cutoffDate))
            {
                iterator.remove();
            }
        }

        Matrix matrix = makeMatrix(articles);
        int themeCount = 40;//(int) Math.ceil(Math.sqrt(articles.size()));
        System.out.println("Estimating number of themes is " + themeCount);
        List<Matrix> factors = matrix.factorise(themeCount);
        return extractThemes(articles, factors.get(0), factors.get(1));
    }


    /**
     * Download all feeds and extract the articles.
     */
    private List<Article> downloadArticles()
    {
        List<Article> articles = new LinkedList<Article>();
        try
        {
            // Download the feeds in parallel so that it completes quicker.
            ExecutorService executor = Executors.newFixedThreadPool(feeds.size());
            List<Callable<List<Article>>> tasks = new ArrayList<Callable<List<Article>>>(feeds.size());
            for (final URL feedURL : feeds)
            {
                tasks.add(new FeedDownloadTask(feedURL, true));
            }

            List<Future<List<Article>>> results = executor.invokeAll(tasks);
            for (Future<List<Article>> result : results)
            {
                try
                {
                    articles.addAll(result.get());
                }
                catch (ExecutionException ex)
                {
                    // Log the failure for this feed, but carry on with other feeds.
                    ex.printStackTrace();
                }
            }
            executor.shutdown();
        }
        catch (InterruptedException ex)
        {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
            ex.printStackTrace();
        }
        return articles;
    }


    private List<Theme> extractThemes(List<Article> articles,
                                      Matrix weights,
                                      Matrix features)
    {
        int featureCount = features.getRowCount();

        List<List<WeightedItem<Article>>> themeArticles = new ArrayList<List<WeightedItem<Article>>>(featureCount);
        for (int i = 0; i < featureCount; i++)
        {
            themeArticles.add(new ArrayList<WeightedItem<Article>>());
        }

        for (int i = 0; i < articles.size(); i++)
        {
            // Identify strongest feature of article.
            double maxWeight = -1;
            int themeIndex = -1;
            for (int j = 0; j < featureCount; j++)
            {
                double featureWeight = weights.get(i, j);
                if (featureWeight > maxWeight)
                {
                    maxWeight = featureWeight;
                    themeIndex = j;
                }
            }
            if (maxWeight >= MINIMUM_ARTICLE_RELEVANCE) // Don't include articles with only tenuous links to the main theme.
            {
                WeightedItem<Article> weightedArticle = new WeightedItem<Article>(maxWeight, articles.get(i));
                int index = Collections.binarySearch(themeArticles.get(themeIndex),
                                                     weightedArticle,
                                                     Collections.reverseOrder());
                if (index < 0)
                {
                    index = -(index + 1);
                }
                themeArticles.get(themeIndex).add(index, weightedArticle);
            }
        }

        List<Theme> themes = new ArrayList<Theme>();
        for (List<WeightedItem<Article>> theme : themeArticles)
        {
            if (theme.size() >= MINIMUM_ARTICLES_PER_THEME)
            {
                themes.add(new Theme(theme));
            }
        }

        Collections.sort(themes, Collections.reverseOrder(new ThemeArticleCountComparator()));
        return themes;
    }



    private Matrix makeMatrix(List<Article> articles)
    {
        for (Article article : articles)
        {
            Map<String, Integer> wordCounts = article.getWordCounts();
            articleWordCounts.put(article, wordCounts);
            for (String word : wordCounts.keySet())
            {
                Integer count = globalWordCounts.get(word);
                globalWordCounts.put(word, 1 + (count == null ? 0 : count));
            }
        }

        List<String> words = listWords(articles, globalWordCounts);

        System.out.println("Total articles: " + articles.size());
        System.out.println("Total words: " + globalWordCounts.size());
        System.out.println("Key words: " + words.size());

        Matrix matrix = new Matrix(articles.size(), words.size());
        int row = 0;
        for (Article article : articles)
        {
            int column = 0;
            for (String word : words)
            {
                Integer count = articleWordCounts.get(article).get(word);
                matrix.set(row, column, count == null ? 0 : count);
                ++column;
            }
            ++row;
        }
        return matrix;
    }


    private List<String> listWords(List<Article> articles,
                                   Map<String, Integer> globalWordCounts)
    {
        List<String> words = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : globalWordCounts.entrySet())
        {
            if (entry.getValue() >= MINIMUM_ARTICLES_PER_THEME && entry.getValue() < articles.size() * 0.3)
            {
                words.add(entry.getKey());
            }
        }
        return words;
    }


}
