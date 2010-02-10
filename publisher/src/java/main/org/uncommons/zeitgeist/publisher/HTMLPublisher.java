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
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplate;
import java.util.List;
import java.util.Date;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Publish Zeitgeist topics as an HTML page.
 * @author Daniel Dyer
 */
class HTMLPublisher implements FilePublisher
{
    private static final String ENCODING = "UTF-8";
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("EEEE d MMMM yyyy / HH:mm z");

    public void publish(List<Topic> topics, String title, int feedCount, int articleCount) throws IOException
    {
        StringTemplateGroup group = new StringTemplateGroup("group");
        StringTemplate template = group.getInstanceOf("news");
        template.setAttribute("topics", topics);
        template.setAttribute("title", title);
        template.setAttribute("dateTime", DATE_FORMAT.format(new Date()));
        template.setAttribute("feedCount", feedCount);
        template.setAttribute("articleCount", articleCount);
        Writer writer = new OutputStreamWriter(new FileOutputStream("news.html"), ENCODING);
        try
        {
            writer.write(template.toString());
            writer.flush();
        }
        finally
        {
            writer.close();
        }
        copyClasspathResource(new File("."), "style.css", "style.css");
    }


    /**
     * Copy a single named resource from the classpath to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param resourcePath The path of the resource.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws IOException If the resource cannot be copied.
     */
    private void copyClasspathResource(File outputDirectory,
                                       String resourcePath,
                                       String targetFileName) throws IOException
    {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourcePath);
        copyStream(outputDirectory, resourceStream, targetFileName);
    }


    /**
     * Helper method to copy the contents of a stream to a file.
     * @param outputDirectory The directory in which the new file is created.
     * @param stream The stream to copy.
     * @param targetFileName The file to write the stream contents to.
     * @throws IOException If the stream cannot be copied.
     */
    private void copyStream(File outputDirectory,
                            InputStream stream,
                            String targetFileName) throws IOException
    {
        File resourceFile = new File(outputDirectory, targetFileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, ENCODING));
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resourceFile), ENCODING));
        try
        {

            String line = reader.readLine();
            while (line != null)
            {
                writer.write(line);
                writer.write('\n');
                line = reader.readLine();
            }
        }
        finally
        {
            reader.close();
            writer.close();
        }
    }
}
