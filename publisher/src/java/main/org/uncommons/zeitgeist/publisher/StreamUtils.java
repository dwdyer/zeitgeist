package org.uncommons.zeitgeist.publisher;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods for working with stream and file resources.
 */
final class StreamUtils
{
    private StreamUtils()
    {
        // Prevents instantiation of utility class.
    }


    /**
     * Copy a single named resource from the classpath to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param resourcePath The path of the resource.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws java.io.IOException If the resource cannot be copied.
     */
    static void copyClasspathResource(File outputDirectory,
                                      String resourcePath,
                                      String targetFileName) throws IOException
    {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(resourcePath);
        copyStreamToFile(resourceStream, new File(outputDirectory, targetFileName));
    }


    /**
     * Copy a single named file to the output directory.
     * @param outputDirectory The destination directory for the copied resource.
     * @param sourceFile The path of the file.
     * @param targetFileName The name of the file created in {@literal outputDirectory}.
     * @throws java.io.IOException If the file cannot be copied.
     */
    static void copyFile(File outputDirectory,
                         File sourceFile,
                         String targetFileName) throws IOException
    {
        FileInputStream inputStream = new FileInputStream(sourceFile);
        try
        {
            copyStreamToFile(inputStream, new File(outputDirectory, targetFileName));
        }
        finally
        {
            inputStream.close();
        }
    }


    /**
     * Helper method to copy the contents of a stream to a file.
     * @param stream The stream to copy.
     * @param target The target file to write the stream contents to.
     * @throws java.io.IOException If the stream cannot be copied.
     */
    static void copyStreamToFile(InputStream stream, File target) throws IOException
    {
        FileOutputStream outputStream = new FileOutputStream(target);
        try
        {
            copyStream(stream, outputStream);
        }
        finally
        {
            outputStream.close();
        }
    }


    /**
     * Helper method to copy the contents of a stream.
     * @param stream The stream to copy.
     * @param target The target stream to write the stream contents to.
     * @throws java.io.IOException If the stream cannot be copied.
     */
    static void copyStream(InputStream stream,
                           OutputStream target) throws IOException
    {
        BufferedInputStream input = new BufferedInputStream(stream);
        try
        {
            BufferedOutputStream output = new BufferedOutputStream(target);
            try
            {
                int i = input.read();
                while (i != -1)
                {
                    output.write(i);
                    i = input.read();
                }
                output.flush();
            }
            finally
            {
                output.close();
            }
        }
        finally
        {
            input.close();
        }
    }
}
