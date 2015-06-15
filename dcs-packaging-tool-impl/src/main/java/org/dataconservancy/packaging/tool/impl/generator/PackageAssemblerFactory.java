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

package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

import java.util.Map;

/**
 * Provides instances of {@link PackageAssembler} used for assembling individual
 * packages.
 */
public class PackageAssemblerFactory {

    private static Map<String, Class<? extends PackageAssembler>> assemblers;

    /**
     * Provide an instance of a {@link PackageAssembler} for assembling a single
     * package.
     * <p>
     * The provided params may influence the behaviour and form of the resulting
     * package content produced by the assembler.
     * </p>
     * 
     * @param params
     *        Package Generation Parameters.
     * @return PackageAssembler ready for adding package resources to.
     * @throws IllegalAccessException if the parameter list doesn't contain a package format id
     * @throws InstantiationException if there are no assemblers are set on the factory
     */
    public static PackageAssembler newAssembler(PackageGenerationParameters params) throws IllegalAccessException, InstantiationException {
        if (assemblers == null || assemblers.size() == 0) {
            throw new IllegalStateException("No assemblers have been set.");
        }

        String formatId = null;
        if (params.getKeys().contains(GeneralParameterNames.PACKAGE_FORMAT_ID)) {
            formatId = params.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID, 0);
        }
        if (formatId == null) {
            throw new IllegalArgumentException("The parameter list must contain a package format id");
        }

        for (String assemblerId : assemblers.keySet()) {
            if (assemblerId.equals(formatId)) {
                Class<? extends PackageAssembler> assemblerClass = assemblers.get(assemblerId);
                PackageAssembler assembler = assemblerClass.newInstance();
                assembler.init(params);
                return assembler;
            }
        }

        return null;
    }


    public static void setAssemblers(Map<String, Class<? extends PackageAssembler>> assemblers) {
        if (assemblers == null || assemblers.size() == 0) {
            throw new IllegalArgumentException("At least one assembler must be provided");
        }

        PackageAssemblerFactory.assemblers = assemblers;
    }
}
