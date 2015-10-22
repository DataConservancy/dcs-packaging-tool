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
 * Existence of command line values for configuration files is checked in the App class - this class is consulted if the
 * command line parameter is absent.
 */
public class Configuration {

    private String userConfDirectory = System.getProperty("user.home") + File.separator + ".dataconservancy";
    private String ontologyFile;
    private String packageFilenameIllegalCharacters;;

    //the default application configuration directory
    private String configurationDirectory;

    //file names to be used for application default files or
    //files placed in the userConfigDirectory
    //these are set in the config_default.properties file
    private String disciplineMapFile;
    private String availableProjectsFile;
    private String packageGenerationParametersFile;
    private String packageMetadataParametersFile;

    //the "resolved" configuration files - precedence is
    //command line, then user config directory, and finally the application default
    private String disciplineMap;
    private String availableProjects;
    private String packageGenerationParameters;
    private String packageMetadataParameters;

    @Option(name="--ontology", aliases={"-o"}, usage="Sets an ontology file")
    public void setOntologyFile(String ontologyFile) {
        this.ontologyFile = ontologyFile;
    }
    
    public String getOntologyFile() {
        return ontologyFile;
    }

    @Option(name="--illegalchars", aliases={"-x"}, usage="Sets the list of characters not allowed in package filenames")
    public void setPackageFilenameIllegalCharacters(String illegalChars) {
        this.packageFilenameIllegalCharacters = illegalChars;
    }

    public String getPackageFilenameIllegalCharacters() { return packageFilenameIllegalCharacters; }

    @Option(name="--external-projects", aliases={"-xp"}, usage="Sets the external project identifiers file")
    public void setAvailableProjects(String availableProjects){
        this.availableProjects = availableProjects;
    }

    public String getAvailableProjects(){
        return availableProjects;
    }

    @Option(name="--disciplines", aliases={"-d"}, usage="Sets the discipline map xml file")
    public void setDisciplineMap(String disciplineMap){
        this.disciplineMap = disciplineMap;
    }

    public String getDisciplineMap() {
        return disciplineMap;
    }

    @Option(name="--generation-params", aliases={"-p"}, usage="Sets the package generation parameters file")
    public void setPackageGenerationParameters(String packageGenerationParameters){
        this.packageGenerationParameters = packageGenerationParameters;
    }

    public String getPackageGenerationParameters(){
        return packageGenerationParameters;
    }

    public void setPackageMetadataParameters(String packageMetadataParameters){
        this.packageMetadataParameters = packageMetadataParameters;
    }

    public String getPackageMetadataParameters(){return packageMetadataParameters; }

    public void setConfigurationDirectory(String dir){
        this.configurationDirectory = dir;
    }

    public String getConfigurationDirectory(){
        return configurationDirectory;
    }

    public void setPackageGenerationParametersFile(String packageGenerationParametersFile) {
        this.packageGenerationParametersFile = packageGenerationParametersFile;
    }

    public String getPackageGenerationParametersFile() {
        return packageGenerationParametersFile;
    }

    public void setDisciplineMapFile(String disciplineMapFile){
        this.disciplineMapFile = disciplineMapFile;
    }

    public String getDisciplineMapFile(){
        return disciplineMapFile;
    }

    public void setAvailableProjectsFile(String availableProjectsFile){
        this.availableProjectsFile= availableProjectsFile;
    }

    public String getAvailableProjectsFile(){
        return availableProjectsFile;
    }

    public void setPackageMetadataParametersFile(String packageMetadataParametersFile){
        this.packageMetadataParametersFile = packageMetadataParametersFile;
    }

    public String getPackageMetadataParametersFile(){
        return packageMetadataParametersFile;
    }

    /**
     *  This method locates the default configuration with the supplied file name, in the user's configuration directory.
     *  This directory is hard-coded in the userConfDirectory field above.
     * @param fileName the name of the file to be found. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     * @return the user's local configuration path
     */
    private String locateUserConfigFile(String fileName) {
        File confFile = new File(userConfDirectory, fileName);
        if (confFile.exists()) {
            return (confFile.getPath());
        } else {
            return null;
        }
    }

    /**
     * This method locates the default configuration with the supplied file name, in the configuration directory.
     * The configuration directory is specified in the  config_defaults.properties file.
     * @param fileName the name of the file to be found. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     * @return the default configuration path
     */
    public String locateDefaultConfigFile(String fileName) {
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
     * @param configurationFileName The name of the configuration file to be resolved. These names are specified in the
     *                              config_defaults.properties file and set on the  *File fields.
     *
     * @return  the resolved configuration.file path
     */
    public String resolveConfigurationFile(String configurationFileName){
        String userFile = locateUserConfigFile(configurationFileName);
        String defaultFile = locateDefaultConfigFile(configurationFileName);
        return (userFile == null ? defaultFile : userFile);
    }

    /**
     * This method gets the InputStream associated with the provided file path string, which
     * may be either a classpath resource or a filesystem path.
     * @param filePath
     * @return
     * @throws IOException
     */
    public InputStream getConfigurationFileInputStream(String filePath) throws IOException{
        InputStream fileStream = null;

        if (filePath.startsWith("classpath:")) {
            String path = filePath.substring("classpath:".length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            fileStream = this.getClass().getResourceAsStream(path);
            //this also works:
            // URL url = this.getClass().getResource(path);
            //fileStream = url.openStream();
        } else {
            fileStream = new FileInputStream(filePath);
        }

        return fileStream;
    }

}
