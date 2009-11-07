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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds all of the information (title, date, contents) about a single article.
 * @author Daniel Dyer
 */
public class Article
{
    private static final Set<String> LOW_VALUE_WORDS = new HashSet<String>();
    static
    {
        // Load the list of words that should be ignored.
        InputStream wordStream = Article.class.getClassLoader().getResourceAsStream("org/uncommons/zeitgeist/low-value-words.txt");
        BufferedReader wordReader = new BufferedReader(new InputStreamReader(wordStream));
        try
        {
            for (String word = wordReader.readLine(); word != null; word = wordReader.readLine())
            {
                String trimmed = word.trim();
                if (trimmed.length() > 0)
                {
                    LOW_VALUE_WORDS.add(trimmed);
                }
            }
        }
        catch (IOException ex)
        {
            throw new IllegalStateException(ex);
        }
        finally
        {
            try
            {
                wordReader.close();
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    
    private final String headline;
    private final String text;
    private final URL articleURL;
    private final Date date;
    private final List<Image> images;
    private final Image feedLogo;


    public Article(String headline,
                   String text,
                   URL link,
                   Date date,
                   List<Image> images,
                   Image feedLogo)
    {
        this.headline = headline;
        this.text = text;
        this.articleURL = link;
        this.date = date;
        this.images = Collections.unmodifiableList(images);
        this.feedLogo = feedLogo;
    }


    public String getHeadline()
    {
        return headline;
    }


    public String getText()
    {
        return text;
    }


    public URL getArticleURL()
    {
        return articleURL;
    }


    public Date getDate()
    {
        return date;
    }


    public List<Image> getImages()
    {
        return images;
    }


    public Image getFeedLogo()
    {
        return feedLogo;
    }


    public Map<String, Integer> getWordCounts()
    {
        Map<String, Integer> wordCounts = countWords(FeedUtils.stripMarkUpAndPunctuation(text));
        // Add headline words to the word counts from the content text.
        for (Map.Entry<String, Integer> entry : countWords(FeedUtils.stripMarkUpAndPunctuation(headline)).entrySet())
        {
            Integer count = wordCounts.get(entry.getKey());
            if (count == null)
            {
                count = 0;
            }
            wordCounts.put(entry.getKey(), count + entry.getValue());
        }
        return wordCounts;
    }


    /**
     * Count how many times each word occurs in the specified text.  Excludes words that are on
     * the "low value words" list (words like "the" and "it").
     * @param text The text to count the words of.
     * @return A map of word counts.
     */
    private Map<String, Integer> countWords(String text)
    {
        Map<String, Integer> wordCounts = new HashMap<String, Integer>();
        String[] words = text.split("\\W+");
        for (String word : words)
        {
            if (word.length() > 0 && !LOW_VALUE_WORDS.contains(word))
            {
                Integer count = wordCounts.get(word);
                wordCounts.put(word, count == null ? 1 : ++count);
            }
        }
        return wordCounts;
    }


    @Override
    public String toString()
    {
        return '[' + headline + "]\n" + text + '\n' + Arrays.toString(getWordCounts().keySet().toArray()) + '\n';
    }
}
