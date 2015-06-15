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

public class Configuration {
    private String ontologyFile;
    private String packageGenerationParamsFile;
    private String packageFilenameIllegalCharacters;
    
    @Option(name="--ontology", aliases={"-o"}, usage="Sets an ontology file")
    public void setOntologyFile(String ontologyFile) {
        this.ontologyFile = ontologyFile;
    }
    
    public String getOntologyFile() {
        return ontologyFile;
    }
    
    @Option(name="--genparams", aliases={"-gp"}, usage="Sets the package generation parameters file")
    public void setPackageGenerationParamsFile(String packageGenerationParamsFile) {
        this.packageGenerationParamsFile = packageGenerationParamsFile;
    }
    
    public String getPackageGenerationParamsFile() {
        return packageGenerationParamsFile;
    }

    @Option(name="--illegalchars", aliases={"-x"}, usage="Sets the list of characters not allowed in package filenames")
    public void setPackageFilenameIllegalCharacters(String illegalChars) {
        this.packageFilenameIllegalCharacters = illegalChars;
    }

    public String getPackageFilenameIllegalCharacters() { return packageFilenameIllegalCharacters; }
}
