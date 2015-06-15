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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZipPackageExtractorTest {
    private final static String FILES_ONLY_ZIP = "/SampleFilePackages/Windows/WindowsZipFile.zip";
    private final static String NESTED_EMPTY_DIRECTORY_ZIP = "/SampleFilePackages/SimpleCollection.zip";
    private final static String DIRECTORY_ZIP = "/SampleFilePackages/Windows/WindowsZipFileWDirectory.zip";
    private final static String FOLDER_WITH_ONLY_SUBFOLDER = "/SampleFilePackages/first_directory.zip";
    private final static String NESTED_ZIP = "/SampleFilePackages/Windows/NestedZip.zip";
    private final static String FILE_ONE = "/SampleFilePackages/File1.txt";
    private final static String FILE_TWO = "/SampleFilePackages/File2.txt";
    private final static String UNZIP_DIRECTORY = "./TestDir/";
    private ZipPackageExtractor packageExtractor;
    
    @Before
    public void setup(){
        packageExtractor = new ZipPackageExtractor();        
    }
    
    @Test
    public void testUnpackFileOnlyZipFile() throws Exception{
        URL fileUrl = ZipPackageExtractorTest.class.getResource(FILES_ONLY_ZIP);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);
        
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackDirectoryZipFile() throws Exception{
        URL fileUrl = ZipPackageExtractorTest.class.getResource(DIRECTORY_ZIP);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);

        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);
        
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 6 files in the package", 6, files.size());
        
        //Check that folders inside the directory have the directory hierarchy
        int directoryFileCount = 0;

        for( File file : files){
           
            if( file.getParent() != null ){
                String unpackDirectory = "./" + fileName + "/";
                if(file.getParent().length() > unpackDirectory.length()){
                    String fileParent = file.getParent().substring(unpackDirectory.length());
                    directoryFileCount++;
                    Assert.assertTrue("Expected parent directory to be 'Directory' but was: " + fileParent, fileParent.equalsIgnoreCase("Directory"));
                }
            }
        }
        
        Assert.assertEquals("Expected two nested files", 2, directoryFileCount);
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }

    @Test
    public void testUnpackZipFileWithEmptyDirectory() throws Exception{
        URL fileUrl = ZipPackageExtractorTest.class.getResource(NESTED_EMPTY_DIRECTORY_ZIP);
        Assert.assertNotNull(fileUrl);

        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);

        String fileName = "testPackageDir";

        List<File> files = packageExtractor.unpackFilesFromStreamWithEmptyDirectories(new FileInputStream(zipFile), fileName, zipFile.getName());

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());

        for (File file : files) {
            assertTrue(file.getPath().contains(fileName));
            if (file.isDirectory()) {
                assertTrue(file.getPath().contains("SimpleCollection"));
                if (file.getParentFile().getParentFile() != null &&
                    file.getParentFile().getParentFile().getParentFile() == null) {
                    //assert that third level down directory is named "Directory
                    assertTrue(file.getPath().contains("Directory"));
                }
            } else {
                assertTrue(file.getPath().contains("SimpleCollection"));
                assertTrue(file.getPath().contains("README.txt"));
            }
        }


        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    
    @Test
    public void testUnpackNestedZipFile() throws Exception{
        URL fileUrl = ZipPackageExtractorTest.class.getResource(NESTED_ZIP);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);
        
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 1 files in the package", 1, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testFileEquality() throws Exception{
        URL zipFileUrl = ZipPackageExtractorTest.class.getResource(FILES_ONLY_ZIP);
        Assert.assertNotNull(zipFileUrl);
        
        File zipFile = new File(zipFileUrl.toURI());
        Assert.assertNotNull(zipFile);
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        
        URL fileOneURL = ZipPackageExtractorTest.class.getResource(FILE_ONE);
        Assert.assertNotNull(fileOneURL);
        URL fileTwoURL = ZipPackageExtractorTest.class.getResource(FILE_TWO);
        Assert.assertNotNull(fileTwoURL);
        
        File fileOne = new File(fileOneURL.toURI());
        Assert.assertNotNull(fileOne);
        File fileTwo = new File(fileTwoURL.toURI());
        Assert.assertNotNull(fileTwo);
        
        boolean foundFileOne = false;
        boolean foundFileTwo = false;
        
        for( File file : files){
            if( file.getName().equalsIgnoreCase("File1.txt")){
                Assert.assertTrue("Expected file to equal File1.txt", FileUtils.contentEquals(file, fileOne));
                foundFileOne = true;
            }
            
            if( file.getName().equalsIgnoreCase("File2.txt")){
                Assert.assertTrue("Expected file to equal File2.txt", FileUtils.contentEquals(file, fileTwo));
                foundFileTwo = true;
            }
        }
        
        //Test to make sure that our comparisson was actually run
        Assert.assertTrue("File comparison wasn't run on file one", foundFileOne);
        Assert.assertTrue("File comparison wasn't run on file two", foundFileTwo);  
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    
    @Test
    public void testUnpackFilesIntoDirectory() throws Exception{
        URL fileUrl = ZipPackageExtractorTest.class.getResource(DIRECTORY_ZIP);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);
        packageExtractor.setExtractDirectory(UNZIP_DIRECTORY);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 6 files in the package", 6, files.size());
        
        //Check that folders inside the directory have the directory hierarchy
        int directoryFileCount = 0;

        for( File file : files){
            Assert.assertTrue(file.getPath().contains("TestDir"));
            if( file.getParent() != null ){
                String unpackDirectory = UNZIP_DIRECTORY + fileName + "/";
                if(file.getParent().length() > unpackDirectory.length()){                    
                    String fileParent = file.getParent().substring(unpackDirectory.length());
                    String parentDirectory = file.getParent().substring(0, unpackDirectory.length());
                    Assert.assertEquals(parentDirectory.length(), unpackDirectory.length() );
                    directoryFileCount++;
                    Assert.assertTrue("Expected parent directory to be 'Directory' but was: " + fileParent, fileParent.equalsIgnoreCase("Directory"));
                }
            }
        }
        
        Assert.assertEquals("Expected two nested files", 2, directoryFileCount);
        packageExtractor.cleanUpExtractedPackage(new File(UNZIP_DIRECTORY));
    }
    
    @Test
    public void testUnpackFileStream() throws Exception {
        URL fileUrl = ZipPackageExtractorTest.class.getResource(FILES_ONLY_ZIP);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);
        FileInputStream fis = new FileInputStream(zipFile);
        assertNotNull(fis);
        
     
        List<File> files = packageExtractor.getFilesFromPackageStream(fileName, zipFile.getName(), fis);

        fis.close();
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackFolderWithOnlySubFolder() throws Exception {
        URL fileUrl = ZipPackageExtractorTest.class.getResource(FOLDER_WITH_ONLY_SUBFOLDER);
        Assert.assertNotNull(fileUrl);
        
        File zipFile = new File(fileUrl.toURI());
        Assert.assertNotNull(zipFile);
        
        String fileName = zipFile.getName().substring(0, zipFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, zipFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
}