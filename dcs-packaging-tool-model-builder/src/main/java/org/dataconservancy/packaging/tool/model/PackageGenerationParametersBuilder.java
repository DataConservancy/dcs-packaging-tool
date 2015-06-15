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
package org.dataconservancy.packaging.tool.model;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides methods to serialize and de-serialize PackageGenerationParameters objects to and from bytestreams.
 */
public interface PackageGenerationParametersBuilder {

    /**
     * Deserialize a suitable bytestream into a {@code PackageGenerationParameters} object
     * @param in the bytestream which contains the serialized PackageGenerationParameters.
     * @return A deserialized {@code PackageGenerationParameters} object.
     * @throws ParametersBuildException if the parameters can not be read from the input stream.
     */
    public PackageGenerationParameters buildParameters (InputStream in) throws ParametersBuildException;

    /**
     * Serialize a {@code PackageGenerationParameters} object into a bytestream.
     * @param params the PackageGenerationParameters
     * @param out   the  OutputStream
     * @throws ParametersBuildException if the parameters can not be saved to the output stream
     */
    public void buildParameters(PackageGenerationParameters params, OutputStream out) throws ParametersBuildException;

}

