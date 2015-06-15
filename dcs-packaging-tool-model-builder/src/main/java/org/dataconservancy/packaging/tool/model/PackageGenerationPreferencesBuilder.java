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

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serializes {@code PackageGenerationPreferences}
 */
public interface PackageGenerationPreferencesBuilder {
    /**
     * <p>
     * Build a instance {@code PackageGenerationPreferences} from one or more preferences files. At the minimum,
     * a general preferences file, provided via the {@code InputStream in} will be loaded. After that if additional files
     * which contains format-specific package generation preferences are detected, they will also be loaded and
     * deserialized into the returned {@code PackageGenerationPreferences} object.
     *
     * </p>
     *
     * Method of additional file detection is determined by implementation.
     * @param  is the InputStream
     * @return  a {@code PackageGenerationPreferences} instance from one or more preferences files
     * @throws PreferencesBuildException if preferences can not be read from the input stream
     */
    public PackageGenerationPreferences buildPackageGenerationPreferences(InputStream is)
        throws PreferencesBuildException;

    /**
     * Serialize the general package generation preferences into the provided output stream.
     * @param preferences  {@code PackageGenerationPreferences}
     * @param os  the OutputStream
     */
    public void buildGeneralPreferences(PackageGenerationPreferences preferences, OutputStream os);

    /**
     * Serialize the package generation preferences related to the specified format into the provided output stream.
     * @param preferences  {@code PackageGenerationPreferences}
     * @param formatId  the format id
     * @param os the OutputStream
     */
    public void buildFormatSpecificPreferences(PackageGenerationPreferences preferences, String formatId,
                                               OutputStream os);
}
