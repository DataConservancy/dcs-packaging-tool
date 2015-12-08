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

package org.dataconservancy.packaging.tool.impl.generator.mocks;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;


/**
 * A sample Assembler for use in testing the factory (spring wiring).  It does nothing, but needs to exist
 */
public class MockPackageAssembler implements PackageAssembler {
    @Override
    public void init(PackageGenerationParameters params, Map<String, List<String>> metadata) {
    }

    @Override
    public URI reserveResource(String path, PackageResourceType type) {
        return null;
    }
    
    @Override
    public URI reserveDirectory(String path, PackageResourceType type) {
        return null;
    }
    

    @Override
    public void putResource(URI uri, InputStream content) {
    }

    @Override
    public URI createResource(String path, PackageResourceType type, InputStream content) {
        return null;
    }

    @Override
    public org.dataconservancy.packaging.tool.api.Package assemblePackage() {
        return null;
    }

    @Override
    public void addParameter(String key, String value) {
    }
}
