package org.uncommons.zeitgeist;

import java.net.URL;

/**
 * @author Daniel Dyer
 */
public class Image
{
    private final URL imageURL;
    private final URL articleURL;


    public Image(URL imageURL, URL articleURL)
    {
        this.imageURL = imageURL;
        this.articleURL = articleURL;
    }


    public URL getImageURL()
    {
        return imageURL;
    }


    public URL getArticleURL()
    {
        return articleURL;
    }
}
