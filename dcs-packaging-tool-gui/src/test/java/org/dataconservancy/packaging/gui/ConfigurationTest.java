package org.dataconservancy.packaging.gui;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test should be ignored unless that class is being worked on actively, since unexpected results may happen
 * if config files are or are not present in the user's home directory. we can looks at this later.
 *
 * .
 */
@Ignore
public class ConfigurationTest {

    private Configuration underTest;
    //these values are from config_default_test.properties, need to be kept consistent
    private String configurationDir = "classpath:/";
    private String disciplineMapFile = "discipline_map.xml";
    private String packageGenerationParametersFile = "packageGenerationParameters";
    private String packageMetadataParametersFile = "packageMetadataParameters-test.xml";
    private String userPropertiesFile = "userProperties.json";

    private String commandLineDisciplineMap = "commandLineDisciplineMap";
    private String commandLinePkgGenParams = "commandLinePackageGenerationParameters";
    private String commandLinePkgMetadataParams = "commandLinePackageGenerationParameters";
    private String commandLineUserProps = "commandLineUserProperties";

    @Before
    public void setup() {
        underTest = new Configuration();
        underTest.setConfigurationDirectory(configurationDir);
        underTest.setDisciplineMapFile(disciplineMapFile);
        underTest.setPackageGenerationParametersFile(packageGenerationParametersFile);
        underTest.setPackageMetadataParametersFile(packageMetadataParametersFile);
        underTest.setUserPropertiesFile(userPropertiesFile);
    }

    @Test
    public void testConfigurationsResolved() {
        Assert.assertEquals(configurationDir + disciplineMapFile, underTest.resolveConfigurationFile(Configuration.ConfigFile.DISCIPLINE_MAP));
        Assert.assertEquals(configurationDir + packageGenerationParametersFile, underTest.resolveConfigurationFile(Configuration.ConfigFile.PKG_GEN_PARAMS));
        Assert.assertEquals(configurationDir + packageMetadataParametersFile, underTest.resolveConfigurationFile(Configuration.ConfigFile.PKG_METADATA_PARAMS));
        Assert.assertEquals(configurationDir + userPropertiesFile, underTest.resolveConfigurationFile(Configuration.ConfigFile.USER_PROPS));

        //now see if resolution happens with command line files specified
        underTest.setDisciplineMap(commandLineDisciplineMap);
        underTest.setPackageGenerationParameters(commandLinePkgGenParams);
        underTest.setPackageMetadataParameters(commandLinePkgMetadataParams);
        underTest.setUserProperties(commandLineUserProps);

        Assert.assertEquals(commandLineDisciplineMap, underTest.resolveConfigurationFile(Configuration.ConfigFile.DISCIPLINE_MAP));
        Assert.assertEquals(commandLinePkgGenParams, underTest.resolveConfigurationFile(Configuration.ConfigFile.PKG_GEN_PARAMS));
        Assert.assertEquals(commandLinePkgMetadataParams, underTest.resolveConfigurationFile(Configuration.ConfigFile.PKG_METADATA_PARAMS));
        Assert.assertEquals(commandLineUserProps, underTest.resolveConfigurationFile(Configuration.ConfigFile.USER_PROPS));
    }

    @After
    public void teardown(){
        underTest.setDisciplineMap(null);
        underTest.setPackageGenerationParameters(null);
        underTest.setPackageMetadataParameters(null);
        underTest.setUserProperties(null);
    }
}
