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
package org.uncommons.zeitgeist.publisher;

import org.antlr.stringtemplate.AttributeRenderer;

/**
 * StringTemplate renderer that escapes ampersands and angle brackets so
 * that values can be used in XML documents without problems.
 * @author Daniel Dyer
 */
class XMLStringRenderer implements AttributeRenderer
{
    public String toString(Object object)
    {
        return ((String) object).replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }


    public String toString(Object object, String formatName)
    {
        return toString(object);
    }
}
