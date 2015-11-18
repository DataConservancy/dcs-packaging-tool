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

import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageState;

import java.util.Map;

/**
 * Provides instances of {@link PackageModelBuilder} for instantiating package
 * models.
 */
public class PackageModelBuilderFactory {

    private static Map<String, PackageModelBuilder> builders;

    /**
     * Provides an instance of {@link PackageModelBuilder} for assembling
     * packages.
     *
     * @param desc
     *        The package state to use for selecting the correct builder
     * @param params
     *        Package Generation Parameters
     * @return Fully configured PackageModelBulder.
     * @throws IllegalAccessException if no builders have been set on the factory
     * @throws InstantiationException if the params don't contain the package format id
     */
    public static PackageModelBuilder newBuilder(PackageState desc, PackageGenerationParameters params)
            throws IllegalAccessException, InstantiationException {

        if (builders == null || builders.size() == 0) {
            throw new IllegalStateException("No builders have been set.");
        }

        String formatId = null;
        if (params.getKeys().contains(GeneralParameterNames.PACKAGE_FORMAT_ID)) {
            formatId = params.getParam(GeneralParameterNames.PACKAGE_FORMAT_ID, 0);
        }
        if (formatId == null) {
            throw new IllegalArgumentException("The parameter list must contain a package format id");
        }

        if (builders.containsKey(formatId)) {
            return builders.get(formatId).newInstance(params);
        }

        return null;
    }

    public static void setBuilders(Map<String, PackageModelBuilder> builders) {
        if (builders == null || builders.size() == 0) {
            throw new IllegalArgumentException("At least one builder must be provided");
        }

        PackageModelBuilderFactory.builders = builders;
    }

}
