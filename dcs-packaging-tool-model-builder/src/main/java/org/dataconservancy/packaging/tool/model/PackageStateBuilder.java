/*
 * Copyright 2015 Johns Hopkins University
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
 * This class is responsible for (de)serialize PackageState objects and other objects contained within them.
 */
public interface PackageStateBuilder {
    /**
     * Write a PackageState to an OutputStream.
     *
     * @param state
     *        {@link org.dataconservancy.packaging.tool.model.PackageState} to serialize.
     * @param stream
     *        The {@link java.io.OutputStream} to serialize to.
     * @throws PackageToolException if an error occurs reading the package state or serializing it to the output stream.
     */
    public void serialize(PackageState state, OutputStream stream) throws PackageToolException;

    /**
     * Reads a PackageState from an InputStream.
     *
     * @param stream
     *        InputStream to read from.
     * @throws PackageToolException if an error occurs reading the description from the stream.
     * @return The {@link org.dataconservancy.packaging.tool.model.PackageState} object read from the InputStream,
     * never null.
     */
    public PackageState deserialize(InputStream stream) throws PackageToolException;

}