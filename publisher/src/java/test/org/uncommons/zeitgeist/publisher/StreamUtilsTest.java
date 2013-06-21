package org.uncommons.zeitgeist.publisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/**
 * Unit test for the {@link StreamUtils} class.
 * @author Daniel Dyer
 */
public class StreamUtilsTest
{
    @Test
    public void testCopyStream() throws IOException
    {
        final byte[] data = {1, 2, 3, 4};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StreamUtils.copyStream(inputStream, outputStream);
        byte[] copy = outputStream.toByteArray();
        assert Arrays.equals(data, copy) : "Copied data differs from original: " + Arrays.toString(copy);
    }


    @Test(dependsOnMethods = "testCopyStream")
    public void testCopyStreamToFile() throws IOException
    {
        final byte[] data = {1, 2, 3, 4};
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        File tempFile = File.createTempFile("temp", null);
        try
        {
            StreamUtils.copyStreamToFile(inputStream, tempFile);
            assert tempFile.exists() : "File does not exist";
            assert tempFile.length() == 4 : "Output file is wrong length: " + tempFile.length();
        }
        finally
        {
            tempFile.delete();
        }
    }


    @Test(dependsOnMethods = "testCopyStreamToFile")
    public void testCopyFile() throws IOException
    {
        // Create temporary file to be copied.
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});
        File tempFile = File.createTempFile("temp", null);
        StreamUtils.copyStreamToFile(inputStream, tempFile);

        // Copy file to temp dir.
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File newFile = new File(tempDir, "newFileName.tmp");
        try
        {
            StreamUtils.copyFile(tempDir, tempFile, "newFileName.tmp");
            assert newFile.exists() : "File does not exist";
            assert newFile.length() == 4 : "Copied file is wrong length: " + newFile.length();
        }
        finally
        {
            newFile.delete();
            tempFile.delete();
        }
    }


    @Test(dependsOnMethods = "testCopyStreamToFile")
    public void testCopyClasspathResource() throws IOException
    {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File newFile = new File(tempDir, "newFileName.tmp");
        try
        {
            StreamUtils.copyClasspathResource(tempDir,
                                              "org/uncommons/zeitgeist/low-value-words.txt",
                                              "newFileName.tmp");
            assert newFile.exists() : "File does not exist";
            assert newFile.length() > 0 : "Copied file is empty.";
        }
        finally
        {
            newFile.delete();
        }
    }
}
