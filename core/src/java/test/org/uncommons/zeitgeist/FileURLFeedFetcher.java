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

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FetcherEvent;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.AbstractFeedFetcher;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Feed fetcher that works with file:// URLs.  Used for testing.
 * @author Daniel Dyer
 */
class FileURLFeedFetcher extends AbstractFeedFetcher
{
    public SyndFeed retrieveFeed(URL url) throws IOException,
                                                 FeedException,
                                                 FetcherException
    {
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        SyndFeed feed = readSyndFeedFromStream(inputStream);
        fireEvent(FetcherEvent.EVENT_TYPE_FEED_RETRIEVED, connection, feed);
        return feed;
    }


	private SyndFeed readSyndFeedFromStream(InputStream inputStream) throws IOException,
                                                                            FeedException
    {
        XmlReader reader = new XmlReader(new BufferedInputStream(inputStream), true);
        try
        {
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            syndFeedInput.setPreserveWireFeed(isPreserveWireFeed());

            return syndFeedInput.build(reader);
        }
        finally
        {
            reader.close();
        }
	}
}
