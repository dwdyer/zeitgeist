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
        result = expandEntities(result);
        return stripPunctuation(result).toLowerCase();
    }


    private static String expandEntities(String text)
    {
        // Expand accented characters, apostrophes and currency symbols.
        // We don't care about case, so we'll just use lower case characters throughout.

        // For accented character, we strip the accents.  This is because we deal mostly
        // with English text and sometimes foreign terms are used with the accents and
        // sometimes without.  If this happens they are treated as different words which
        // means the algorithm will not detect the similarity in the articles.
        String result = text.replaceAll("&Agrave;|&#192;|&agrave;|&#224;", "a");
        result = result.replaceAll("&Aacute;|&#193;|&aacute;|&#225;", "a");
        result = result.replaceAll("&Acirc;|&#194;|&acirc;|&#226;", "a");
        result = result.replaceAll("&Atilde;|&#195;|&atilde;|&#227;", "a");
        result = result.replaceAll("&Auml;|&#196;|&auml;|&#228;", "a");
        result = result.replaceAll("&Aring;|&#197;|&aring;|&#229;", "a");
        result = result.replaceAll("&Ccedil;|&#199;|&ccedil;|&#231;", "c");
        result = result.replaceAll("&Egrave;|&#200;|&egrave;|&#232;", "e");
        result = result.replaceAll("&Eacute;|&#201;|&eacute;|&#233;", "e");
        result = result.replaceAll("&Ecirc;|&#202;|&ecirc;|&#234;", "e");
        result = result.replaceAll("&Euml;|&#203;|&euml;|&#235;", "e");
        result = result.replaceAll("&Igrave;|&#204;|&igrave;|&#236;", "i");
        result = result.replaceAll("&Iacute;|&#205;|&iacute;|&#237;", "i");
        result = result.replaceAll("&Icirc;|&#206;|&icirc;|&#238;", "i");
        result = result.replaceAll("&Iuml;|&#207;|&iuml;|&#239;", "i");
        result = result.replaceAll("&Ntilde;|&#209;|&ntilde;|&#241;", "n");
        result = result.replaceAll("&Ograve;|&#210;|&agrave;|&#242;", "o");
        result = result.replaceAll("&Oacute;|&#211;|&aacute;|&#243;", "o");
        result = result.replaceAll("&Ocirc;|&#212;|&acirc;|&#244;", "o");
        result = result.replaceAll("&Otilde;|&#213;|&atilde;|&#245;", "o");
        result = result.replaceAll("&Ouml;|&#214;|&auml;|&#246;", "o");
        result = result.replaceAll("&Oslash;|&#216;|&oslash;|&#248;", "o");
        result = result.replaceAll("&Ugrave;|&#217;|&ugrave;|&#249;", "u");
        result = result.replaceAll("&Uacute;|&#218;|&uacute;|&#250;", "u");
        result = result.replaceAll("&Ucirc;|&#219;|&ucirc;|&#251;", "u");
        result = result.replaceAll("&Uuml;|&#220;|&uuml;|&#252;", "u");
        result = result.replaceAll("&Yacute;|&#221;|&yacute;|&#253;", "y");
        result = result.replaceAll("&yuml;|&#255;", "y");
        result = result.replaceAll("&szlig;|&#223;", "ss");

        result = result.replaceAll("&apos;|&#39;", "'");

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
    private static String stripPunctuation(String text)
    {
        String result = text.replaceAll("[\\.\\,;\\:\\?\\s\\\"]+", " ");
        // An apostrophe or hyphen is only significant if it is between two letters
        // (i.e. there are no spaces on either side and it's not the first or last
        // character in the string).
        result = result.replaceAll("^'| '|' |'$|^-| -|- |-$", " ");
        return result.trim();
    }
}
