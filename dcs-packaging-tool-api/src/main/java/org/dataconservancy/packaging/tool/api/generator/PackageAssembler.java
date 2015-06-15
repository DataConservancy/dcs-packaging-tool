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

import org.dataconservancy.packaging.tool.api.*;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

import java.io.InputStream;

import java.net.URI;

/**
 * Assembles some packaging model into physical form.
 * <p>
 * The package assembler allows for the creation of named resources within a
 * logical package, and the ultimate assembly of these resources into a final,
 * physical packaged form.
 * <p>
 */
public interface PackageAssembler {

    /**
     * initializes the assembler using the specified parameters, and whatever assembler-specific
     * work needs to be done.
     * @param params The parameters object containing whatever is needed for the assembler
     *               to function properly.
     */
    void init(PackageGenerationParameters params);

    /**
     * Reserve (create a name, and potentially storage location for) a resource.
     * <p>
     * Given a path and resource type, this method reserves a resource name
     * (URI) for future population of content via
     * {@link #putResource(URI, InputStream)}. The package content is assumed to
     * be empty (0 bytes) until content is provided.
     * </p>                                                     z
     * <p>
     * Note: This operation is optional. If an assembler does not support
     * reserving URIs and updating content, then it shall throw and
     * {@link UnsupportedOperationException}
     * </p>
     *
     * @param path
     *        Logical file path (including filename) of the resource relative to
     *        the package.
     * @param type
     *        Resource type (e.g. data, metadata, etc).
     */
    URI reserveResource(String path, PackageResourceType type);

    /**
     * Commit new content to a previously created or reserved resource.
     * <p>
     * Using a URI obtained from previously reserving a resource (via
     * {@link #reserveResource(String, PackageResourceType)}), this allows the
     * content of the resource named by the URI to be set.
     * </p>
     * <p>
     * Use of URIs not obtained through
     * {@link #reserveResource(String, PackageResourceType)} is undefined.
     * </p>
     * <p>
     * Note: This operation is optional. If an assembler does not support
     * reserving URIs and updating content, then it shall throw and
     * {@link UnsupportedOperationException}
     * </p>
     * 
     * @param uri
     *        URI naming the resource whose content is to be set.
     * @param content
     *        InputStream containing the content of the resource.
     **/
    void putResource(URI uri, InputStream content);

    /**
     * Create a new resource in the package.
     * <p>
     * Given a relative file path and some content, this method will add the
     * content to the package, and return a URI that names it.
     * </p>
     * 
     * @param path
     *        Logical file path (including filename) of the resource relative to
     *        the package.
     * @param type
     *        Resource type (e.g. data, metadata, etc).
     * @param content
     *        InputStream containing the content of the resource.
     */
    URI createResource(String path,
                       PackageResourceType type,
                       InputStream content);

    /**
     * Produce a {@code Package} object based on the added resources.
     * <p>
     * Package's content is assembled from all previously created resources into final physical packaging
     * form contained in an InputStream.
     * </p>
     * <p>
     * Package's contentType is set based on the archiving and compression algorithms applied on the content.
     * </p>
     * <p>
     * Package's name is set based on the input parameter "package-name".
     * </p>
     * <p>
     * Note: Invoking this method more than once is undefined. It can be assumed
     * that this method may imply some sort of finalization or cleanup of
     * resources used in building the package.
     * </p>
     */
    public org.dataconservancy.packaging.tool.api.Package assemblePackage();


    /**
     * Add a parameter to configure assembler information after initialization.  Useful
     * for times when a parameter may not be available until work is done later, but
     * is needed prior to calling assemblePackage, such as putting information into an
     * information file that isn't determined until the data files are written.
     * @param key The name of the parameter to add
     * @param value The value of the parameter to add
     */
    public void addParameter(String key, String value);
}
