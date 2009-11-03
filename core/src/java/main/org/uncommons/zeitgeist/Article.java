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
                if (word.trim().length() > 0)
                {
                    String stem = new Stemmer().stem(word);
                    LOW_VALUE_WORDS.add(stem);
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


    public Article(String headline,
                   String text,
                   URL link,
                   Date date,
                   List<Image> images)
    {
        this.headline = headline;
        this.text = text;
        this.articleURL = link;
        this.date = date;
        this.images = Collections.unmodifiableList(images);
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


    public Map<String, Integer> getWordCounts()
    {
        Map<String, Integer> wordCounts = extractWords(text);
        // Add headline words to the word counts from the content text.
        for (Map.Entry<String, Integer> entry : extractWords(headline).entrySet())
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


    private Map<String, Integer> extractWords(String text)
    {
        Map<String, Integer> wordCounts = new HashMap<String, Integer>();
        String[] words = text.split("\\W+");
        for (String word : words)
        {
            String lower = word.toLowerCase();
            String stem = new Stemmer().stem(lower);
            if (lower.length() >= 3 && !LOW_VALUE_WORDS.contains(stem))
            {
                Integer count = wordCounts.get(stem);
                wordCounts.put(stem, count == null ? 1 : ++count);
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
