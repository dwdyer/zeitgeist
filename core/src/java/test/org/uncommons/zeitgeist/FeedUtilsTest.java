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

import org.testng.annotations.Test;

/**
 * Unit test for the {@link FeedUtils} class.
 * @author Daniel Dyer
 */
public class FeedUtilsTest
{
    @Test
    public void testStripMarkup()
    {
        String text = "<b>some bold text</b>";
        String stripped = FeedUtils.stripMarkUpAndPunctuation(text);
        assert stripped.equals("some bold text") : "Stripped text is wrong: " + stripped;
    }


    @Test
    public void testExpandEntities()
    {
        String text = "&pound;&amp;&quot;&#163;&uuml;";
        // Pounds signs (&pound; and &#163;) and 'u' with umlaut should be expanded to a plain u,
        // other entities should be ignored (replaced with whitespace).
        String stripped = FeedUtils.stripMarkUpAndPunctuation(text);
        assert stripped.equals("\u00A3 \u00A3u") : "Stripped text is wrong: " + stripped; 
    }


    @Test
    public void testStripPunctuation()
    {
        String text = "Hello, this is some text.";
        String stripped = FeedUtils.stripPunctuation(text);
        // Comma and full stop should be removed.
        assert stripped.equals("Hello this is some text") : "Stripped text is wrong: " + stripped;
    }


    @Test
    public void testApostrophes()
    {
        String text = "This is some 'text' that might include an Irish name like O'Neill";
        String stripped = FeedUtils.stripPunctuation(text);
        // Single quotes should be removed but not apostrophes in words.
        assert stripped.equals("This is some text that might include an Irish name like O'Neill")
               : "Stripped text is wrong: " + stripped;
    }


    @Test
    public void testStripLeadingAndTrailingSingleQuotes()
    {
        // These quotes aren't next to whitespace, but they should still be removed.
        String text = "'quotes'";
        String stripped = FeedUtils.stripMarkUpAndPunctuation(text);
        assert stripped.equals("quotes") : "Stripped text is wrong: " + stripped;
    }


    @Test
    public void testCommasInNumbers()
    {
        String text = "100,000";
        String stripped = FeedUtils.stripPunctuation(text);
        assert stripped.equals("100000") : "Stripped text is wrong: " + stripped;
    }


    @Test
    public void testAcronyms()
    {
        String text = "Some words about N.A.S.A. and the U.S.";
        String stripped = FeedUtils.stripPunctuation(text);
        assert stripped.equals("Some words about NASA and the US") : "Stripped text is wrong: " + stripped;
    }
}
