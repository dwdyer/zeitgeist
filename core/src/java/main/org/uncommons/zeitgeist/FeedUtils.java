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

/**
 * Utility methods for manipulating feed content.
 * @author Daniel Dyer
 */
class FeedUtils
{
    private FeedUtils()
    {
        // Prevents instantiation of utility class.
    }


    /**
     * Naive and inefficient conversion of HTML to plain text.
     */
    public static String stripMarkUpAndPunctuation(String text)
    {
        // Remove all tags.
        String result = text.replaceAll("<.*?>"," ");
        result = anglicise(result);
        result = expandEntities(result);
        return stripPunctuation(result).toLowerCase();
    }


    /**
     * For accented characters, we strip the accents.  This is because we deal mostly
     * with English text and sometimes foreign terms are used with the accents and
     * sometimes without.  If this happens they are treated as different words which
     * means the algorithm will not detect the similarity in the articles.
     */
    static String anglicise(String text)
    {
        String result = text.replaceAll("&Agrave;|&#192;|\u00C0|&agrave;|&#224;|\u00E0", "a");
        result = result.replaceAll("&Aacute;|&#193;|\u00C1|&aacute;|&#225;|\u00E1", "a");
        result = result.replaceAll("&Acirc;|&#194;|\u00C2|&acirc;|&#226;|\u00E2", "a");
        result = result.replaceAll("&Atilde;|&#195;|\u00C3|&atilde;|&#227;|\u00E3", "a");
        result = result.replaceAll("&Auml;|&#196;|\u00C4|&auml;|&#228;|\u00E4", "a");
        result = result.replaceAll("&Aring;|&#197;|\u00C5|&aring;|&#229;|\u00E5", "a");
        result = result.replaceAll("&Ccedil;|&#199;|\u00C7|&ccedil;|&#231;|\u00E7", "c");
        result = result.replaceAll("&Egrave;|&#200;|\u00C8|&egrave;|&#232;|\u00E8", "e");
        result = result.replaceAll("&Eacute;|&#201;|\u00C9|&eacute;|&#233;|\u00E9", "e");
        result = result.replaceAll("&Ecirc;|&#202;|\u00CA|&ecirc;|&#234;|\u00EA", "e");
        result = result.replaceAll("&Euml;|&#203;|\u00CB|&euml;|&#235;|\u00EB", "e");
        result = result.replaceAll("&Igrave;|&#204;|\u00CC|&igrave;|&#236;|\u00EC", "i");
        result = result.replaceAll("&Iacute;|&#205;|\u00CD|&iacute;|&#237;|\u00ED", "i");
        result = result.replaceAll("&Icirc;|&#206;|\u00CE|&icirc;|&#238;|\u00EE", "i");
        result = result.replaceAll("&Iuml;|&#207;|\u00CF|&iuml;|&#239;|\u00EF", "i");
        result = result.replaceAll("&Ntilde;|&#209;|\u00D1|&ntilde;|&#241;|\u00F1", "n");
        result = result.replaceAll("&Ograve;|&#210;|\u00D2|&ograve;|&#242;|\u00F2", "o");
        result = result.replaceAll("&Oacute;|&#211;|\u00D3|&oacute;|&#243;|\u00F3", "o");
        result = result.replaceAll("&Ocirc;|&#212;|\u00D4|&ocirc;|&#244;|\u00F4", "o");
        result = result.replaceAll("&Otilde;|&#213;|\u00D5|&otilde;|&#245;|\u00F5", "o");
        result = result.replaceAll("&Ouml;|&#214;|\u00D6|&ouml;|&#246;|\u00F6", "o");
        result = result.replaceAll("&Oslash;|&#216;|\u00D8|&oslash;|&#248;|\u00F8", "o");
        result = result.replaceAll("&Ugrave;|&#217;|\u00D9|&ugrave;|&#249;|\u00F9", "u");
        result = result.replaceAll("&Uacute;|&#218;|\u00DA|&uacute;|&#250;|\u00FA", "u");
        result = result.replaceAll("&Ucirc;|&#219;|\u00DB|&ucirc;|&#251;|\u00FB", "u");
        result = result.replaceAll("&Uuml;|&#220;|\u00DC|&uuml;|&#252;|\u00FC", "u");
        result = result.replaceAll("&Yacute;|&#221;|\u00DD|&yacute;|&#253;|\u00FD", "y");
        result = result.replaceAll("&szlig;|&#223;|\u00DF", "ss");
        result = result.replaceAll("&yuml;|&#255;|\u00FF", "y");
        return result;
    }


    static String expandEntities(String text)
    {
        // Expand apostrophes and currency symbols.
        String result = text.replaceAll("&apos;|&#39;", "'");

        result = result.replaceAll("&cent;|&#162;", "\u00A2");
        result = result.replaceAll("&pound;|&#163;", "\u00A3");
        result = result.replaceAll("&euro;", "\u20AC");
        result = result.replaceAll("&yen;|&#165;", "\u00A5");

        // All other entities can be ignored (replaced by whitespace).
        // We use spaces rather than empty strings because the entity might
        // be separating two words and they would otherwise become concatenated.
        result = result.replaceAll("&\\w*;"," ");
        
        return result;
    }


    /**
     * Strips all non-significant punctuation.  Only hyphens and apostrophes within
     * words are retained.  New lines, commas, full-stops, quotes and excess white space
     * are removed.
     */
    static String stripPunctuation(String text)
    {
        // Remove commas from numbers.
        String result = text.replaceAll("(\\d),(\\d)", "$1$2");
            
        result = result.replaceAll("[\\.,;:\\?\\s\"\\(\\)&\\|/\u2013\u2022]+", " ");
        // An apostrophe or hyphen is only significant if it is between two letters
        // (i.e. there are no spaces on either side and it's not the first or last
        // character in the string).
        result = result.replaceAll("^'| '|' |'$|^-| -|- |-$", " ");
        // Strip possessive apostrophes.
        result = result.replaceAll("'s\\s|\u2019s\\s", " ");
        return result.trim();
    }
}
