// ============================================================================
//   Copyright 2009-2012 Daniel W. Dyer
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

/**
 * Holds information about an image from a feed.
 * @author Daniel Dyer
 */
public class Image implements Comparable<Image>
{
    private final URL imageURL;
    private final URL articleURL;
    private final Integer width;


    public Image(URL imageURL, URL articleURL, Integer width)
    {
        this.imageURL = imageURL;
        this.articleURL = articleURL;
        this.width = width;
    }


    public URL getImageURL()
    {
        return imageURL;
    }


    /**
     * When storing a local cached version of this image, use this name.
     */
    public String getCachedFileName()
    {
        String remote = imageURL.toString();
        String local = remote.replaceAll("http://", "").replaceAll("https://", "").replaceAll("[/%]", "_");
        // Remove query string.
        int queryIndex = local.indexOf('?');
        if (queryIndex >= 0)
        {
            local = local.substring(0, queryIndex);
        }
        // Make sure the name has an appropriate extension so that it gets served with an appropriate mime type.
        if (!local.endsWith(".jpg") && !local.endsWith(".jpeg") && !local.endsWith(".ico"))
        {
            local += ".jpg";
        }
        return local;
    }


    public URL getArticleURL()
    {
        return articleURL;
    }


    /**
     * @return The width of the image in pixels or null if it is unknown.
     */
    public Integer getWidth()
    {
        return width;
    }


    public String getImageCredit()
    {
        String host = articleURL.getHost();
        // Remove common prefixes such as 'www', 'rss' or 'feeds' to keep the URL as short as possible.
        String credit = host.replaceFirst("^feeds\\.", "");
        credit = credit.replaceFirst("^rss\\.", "");
        credit = credit.replaceFirst("^www\\.", "");
        return credit;
    }


    /**
     * Larger (wider) images take precedence over smaller images.  When used for sorting this comparator
     * will order the images in descending order of width (this is the reverse of usual ordering for a
     * comparator).
     * @param image The image to compare to.
     */
    public int compareTo(Image image)
    {
        if (this.width != null && image.width != null)
        {
            return image.width - this.width;
        }
        else if (this.width != null)
        {
            return -1; // This image is considered bigger than the unknown width of the other image.
        }
        else if (image.width != null)
        {
            return 1; // This image is considered smaller than the known width of the other image.
        }
        return 0; // Both sizes are unknown and therefore considered equal.
    }
}
