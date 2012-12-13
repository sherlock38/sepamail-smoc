package org.smoc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import org.smoc.exceptions.ConfigurationFileNotFoundException;
import org.smoc.exceptions.InvalidConfigurationException;

/**
 * The ConfigReader class reads and parses the SMOC module configuration file.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class ConfigReader {

    private File configFile;

    /**
     * ConfigReader class constructor
     * 
     * @param configFilename Path and name of SMOC configuration file
     * @throws ConfigurationFileNotFoundException
     */
    public ConfigReader(String configFilename) throws ConfigurationFileNotFoundException {

        // Initialise class attributes
        this.configFile = new File(configFilename);

        // Check if the configuration file exists
        if (!this.configFile.exists()) {

            // File does not exist so we throw the appropriate exception
            throw new ConfigurationFileNotFoundException(configFilename);
        }
    }

    /**
     * Parse the SMOC module
     * 
     * @return Dictionary of configuration file properties and assigned values
     * @throws InvalidConfigurationException
     * @throws IOException
     */
    public HashMap<String, String> parse() throws InvalidConfigurationException, IOException {

        // Configuration properties and assigned values
        HashMap<String, String> kvps = new HashMap<>();

        // Load list of required and optional properties for the SMOC configuration file
        Properties configDefinitionProperties = new Properties();
        configDefinitionProperties.load(ConfigReader.class.getResourceAsStream("smoc.properties"));

        // Array of required properties
        ArrayList<String> requiredProperties = 
                new ArrayList<>(Arrays.asList(configDefinitionProperties.getProperty("required").split(",")));

        // Array of optional properties
        ArrayList<String> optionalProperties = 
                new ArrayList<>(Arrays.asList(configDefinitionProperties.getProperty("optional").split(",")));

        // Load configuration file
        Properties configProperties = new Properties();
        configProperties.load(new FileInputStream(this.configFile));

        // Check contents of configuration file
        if (configProperties.isEmpty()) {

            // Configuration file is empty
            throw new InvalidConfigurationException();

        } else {

            // Iterate through the list of property keys and build a configuration settings map
            for (Iterator<String> it = configProperties.stringPropertyNames().iterator(); it.hasNext();) {

                // Get current key
                String currentKey = it.next();

                // Get the value of the key
                String value = configProperties.getProperty(currentKey);

                // Check value assigned to key
                if (value.length() > 0) {

                    // Add key value pair to configuration settings map
                    kvps.put(currentKey, value);

                    // Remove the key found from the list of required keys
                    if (requiredProperties.contains(currentKey)) {
                        requiredProperties.remove(currentKey);
                    }

                } else {

                    // Check if key value is mandatory
                    if (requiredProperties.contains(currentKey)) {

                        // Throw exception since required value has not been specified
                        throw new InvalidConfigurationException(currentKey, value);

                    } else if (optionalProperties.contains(currentKey)) {

                        // Remove the key from list of optional keys
                        optionalProperties.remove(currentKey);
                    }
                }
            }

            // Check if all required keys have been defined
            if (!requiredProperties.isEmpty()) {

                // Throw exception since all required keys have not been defined
                throw new InvalidConfigurationException(Arrays.toString(requiredProperties.toArray()));
            }
        }

        return kvps;
    }
}
