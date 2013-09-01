package org.uncommons.zeitgeist.publisher;

import java.util.Locale;
import org.stringtemplate.v4.AttributeRenderer;

/**
 * A StringTemplate renderer that escapes ampersands in URLs.
 * @author Daniel Dyer
 */
class URLRenderer implements AttributeRenderer
{
    @Override
    public String toString(Object o, String s, Locale locale)
    {
        return o.toString().replaceAll("&","&amp;");
    }
}
