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
import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TarPackageExtractorTest {
    private final static String FILES_ONLY_TAR = "/SampleFilePackages/Windows/WindowsTarFile.tar";
    private final static String DIRECTORY_TAR = "/SampleFilePackages/Windows/WindowsTarFileWDirectory.tar";
    private final static String FOLDER_WITH_ONLY_SUBFOLDER = "/SampleFilePackages/first_directory.tar";

    private final static String UNPACK_DIRECTORY = "./TestDir/";
    private final static String FILE_ONE = "/SampleFilePackages/File1.txt";
    private final static String FILE_TWO = "/SampleFilePackages/File2.txt";

    private final static String BAD_TAR_DIRECTORY = "/SampleFilePackages/tar_with_bad_directory.tar";
    private final static String BAD_TAR_FILE = "/SampleFilePackages/tar_with_bad_file.tar";
    private TarPackageExtractor packageExtractor;
    
    @Before
    public void setup(){
        packageExtractor = new TarPackageExtractor();        
    }
    
    @Test
    public void testUnpackFileOnlyTarFile() throws Exception{
        URL fileUrl = TarPackageExtractorTest.class.getResource(FILES_ONLY_TAR);
        Assert.assertNotNull(fileUrl);
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);
        
        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackDirectoryTarFile() throws Exception{
        URL fileUrl = TarPackageExtractorTest.class.getResource(DIRECTORY_TAR);
        Assert.assertNotNull(fileUrl);
        
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);
        
        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);
        
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);
        
        Assert.assertNotNull(files);

        Assert.assertEquals("Expected there to be 6 files in the package", 6, files.size());
        
        //Check that folders inside the directory have the directory hierarchy
        int directoryFileCount = 0;

        for( File file : files) {           
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
    public void testFileEquality() throws Exception{
        URL tarFileUrl = TarPackageExtractorTest.class.getResource(FILES_ONLY_TAR);
        Assert.assertNotNull(tarFileUrl);
        
        File tarFile = new File(tarFileUrl.toURI());
        Assert.assertNotNull(tarFile);
        
        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);
        
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        
        URL fileOneURL = TarPackageExtractorTest.class.getResource(FILE_ONE);
        Assert.assertNotNull(fileOneURL);
        URL fileTwoURL = TarPackageExtractorTest.class.getResource(FILE_TWO);
        Assert.assertNotNull(fileTwoURL);
        
        File fileOne = new File(fileOneURL.toURI());
        Assert.assertNotNull(fileOne);
        File fileTwo = new File(fileTwoURL.toURI());
        Assert.assertNotNull(fileTwo);
        
        boolean foundFileOne = false;
        boolean foundFileTwo = false;
        
        for( File file : files){
            if( file.getName().equalsIgnoreCase("TarFile1.txt")){
                Assert.assertTrue("Expected file to equal File1.txt", FileUtils.contentEquals(file, fileOne));
                foundFileOne = true;
            }
            
            if( file.getName().equalsIgnoreCase("TarFile2.txt")){
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
        URL fileUrl = TarPackageExtractorTest.class.getResource(DIRECTORY_TAR);
        Assert.assertNotNull(fileUrl);
        
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);
        
        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);
        
        packageExtractor.setExtractDirectory(UNPACK_DIRECTORY);
        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 6 files in the package", 6, files.size());
        
        //Check that folders inside the directory have the directory hierarchy
        int directoryFileCount = 0;

        for( File file : files){
            Assert.assertTrue(file.getPath().contains("TestDir"));
            if( file.getParent() != null ){
                String unpackDirectory = UNPACK_DIRECTORY + fileName + "/";
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
        packageExtractor.cleanUpExtractedPackage(new File(UNPACK_DIRECTORY));
    }
    
    @Test
    public void testUnpackFileStream() throws Exception {
        URL fileUrl = GZipPackageExtractorTest.class.getResource(FILES_ONLY_TAR);
        Assert.assertNotNull(fileUrl);
        
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);

        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);
        
        FileInputStream fis = new FileInputStream(tarFile);
        assertNotNull(fis);
        
     
        List<File> files = packageExtractor.getFilesFromPackageStream(fileName, tarFile.getName(), fis);

        fis.close();
        
        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }
    
    @Test
    public void testUnpackFolderWithOnlySubFolder() throws Exception {
        URL fileUrl = TarPackageExtractorTest.class.getResource(FOLDER_WITH_ONLY_SUBFOLDER);
        Assert.assertNotNull(fileUrl);
        
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);
        
        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);

        Assert.assertNotNull(files);
        Assert.assertEquals("Expected there to be 3 files in the package", 3, files.size());
        packageExtractor.cleanUpExtractedPackage(new File("./" + fileName));
    }

    @Test(expected=UnpackException.class)
    public void testTarFileWithInvalidDirectoryNameThrowsUnpackException() throws Exception{
        URL fileUrl = TarPackageExtractorTest.class.getResource(BAD_TAR_DIRECTORY);
        Assert.assertNotNull(fileUrl);
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);

        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);
    }

    @Test(expected=UnpackException.class)
    public void testTarFileWithInvalidFileNameThrowsUnpackException() throws Exception{
        URL fileUrl = TarPackageExtractorTest.class.getResource(BAD_TAR_FILE);
        Assert.assertNotNull(fileUrl);
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);

        String fileName = tarFile.getName().substring(0, tarFile.getName().length()-4);

        List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);
    }

    @Test
    public void testErrorStringForInvalidFileName() throws Exception{
        URL fileUrl = TarPackageExtractorTest.class.getResource(BAD_TAR_FILE);
        Assert.assertNotNull(fileUrl);
        File tarFile = new File(fileUrl.toURI());
        Assert.assertNotNull(tarFile);

        String fileName = tarFile.getName().substring(0, tarFile.getName().length() - 4);
        String expectedErrorMessage = "Path element in ./tar_with_bad_file/testdirectory2/bad?CharacterIsQuestionMark contains an invalid character: one or more of <>:\"/|?*\n";
        String actualMessage = "";
        try {
            List<File> files = packageExtractor.getFilesFromPackageFile(fileName, tarFile);
        } catch(UnpackException ue) {
            actualMessage=ue.getMessage();
        }

        //TODO: This test does not work on windows
        if (!SystemUtils.IS_OS_WINDOWS) {
            Assert.assertTrue(expectedErrorMessage.equals(actualMessage));
        }
    }
}