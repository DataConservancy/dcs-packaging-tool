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

public class Configuration {

    private String userConfDirectory = System.getProperty("user.home") + File.separator + ".dataconservancy";
    private String ontologyFile;
    private String packageFilenameIllegalCharacters;

    //the default application configuration directory
    private String configurationDirectory;

    //file names to be used for application default files or
    //files placed in the userConfigDirectory
    //these are set in the config_default.properties file
    private String disciplineMapFile;
    private String availableProjectsFile;
    private String packageGenerationParametersFile;

    //the "resolved" configuration files - precedence is
    //command line, then user config directory, and finally the application default
    private String disciplineMap;
    private String availableProjects;
    private String packageGenerationParameters;

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

    private String locateUserConfigFile(String fileName) {
        File confFile = new File(userConfDirectory, fileName);
        if (confFile.exists()) {
            return (confFile.getPath());
        } else {
            return null;
        }
    }

    private String locateDefaultConfigFile(String fileName) {
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

    public String resolveConfigurationFile(String configurationFileName){
        String userFile = locateUserConfigFile(configurationFileName);
        String defaultFile = locateDefaultConfigFile(configurationFileName);
        return (userFile == null ? defaultFile : userFile);
    }

}
