package org.smoc.test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import org.smoc.Smoc;
import org.smoc.exceptions.ConfigurationFileNotFoundException;
import org.smoc.exceptions.InvalidConfigurationException;

/**
 * The SmocTest class is a command line application that is used to test the functionalities of the SMOC module
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class SmocTest {

    /**
     * SMOC module test application entry point
     * 
     * @param args Command line arguments
     * @return void
     */
    public static void main(String [] args) {

        // Smoc class instance
        Smoc smoc;

        try {

            // Configuration file
            String conf = getCurrentWorkingDirectory() + System.getProperty("file.separator") + "conf" +
                    System.getProperty("file.separator") + "smoc.properties";

            // Check parameter count
            if (args.length == 1) {

                // Smoc class initialisation
                smoc = new Smoc(conf);

                // Send missive file by email
                smoc.sendMissive("Test missive enveloppe SMIME", args[0]);

            } else {

                // Display application usage
                showUsage();
            }

        } catch (ConfigurationFileNotFoundException ex) {

            System.out.println(ex.getMessage());

        } catch (IOException | InvalidConfigurationException ex) {

            System.out.println(ex.getMessage());

        } catch (Exception ex) {

            System.out.println(ex.getMessage());
        }
    }

    /**
     * Get the current working directory of the application
     *
     * @return The absolute path to the application working directory
     */
    public static String getCurrentWorkingDirectory() {

        String path;

        // Current class directory
        URL location = SmocTest.class.getProtectionDomain().getCodeSource().getLocation();

        try {
            path = URLDecoder.decode(location.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            path = location.getPath();
        }

        // Absolute current working directory
        return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + ".";

        // Absolute current working directory - added for debugging
        //return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + "./..";
    }

    /**
     * Display application usage
     */
    private static void showUsage() {

        System.out.println("java -jar SMOCTest.jar <fichier à convertir>");
    }
}
