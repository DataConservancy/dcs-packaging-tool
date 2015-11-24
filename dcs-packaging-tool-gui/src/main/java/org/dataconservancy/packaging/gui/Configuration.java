/*
 * Copyright 2014 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.packaging.gui;

import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class is responsible for getters and setters for various configuration parameters, as well as path resolution for
 * configuration files. Parameters which may be specified on the command line are handled here. When configuration files
 * may be obtained from the command line, the user's configuration directory or the default configuration location in the
 * jar file, we resolve the file location in that order: if the command line option exists we set the configuration file
 * location to the value given to the command line parameter; if not we look in the user's configuration directory
 * ${user.home}/.dataconservancy for the file; if that does not exist we get the default configuration file. The file in
 * the user's configuration directory must have the file name defined in the config_defaults.properties file for that parameter.
 *
 * Existence of command line values for configuration files is checked in the App class.
 */
public class Configuration {

    private static String userConfDirectory = System.getProperty("user.home") + File.separator + ".dataconservancy";

    //the default application configuration directory
    private static String configurationDirectory;

    //file names to be used for application default files or
    //files placed in the userConfigDirectory
    //these are set in the config_default.properties file
    private static String disciplineMapFile;
    private static String packageGenerationParametersFile;
    private static String packageMetadataParametersFile;
    private static String userPropertiesFile;

    //command line supplied files - precedence is
    //command line, then user config directory, and finally the application default
    private static String disciplineMap;
    private static String packageGenerationParameters;
    private static String packageMetadataParameters;
    private static String userProperties;

    public enum ConfigFile{
        DISCIPLINE_MAP{
            //the filename set in the config_default.properties file
            public String fileName(){
                return disciplineMapFile;
            }
            //the command line file path, if set
            public String commandLinePath(){
                return disciplineMap;
            }
        },
        PKG_GEN_PARAMS{
            //the filename set in the config_default.properties file
            public String fileName(){
                return packageGenerationParametersFile;
            }
             //the command line file path, if set
            public String commandLinePath(){
                return packageGenerationParameters;
            }
        },
        PKG_METADATA_PARAMS{
            //the filename set in the config_default.properties file
            public String fileName(){ return packageMetadataParametersFile; }
             //the command line file path, if set
            public String commandLinePath(){
                return packageMetadataParameters;
            }
        },
        USER_PROPS{
            //the filename set in the config_default.properties file
            public String fileName(){
                return userPropertiesFile;
            }
             //the command line file path, if set
            public String commandLinePath(){
                return userProperties;
            }
        };
       abstract String commandLinePath();
       abstract String fileName();
    }

    public static void setConfigurationDirectory(String dir){
        configurationDirectory = dir;
    }

    //Setters for the names of the files
    public static void setDisciplineMapFile(String file){
        disciplineMapFile = file;
    }

    public void setPackageGenerationParametersFile(String file) {
        packageGenerationParametersFile = file;
    }

    public void setPackageMetadataParametersFile(String file){
        packageMetadataParametersFile = file;
    }

    public void setUserPropertiesFile(String file){
        userPropertiesFile = file;
    }

    //Setters for the actual file locations we will be using

    @Option(name="--disciplines", aliases={"-d"}, usage="Sets the discipline map xml file")
    public void setDisciplineMap(String file){
        disciplineMap = file;
    }

    @Option(name="--generation-params", aliases={"-p"}, usage="Sets the package generation parameters file")
    public void setPackageGenerationParameters(String file){
        packageGenerationParameters = file;
    }

    @Option(name="--metadata-params", aliases={"-m"}, usage="Sets the package metadata parameters file")
    public void setPackageMetadataParameters(String file){
        packageMetadataParameters = file;
    }

    @Option(name="--user-props", aliases={"-u"}, usage ="Sets the user defined properties file")
    public void setUserProperties(String file){
        userProperties = file;
    }

    /**
     *  This method locates the default configuration with the supplied file name, in the user's configuration directory.
     *  This directory is hard-coded in the userConfDirectory field above.
     * @param fileName the name of the file to be found. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     * @return the user's local configuration path
     */
    private static String locateUserConfigFile(String fileName) {
        File confFile = new File(userConfDirectory, fileName);
        if (confFile.exists()) {
            return (confFile.getPath());
        } else {
            return null;
        }
    }

    /**
     * This method locates the default configuration with the supplied file name, in the configuration directory.
     * The configuration directory is specified in the config_defaults.properties file.
     * @param fileName the name of the file to be found. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     * @return the default configuration path
     */
    private static String locateDefaultConfigFile(String fileName) {
        if (configurationDirectory.startsWith("classpath:")) {
            if (configurationDirectory.endsWith("/")) {
                return configurationDirectory + fileName;
            } else {
                return configurationDirectory + "/" + fileName;
            }
        } else {
            File file = new File(configurationDirectory, fileName);
            return file.getPath();
        }
    }

    /**
     *  This method looks for files with this name in the user's config directory and in the default configuration directory.
     *  The method returns the user's file if found, else it returns the default file.
     *
     * @param configFile The ConfigFile representing the file to be resolved. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     *
     * @return  the resolved configuration.file path
     */
    public static String resolveConfigurationFile(ConfigFile configFile){
        //look for a file name specified on the command line
        if(configFile.commandLinePath() != null) {
            return configFile.commandLinePath();
        } else {
            //if that fails, keep going
            String fileName = configFile.fileName();
            String userFile = locateUserConfigFile(fileName);
            String defaultFile = locateDefaultConfigFile(fileName);
            return (userFile == null ? defaultFile : userFile);
        }
    }

    /**
     * This method gets the InputStream associated with the provided file path string, which
     * may be either a classpath resource or a filesystem path.
     * @param configFile the ConfigFile representing the configuration file
     * @return the InputStream associated with the file path
     * @throws IOException if there was an error obtaining the InputStream
     */
    public static InputStream getConfigurationFileInputStream(ConfigFile configFile) throws IOException {
        InputStream fileStream;
        String filePath = resolveConfigurationFile(configFile);
        if (filePath.startsWith("classpath:")) {
            String path = filePath.substring("classpath:".length());
            fileStream = Configuration.class.getResourceAsStream(path);
            //this also works:
            //URL url = Configuration.class.getResource(path);
            //fileStream = url.openStream();
        } else {
            fileStream = new FileInputStream(filePath);
        }
        if (fileStream == null){
                throw new IOException("Could not open configuration file " + filePath);
        }
        return fileStream;
    }

}
