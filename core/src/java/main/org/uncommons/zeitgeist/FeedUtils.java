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

import java.util.regex.Pattern;

/**
 * Utility methods for manipulating feed content.
 * @author Daniel Dyer
 */
class FeedUtils
{
    private static final Pattern ACCENTED_A_PATTERN = Pattern.compile("&Agrave;|&#192;|\u00C0|&agrave;|&#224;|\u00E0|&Aacute;|&#193;|\u00C1|&aacute;|&#225;|\u00E1|&Acirc;|&#194;|\u00C2|&acirc;|&#226;|\u00E2|&Atilde;|&#195;|\u00C3|&atilde;|&#227;|\u00E3|&Auml;|&#196;|\u00C4|&auml;|&#228;|\u00E4|&Aring;|&#197;|\u00C5|&aring;|&#229;|\u00E5");
    private static final Pattern ACCENTED_C_PATTERN = Pattern.compile("&Ccedil;|&#199;|\u00C7|&ccedil;|&#231;|\u00E7");
    private static final Pattern ACCENTED_E_PATTERN = Pattern.compile("&Egrave;|&#200;|\u00C8|&egrave;|&#232;|\u00E8|&Eacute;|&#201;|\u00C9|&eacute;|&#233;|\u00E9|&Ecirc;|&#202;|\u00CA|&ecirc;|&#234;|\u00EA|&Euml;|&#203;|\u00CB|&euml;|&#235;|\u00EB");
    private static final Pattern ACCENTED_I_PATTERN = Pattern.compile("&Igrave;|&#204;|\u00CC|&igrave;|&#236;|\u00EC|&Iacute;|&#205;|\u00CD|&iacute;|&#237;|\u00ED|&Icirc;|&#206;|\u00CE|&icirc;|&#238;|\u00EE|&Iuml;|&#207;|\u00CF|&iuml;|&#239;|\u00EF");
    private static final Pattern ACCENTED_N_PATTERN = Pattern.compile("&Ntilde;|&#209;|\u00D1|&ntilde;|&#241;|\u00F1");
    private static final Pattern ACCENTED_O_PATTERN = Pattern.compile("&Ograve;|&#210;|\u00D2|&ograve;|&#242;|\u00F2|&Oacute;|&#211;|\u00D3|&oacute;|&#243;|\u00F3|&Ocirc;|&#212;|\u00D4|&ocirc;|&#244;|\u00F4|&Otilde;|&#213;|\u00D5|&otilde;|&#245;|\u00F5|&Ouml;|&#214;|\u00D6|&ouml;|&#246;|\u00F6|&Oslash;|&#216;|\u00D8|&oslash;|&#248;|\u00F8");
    private static final Pattern ACCENTED_U_PATTERN = Pattern.compile("&Ugrave;|&#217;|\u00D9|&ugrave;|&#249;|\u00F9|&Uacute;|&#218;|\u00DA|&uacute;|&#250;|\u00FA|&Ucirc;|&#219;|\u00DB|&ucirc;|&#251;|\u00FB|&Uuml;|&#220;|\u00DC|&uuml;|&#252;|\u00FC");
    private static final Pattern ACCENTED_Y_PATTERN = Pattern.compile("&Yacute;|&#221;|\u00DD|&yacute;|&#253;|\u00FD|&yuml;|&#255;|\u00FF");
    private static final Pattern SCHARFES_S_PATTERN = Pattern.compile("&szlig;|&#223;|\u00DF");
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[,;:\\?\\s\"\\(\\)&\\|/\u2013\u2022]+");

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
        String result = ACCENTED_A_PATTERN.matcher(text).replaceAll("a");
        result = ACCENTED_C_PATTERN.matcher(result).replaceAll("c");
        result = ACCENTED_E_PATTERN.matcher(result).replaceAll("e");
        result = ACCENTED_I_PATTERN.matcher(result).replaceAll("i");
        result = ACCENTED_N_PATTERN.matcher(result).replaceAll("n");
        result = ACCENTED_O_PATTERN.matcher(result).replaceAll("o");
        result = ACCENTED_U_PATTERN.matcher(result).replaceAll("u");
        result = ACCENTED_Y_PATTERN.matcher(result).replaceAll("y");
        result = SCHARFES_S_PATTERN.matcher(result).replaceAll("ss");
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

        // Full-stops that occur in words, with no adjacent spaces are usually part
        // of an acronym.
        result = result.replaceAll("\\.", "");
            
        result = PUNCTUATION_PATTERN.matcher(result).replaceAll( " ");
        // An apostrophe or hyphen is only significant if it is between two letters
        // (i.e. there are no spaces on either side and it's not the first or last
        // character in the string).
        result = result.replaceAll("^'| '|' |'$|^-| -|- |-$", " ");
        // Strip possessive apostrophes.
        result = result.replaceAll("'s\\s|\u2019s\\s", " ");
        return result.trim();
    }
}
