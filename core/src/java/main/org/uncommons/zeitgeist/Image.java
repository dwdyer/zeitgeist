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


    public String getImageCredit()
    {
        String host = articleURL.getHost();
        // Remove common prefixes such as 'www', 'rss' or 'feeds' to keep the URL as short as possible.
        String credit = host.replaceFirst("^feeds\\.", "");
        credit = credit.replaceFirst("^rss\\.", "");
        credit = credit.replaceFirst("^www\\.", "");
        return credit;
    }
}
