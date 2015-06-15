/*
 * Copyright 2013 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.api.generator;

import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

/**
 * Creates resources to be packaged in accordance with some packaging model.
 * <p>
 * Takes a {@link PackageDescription}, transforms it into some other packaging
 * model, instantiates it into physical resources by serializing and invoking
 * {@link PackageAssembler}
 * </p>
 */
public interface PackageModelBuilder {

    /**
     * Initialize a builder based on the specified parameters.
     * @param params The parameters used to initialize a specific instance of the builder object
     */
    public void init(PackageGenerationParameters params);

    /**
     * Create the resources that instantiate a package in a particular model.
     * <p>
     * At the end of building the package according to a particular model, the
     * PackageModelbuilder will have added one or more resources to the
     * PackageAssembler, making the package ready for final assembly via
     * {@link PackageAssembler#assemblePackage()}.
     * </p>
     * 
     * @param desc
     *        A valid Package Description.
     * @param assembler
     *        The assembler in which packaged resources will be created.
     */
    public void buildModel(PackageDescription desc, PackageAssembler assembler);
}
