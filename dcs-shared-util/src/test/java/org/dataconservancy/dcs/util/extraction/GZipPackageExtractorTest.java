/* Copyright 2012 Johns Hopkins University
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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GZipPackageExtractorTest {
    private final static String FILE_ONLY_GZIP = "/SampleFilePackages/Windows/GzippedFile1.txt.gz";
    private final static String TAR_GZIP = "/SampleFilePackages/Windows/WindowsTarFile.tar.gz";
    private final static String UNPACK_DIRECTORY = "./TestDir/";
    private final static String FILE_ONE = "/SampleFilePackages/File1.txt";
    private GZipPackageExtractor packageExtractor;
    
    @Before
    public void setup(){
        packageExtractor = new GZipPackageExtractor();        
    }
    
    @Test
    public void testUnpackFileGZipFile() throws Exception{
        URL fileUrl = GZipPackageExtractorTest.class.getResource(FILE_ONLY_GZIP);
        Assert.assertNotNull(fileUrl);
        
        File gzipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, gzipFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackTarGzipFile() throws Exception{
        URL fileUrl = GZipPackageExtractorTest.class.getResource(TAR_GZIP);
        Assert.assertNotNull(fileUrl);        
        
        File gzipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);
        List<File> gZippedFiles = packageExtractor.getFilesFromPackageFile(fileName, gzipFile);
                
        Assert.assertNotNull(gZippedFiles);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, gZippedFiles.size());
        
        File tarFile = gZippedFiles.get(0);
        TarPackageExtractor tarExtractor = new TarPackageExtractor();
        List<File> files = tarExtractor.getFilesFromPackageFile(fileName, tarFile);
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());

        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testFileEquality() throws Exception{
        URL gzipFileUrl = GZipPackageExtractorTest.class.getResource(FILE_ONLY_GZIP);
        Assert.assertNotNull(gzipFileUrl);
        
        File gzipFile = new File(gzipFileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, gzipFile);
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, files.size());
        
        URL fileOneURL = GZipPackageExtractorTest.class.getResource(FILE_ONE);
        Assert.assertNotNull(fileOneURL);
        
        File fileOne = new File(fileOneURL.toURI());
        Assert.assertNotNull(fileOne);
        
        boolean foundFileOne = false;
        
        for( File file : files){
            if( file.getName().equalsIgnoreCase("GzippedFile1.txt")){
                Assert.assertTrue("Expected file to equal File1.txt", FileUtils.contentEquals(file, fileOne));
                foundFileOne = true;
            }
        }
        
        //Test to make sure that our comparisson was actually run
        Assert.assertTrue("File comparison wasn't run on file one", foundFileOne);
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    
    @Test
    public void testUnpackFilesIntoDirectory() throws Exception {
        URL fileUrl = TarPackageExtractorTest.class.getResource(TAR_GZIP);
        Assert.assertNotNull(fileUrl);
        
        File gzipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);
        packageExtractor.setExtractDirectory(UNPACK_DIRECTORY);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, gzipFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, files.size());
        Assert.assertTrue(files.get(0).getPath().contains("TestDir"));

        packageExtractor.cleanUpExtractedPackage(new File(UNPACK_DIRECTORY));
    }
    
    @Test
    public void testUnpackFileStream() throws Exception {
        URL fileUrl = GZipPackageExtractorTest.class.getResource(FILE_ONLY_GZIP);
        Assert.assertNotNull(fileUrl);
        
        File gzipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);
        FileInputStream fis = new FileInputStream(gzipFile);
        assertNotNull(fis);
        
     
        List<File> files = packageExtractor.getFilesFromPackageStream(fileName, gzipFile.getName(), fis);

        fis.close();
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackTarGzipFromStream() throws Exception {
        URL fileUrl = GZipPackageExtractorTest.class.getResource(TAR_GZIP);
        Assert.assertNotNull(fileUrl);        
        
        File gzipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(gzipFile);
        String fileName = gzipFile.getName().substring(0, gzipFile.getName().length()-7);

        FileInputStream fis = new FileInputStream(gzipFile);
        assertNotNull(fis);
        
        List<File> gZippedFiles = packageExtractor.getFilesFromPackageStream(fileName, gzipFile.getName(), fis);
                
        Assert.assertNotNull(gZippedFiles);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, gZippedFiles.size());
        
        File tarFile = gZippedFiles.get(0);
        TarPackageExtractor tarExtractor = new TarPackageExtractor();
        List<File> files = tarExtractor.getFilesFromPackageFile(fileName, tarFile);
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());

        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
}