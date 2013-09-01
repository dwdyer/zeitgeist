package org.uncommons.zeitgeist.publisher;

import java.net.MalformedURLException;
import java.net.URL;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link URLRenderer} class.
 * @author Daniel Dyer
 */
public class URLRendererTest
{
    @Test
    public void testEscapeAmpersands() throws MalformedURLException
    {
        URL url = new URL("http://example.com/test?name1=value1&name2=value2");
        String escaped = new URLRenderer().toString(url, "The format parameter is irrelevant.", null);
        assert "http://example.com/test?name1=value1&amp;name2=value2".equals(escaped)
               : "Ampersand not correctly escaped: " + escaped;
    }
}
