package org.uncommons.zeitgeist.publisher;

import org.antlr.stringtemplate.AttributeRenderer;

/**
 * A StringTemplate renderer that escapes ampersands in URLs.
 * @author Daniel Dyer
 */
class URLRenderer implements AttributeRenderer
{
    public String toString(Object o)
    {
        return o.toString().replaceAll("&","&amp;");
    }


    public String toString(Object o, String s)
    {
        return toString(o);
    }
}
