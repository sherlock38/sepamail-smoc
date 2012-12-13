package org.smoc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.io.IOUtils;

/**
 * FileUtils is a class which groups static methods pertaining file operations used throughout the SMOC module.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class SmocFileUtils {

    /**
     * Read the content of the specified file to string
     * 
     * @param filename Path and name of file that needs to be read
     * @return Content of file as string
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static String readFile(String filename) throws FileNotFoundException, IOException {

        String content = null;
        FileInputStream fis;

        // File object instance
        File file = new File(filename);

        // Check if file exists
        if (file.exists()) {

            // File input stream
            fis = new FileInputStream(file);

            // File contents as byte array
            content = IOUtils.toString(fis, "UTF-8");

            // Close the file stream
            fis.close();

        } else {

            // File could not be found
            throw new FileNotFoundException();
        }
        
        return content;
    }
}
