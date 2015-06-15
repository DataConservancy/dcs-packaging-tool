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
 * Interface for serializing/deserializing {@link PackageDescription}s
 */
public interface PackageDescriptionBuilder {

    /**
     * Writes a PackageDescription to an OutputStream.
     * 
     * @param description
     *        {@link PackageDescription} to serialize.
     * @param stream
     *        The {@link OutputStream} to serialize to.
     * @throws PackageToolException if an error occurs reading the description or serializing it to the output stream.
     */
    public void serialize(PackageDescription description, OutputStream stream)
            throws PackageToolException;

    /**
     * Reads a PackageDescription from an InputStream.
     * 
     * @param stream
     *        InputStream to read from.
     * @throws PackageToolException if an error occurs reading the description from the stream.
     * @return The PackageDescription object read from the InputStream, never null.
     */
    public PackageDescription deserialize(InputStream stream) throws PackageToolException;
}
