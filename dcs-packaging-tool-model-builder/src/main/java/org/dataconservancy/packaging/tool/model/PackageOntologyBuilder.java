/*
 * Copyright 2015 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serialize and deserialize PackageOntology objects.
 */
public interface PackageOntologyBuilder {

    /**
     * Deserialize a suitable bytestream into a {@code PackageOntology} object
     * @param in the bytestream which contains the serialized PackageOntology.
     * @return A deserialized {@code PackageOntology} object.
     */
    public PackageOntology buildOntology(InputStream in);

    /**
     * Serialize a {@code PackageOntology} object into a bytestream.
     * @param ontology the PackageOntology
     * @param out the OutputStream
     */
    public void buildOntology(PackageOntology ontology, OutputStream out);
}
