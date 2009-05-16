package org.uncommons.zeitgeist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
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
        this.images = images;
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
        Map<String, Integer> wordCounts = extractWords(headline);
        wordCounts.putAll(extractWords(text));
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
