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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link Image} class.
 * @author Daniel Dyer
 */
public class ImageTest
{
    @Test
    public void testImageCredit() throws MalformedURLException
    {
        Image image = new Image(new URL("http://images.example.com/image.jpg"),
                                new URL("http://www.uncommons.org/article.html"),
                                null);
        String credit = image.getImageCredit();
        // Image credit should be derived from the article URL, not the URL of the image.
        assert credit.equals("uncommons.org") : "Incorrect image credit: " + credit;
    }


    @Test
    public void testGenerateCachedFileName() throws MalformedURLException
    {
        Image image = new Image(new URL("http://images.example.com/image.jpg"),
                                new URL("http://www.uncommons.org/article.html"),
                                null);
        String cachedName = image.getCachedFileName();
        assert cachedName.equals("images.example.com_image.jpg") : "Incorrect file name: " + cachedName;
    }


    /**
     * Test that query string is stripped from file name.
     */
    @Test
    public void testGenerateCachedFileNameOmitQueryString() throws MalformedURLException
    {
        Image image = new Image(new URL("http://images.example.com/image.ico?junk"),
                                new URL("http://www.uncommons.org/article.html"),
                                null);
        String cachedName = image.getCachedFileName();
        assert cachedName.equals("images.example.com_image.ico") : "Incorrect file name: " + cachedName;
    }


    /**
     * Test that query string is stripped from file name.
     */
    @Test
    public void testGenerateCachedFileNameOmitURLEncoding() throws MalformedURLException
    {
        Image image = new Image(new URL("http://images.example.com/image%2Csomething.jpeg"),
                                new URL("http://www.uncommons.org/article.html"),
                                null);
        String cachedName = image.getCachedFileName();
        assert cachedName.equals("images.example.com_image_2Csomething.jpeg") : "Incorrect file name: " + cachedName;
    }


    @Test
    public void testGenerateCachedFileNameWithExtension() throws MalformedURLException
    {
        Image image = new Image(new URL("http://images.example.com/image.bin"),
                                new URL("http://www.uncommons.org/article.html"),
                                null);
        String cachedName = image.getCachedFileName();
        assert cachedName.equals("images.example.com_image.bin.jpg");
    }


    @Test
    public void testSortByWidth() throws MalformedURLException
    {
        Image smallest = new Image(new URL("http://example.com/smallest.jpg"),
                                   new URL("http://example.com/smallest.html"),
                                   100);
        Image middle = new Image(new URL("http://example.com/middle.jpg"),
                                 new URL("http://example.com/middle.html"),
                                 200);
        Image largest = new Image(new URL("http://example.com/largest.jpg"),
                                  new URL("http://example.com/largest.html"),
                                  300);
        List<Image> images = new ArrayList<Image>();
        images.add(smallest);
        images.add(middle);
        images.add(largest);
        Collections.sort(images);
        assert images.get(0) == largest : "Wrong image: " + images.get(0).getWidth();
        assert images.get(1) == middle : "Wrong image: " + images.get(1).getWidth();
        assert images.get(2) == smallest : "Wrong image: " + images.get(2).getWidth();
    }


    @Test
    public void testStableSortUnknownWidths() throws MalformedURLException
    {
        Image image1 = new Image(new URL("http://example.com/1.jpg"),
                                 new URL("http://example.com/1.html"),
                                 null);
        Image image2 = new Image(new URL("http://example.com/2.jpg"),
                                 new URL("http://example.com/2.html"),
                                 null);
        Image image3 = new Image(new URL("http://example.com/3.jpg"),
                                 new URL("http://example.com/3.html"),
                                 null);
        List<Image> images = new ArrayList<Image>();
        images.add(image1);
        images.add(image2);
        images.add(image3);
        // Unknown widths are considered equal and the sort should be stable (i.e. equal elements are
        // not reordered).
        Collections.sort(images);
        assert images.get(0) == image1 : "Wrong image.";
        assert images.get(1) == image2 : "Wrong image.";
        assert images.get(2) == image3 : "Wrong image.";
    }


    @Test
    public void testSortKnownAndUnknownWidths() throws MalformedURLException
    {
        Image image1 = new Image(new URL("http://example.com/1.jpg"),
                                 new URL("http://example.com/1.html"),
                                 100);
        Image image2 = new Image(new URL("http://example.com/2.jpg"),
                                 new URL("http://example.com/2.html"),
                                 null);
        Image image3 = new Image(new URL("http://example.com/3.jpg"),
                                 new URL("http://example.com/3.html"),
                                 200);
        List<Image> images = new ArrayList<Image>();
        images.add(image1);
        images.add(image2);
        images.add(image3);
        Collections.sort(images);
        // The unknown width image should be sorted after the known widths.
        assert images.get(2) == image2 : "Wrong image.";
    }
}
