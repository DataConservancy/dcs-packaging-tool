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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

public class FunctionalAssemblerMock
        implements PackageAssembler {

    private Map<URI, File> files = new HashMap<URI, File>();
    
    private String pathPart = "FunctionalAssemblerMock/";

    final File dir;


    public FunctionalAssemblerMock(File dir) {
        this.dir = new File(dir, pathPart);
        this.dir.mkdirs();
    }

    @Override
    public void init(PackageGenerationParameters params) {

    }

    @Override
    public URI reserveResource(String path, PackageResourceType type) {
        try {
            File reserved = File.createTempFile("resource-", ".xml", dir);
            reserved.deleteOnExit();
            URI uri = URI.create("file://" + pathPart + reserved.getName());
            files.put(uri, reserved);
            return uri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putResource(URI uri, InputStream content) {
        try {
            assertTrue(files.containsKey(uri));
            FileOutputStream out = new FileOutputStream(files.get(uri));
            IOUtils.copy(content, out);
            
            
            out.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public URI createResource(String path,
                              PackageResourceType type,
                              InputStream content) {
        URI uri = reserveResource(path, type);
        putResource(uri, content);
        return uri;
    }

    @Override
    public org.dataconservancy.packaging.tool.api.Package assemblePackage() {
        return null;
    }

    @Override
    public void addParameter(String key, String value) {

    }

}