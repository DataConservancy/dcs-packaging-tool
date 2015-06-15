/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.dcs.util.extraction;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PackageSelectorTest {

    private final static String FILE_ONLY_GZIP = "/SampleFilePackages/Windows/File1.txt.gz";
    private final static String TAR_GZIP = "/SampleFilePackagess/Windows/WindowsTarFile.tar.gz";
    private final static String FILES_ONLY_TAR = "/SampleFilePackages/Windows/WindowsTarFile.tar";
    private final static String FILE_ONE = "/SampleFiles/File1.txt";
    private final static String FILES_ONLY_ZIP = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    
    private PackageSelectorImpl packageSelector;
    
    @Before
    public void setup() {
        packageSelector = new PackageSelectorImpl();
        
        Map<String, PackageExtractor> extractors = new HashMap<String, PackageExtractor>();
        extractors.put(PackageSelector.ZIP_KEY, new ZipPackageExtractor());
        extractors.put(PackageSelector.GZIP_KEY, new GZipPackageExtractor());
        extractors.put(PackageSelector.TAR_KEY, new TarPackageExtractor());
        
        packageSelector.setExtractors(extractors);
    }
    
    /**
     * Tests that when a zip file is passed in the content disposition header a zip package extractor is returned.
     */
    @Test
    public void testZipFileContentDispositionReturnsZipExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment;  filename=\"" + FILES_ONLY_ZIP + "\"");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof ZipPackageExtractor);
    }
    
    /**
     * Tests that when a gzip file is passed in the content disposition header a GZip package extractor is returned.
     */
    @Test
    public void testGZipFileContentDispositionReturnsGZipExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=\"" + FILE_ONLY_GZIP + "\"");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof GZipPackageExtractor);
    }
    
    /**
     * Tests that when a tar file is passed in the content disposition header a Tar package extractor is returned.
     */
    @Test
    public void testTarFileContentDispositionReturnsTarExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=\"" + FILES_ONLY_TAR + "\"");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof TarPackageExtractor);
    }
    
    /**
     * Tests that when a tar gzip file is passed in the content disposition header a gzip package extractor is returned.
     */
    @Test
    public void testTarGZipFileContentDispositionReturnsGZipExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=\"" + TAR_GZIP + "\"");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof GZipPackageExtractor);
    }
    
    /**
     * Tests that if a non package file is passed in the content disposition header null is returned.
     */
    @Test
    public void testNotPackageFileReturnsNullExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=\"" + FILE_ONE + "\"");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNull(extractor);
    }
    
    /**
     * Tests that if a zip file mime type header is passed in a zip package extractor is returned.
     */
    @Test
    public void testZipMimeTypeReturnsZipExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Type", "application/zip");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof ZipPackageExtractor);
    }
    
    /**
     * Tests that if a gzip mime type header is passed in a gzip package extractor is returned.
     */
    @Test
    public void testGZipMimeTypeReturnsGZipExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Type", "application/x-gzip");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof GZipPackageExtractor);
    }
    
    /**
     * Tests that if a tar mime type header is passed in a tar package extractor is returned.
     */
    @Test
    public void testTarMimeTypeReturnsTarExtractor() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Type", "application/x-tar");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof TarPackageExtractor);
    }
    
    /**
     * Tests that if a none package mime type is passed in null is returned. 
     */
    @Test
    public void testApplicationMimeTypeReturnsNull() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Type", "application/octet-stream");
        
        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNull(extractor);
    }
    
    /**
     * Tests that the content-disposition header is given preference over the mime type if both are present.
     */
    @Test
    public void testContentDispositionTakesPrecedence() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=" + FILES_ONLY_ZIP);
        metadata.put("Content-Type", "application/octet-stream");

        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof ZipPackageExtractor);
    }
    
    /**
     * Tests that if the content disposition doesn't provide an extractor the mime type is used to determine the extractor.
     */
    @Test
    public void testUnknownContentDispositionKnownContentType() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=" + FILE_ONE);
        metadata.put("Content-Type", "application/zip");

        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof ZipPackageExtractor);
    }
    
    /**
     * Tests that if the content disposition and the mime type don't match content disposition is used.
     */
    @Test
    public void testUnknownMismatchDispositionAndType() {
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("Content-Disposition", "attachment; filename=" + FILES_ONLY_TAR);
        metadata.put("Content-Type", "application/zip");

        PackageExtractor extractor = packageSelector.selectPackageExtractor(null, metadata);
        assertNotNull(extractor);
        assertTrue(extractor instanceof TarPackageExtractor);
    }
}