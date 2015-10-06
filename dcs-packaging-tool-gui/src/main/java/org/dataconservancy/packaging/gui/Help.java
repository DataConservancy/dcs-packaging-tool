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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.packaging.gui.presenter.impl.PackageGenerationPresenterImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper to access bundled resource for errors.
 */
public class Help {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public enum HelpKey {

        PACKAGE_GENERATION_HELP("packagegeneration.help"), 
        CREATE_NEW_PACKAGE_HELP("createnewpackage.help"), 
        PACKAGE_DESCRIPTION_HELP("packagedescription.help");


        private String property;

        private HelpKey(String property) {
            this.property = property;
        }

        public String getProperty() {
            return property;
        }
    }

    private ResourceBundle bundle;

    public Help(ResourceBundle bundle) {
        this.bundle = bundle;

        for (HelpKey key : HelpKey.values()) {
            if (!bundle.containsKey(key.getProperty())) {
                throw new IllegalArgumentException("Missing resource in bundle: " + key.getProperty());
            }
        }
    }

    public String get(HelpKey key) {
        if (!bundle.containsKey(key.getProperty())) {
            log.error("Error getting help file. " + key.getProperty() + " resource does not exist.");

            throw new IllegalArgumentException("No such resource: " + key.getProperty());
        }
        
        return get(key.getProperty());
        
        
    }
    
    public String get(String property) {
        String helpText = "";

        if (!bundle.containsKey(property)) {
            log.error("Error getting help file. " + property + " resource does not exist.");

            throw new IllegalArgumentException("No such resource: " + property);
        }
        
        String filePath = bundle.getString(property);
        
        // Read the help text from the specified file. 
        File helpFile = null;
        
        try {
            if (filePath.startsWith("classpath:")) {
                String path = filePath.substring("classpath:".length());
                if(!path.startsWith("/")){
                    path = "/" + path;
                }
                InputStream fileStream = PackageGenerationPresenterImpl.class.getResourceAsStream(path);
                
                if (fileStream != null) { 
                    helpText = IOUtils.toString(fileStream, "UTF-8");
                }
            } else {
                helpFile = new File(filePath);
                
                if (helpFile != null) {
                    FileInputStream fis = new FileInputStream(helpFile);
                    helpText = IOUtils.toString(fis, "UTF-8");
                }
            }
        } catch (IOException e) {
            log.error("Error reading help file. " + e.getMessage());
        } 

        return helpText;
    }
}