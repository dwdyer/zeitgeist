// ============================================================================
//   Copyright 2009-2010 Daniel W. Dyer
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.grlea.log.SimpleLogger;

/**
 * Identifies common topics across multiple articles from multiple RSS feeds.
 * Based on the non-negative matrix factorisation example in the book Programming
 * Collective Intelligence by Toby Segaran. 
 * @author Daniel Dyer
 */
public class Zeitgeist
{
    private static final SimpleLogger LOG = new SimpleLogger(FeedDownloadTask.class);
    private static final int MINIMUM_SOURCES_PER_TOPIC = 3;
    private static final int MINIMUM_ARTICLES_FOR_KEYWORD = 4; // Ignore obscure words.
    private static final double MINIMUM_ARTICLE_RELEVANCE = 8;

    private final List<Article> articles;

    /**
     * Create a Zeitgeist from the specified list of articles.  Typically the
     * list of articles is acquired from an {@link ArticleFetcher}.
     * @param articles A list of articles fetched from one or more feeds.
     */
    public Zeitgeist(List<Article> articles)
    {
        this.articles = articles;
    }


    public List<Topic> getTopics()
    {
        Matrix matrix = makeMatrix(articles);
        int topicCount = (int) Math.ceil(Math.sqrt(matrix.getColumnCount()));
        LOG.debug("Estimating number of topics is " + topicCount);
        List<Matrix> factors = matrix.factorise(topicCount);
        return extractTopics(articles, factors.get(0), factors.get(1));
    }


    private List<Topic> extractTopics(List<Article> articles,
                                      Matrix weights,
                                      Matrix features)
    {
        int featureCount = features.getRowCount();

        List<List<WeightedItem<Article>>> articlesByTopic = new ArrayList<List<WeightedItem<Article>>>(featureCount);
        for (int i = 0; i < featureCount; i++)
        {
            articlesByTopic.add(new ArrayList<WeightedItem<Article>>());
        }

        for (int i = 0; i < articles.size(); i++)
        {
            // Identify strongest feature of article.
            double maxWeight = -1;
            int topicIndex = -1;
            for (int j = 0; j < featureCount; j++)
            {
                double featureWeight = weights.get(i, j);
                if (featureWeight > maxWeight)
                {
                    maxWeight = featureWeight;
                    topicIndex = j;
                }
            }
            if (maxWeight >= MINIMUM_ARTICLE_RELEVANCE) // Don't include articles with only tenuous links to the main topic.
            {
                WeightedItem<Article> weightedArticle = new WeightedItem<Article>(maxWeight, articles.get(i));
                int index = Collections.binarySearch(articlesByTopic.get(topicIndex),
                                                     weightedArticle,
                                                     Collections.reverseOrder());
                if (index < 0)
                {
                    index = -(index + 1);
                }
                articlesByTopic.get(topicIndex).add(index, weightedArticle);
            }
        }

        List<Topic> topics = new ArrayList<Topic>();
        for (List<WeightedItem<Article>> topicArticles : articlesByTopic)
        {
            Topic topic = new Topic(topicArticles);
            int sources = topic.countDistinctSources();
            if (sources >= MINIMUM_SOURCES_PER_TOPIC)
            {
                topics.add(topic);
            }
            else
            {
                String detail = topicArticles.isEmpty() ? "???" : topicArticles.get(0).getItem().getHeadline();
                LOG.verbose(String.format("Discarding topic \"%s\" (%d), too few sources (%d)",
                                          detail,
                                          topicArticles.size(),
                                          sources));
            }
        }

        Collections.sort(topics, Collections.reverseOrder(new TopicArticleCountComparator()));
        return topics;
    }



    private Matrix makeMatrix(List<Article> articles)
    {
        // Which words appear in which articles and how many times.
        Map<Article, Map<String, Integer>> articleWordCounts = new HashMap<Article, Map<String, Integer>>();
        // How many articles does each word appear in.
        Map<String, Integer> globalWordCounts = new TreeMap<String, Integer>();

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

        List<String> words = listWords(globalWordCounts);

        LOG.info("Total articles: " + articles.size());
        LOG.info("Total words: " + globalWordCounts.size());
        LOG.info("Key words: " + words.size());
        LOG.debug(words.toString());

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


    private List<String> listWords(Map<String, Integer> globalWordCounts)
    {
        List<String> words = new ArrayList<String>();
        for (Map.Entry<String, Integer> entry : globalWordCounts.entrySet())
        {
            // If a word doesn't occur in enough different articles, discard it.
            if (entry.getValue() >= MINIMUM_ARTICLES_FOR_KEYWORD)
            {
                words.add(entry.getKey());
            }
        }
        return words;
    }
}
