// ============================================================================
//   Copyright 2009-2012 Daniel W. Dyer
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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link Topic} class.
 * @author Daniel Dyer
 */
public class TopicTest
{
    @Test
    public void testCountDistinctSources() throws MalformedURLException
    {
        Article article1 = new Article("1", "1", new URL("http://www.site1.com/1"), null, Collections.<Image>emptyList(), null, null, null);
        Article article2 = new Article("2", "2", new URL("http://www.site2.com/2"), null, Collections.<Image>emptyList(), null, null, null);
        Article article3 = new Article("3", "3", new URL("http://www.site2.com/3"), null, Collections.<Image>emptyList(), null, null, null);
        Article article4 = new Article("4", "4", new URL("http://www.site1.com/4"), null, Collections.<Image>emptyList(), null, null, null);
        Article article5 = new Article("5", "5", new URL("http://www.site3.com/5"), null, Collections.<Image>emptyList(), null, null, null);
        @SuppressWarnings("unchecked")
        List<WeightedItem<Article>> articles = Arrays.asList(new WeightedItem<Article>(5, article1),
                                                             new WeightedItem<Article>(4, article2),
                                                             new WeightedItem<Article>(3, article3),
                                                             new WeightedItem<Article>(2, article4),
                                                             new WeightedItem<Article>(1, article5));
        Topic topic = new Topic(articles);
        int sources = topic.countDistinctSources();
        assert sources == 3 : "Should be 3 distinct sources, is " + sources;
    }
}
