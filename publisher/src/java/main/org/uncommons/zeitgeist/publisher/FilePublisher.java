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

import org.uncommons.zeitgeist.Topic;
import java.util.List;
import java.io.IOException;

/**
 * Interface implemented by objects that publish Zeitgeist topics to a file
 * (different implementations will generate different file types).
 * @author Daniel Dyer
 */
public interface FilePublisher
{
    void publish(List<Topic> topics,
                 String title,
                 int feedCount,
                 int articleCount) throws IOException;

}
