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

import java.util.Comparator;
import java.io.Serializable;

/**
 * Comparator used for sorting topics in order of the number of articles that make up
 * the topic.
 * @author Daniel Dyer
 */
class TopicArticleCountComparator implements Comparator<Topic>, Serializable
{
    public int compare(Topic o1, Topic o2)
    {
        return o1.getArticles().size() - o2.getArticles().size();
    }
}
