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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.antlr.stringtemplate.AttributeRenderer;

/**
 * A StringTemplate renderer that uses the format specification provided by the template.
 * @author Daniel Dyer
 */
class DateRenderer implements AttributeRenderer
{
    public String toString(Object object)
    {
        return object.toString();
    }


    public String toString(Object object, String formatString)
    {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        return format.format((Date) object);
    }
}
