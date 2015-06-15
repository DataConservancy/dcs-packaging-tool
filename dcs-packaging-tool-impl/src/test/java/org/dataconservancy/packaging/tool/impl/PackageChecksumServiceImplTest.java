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
package org.dataconservancy.packaging.tool.impl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dataconservancy.packaging.model.Checksum;
import org.dataconservancy.packaging.model.impl.ChecksumImpl;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test class for PackageChecksumServiceImpl
 */
public class PackageChecksumServiceImplTest {
    private PackageChecksumServiceImpl underTest;

    private File fileOneTmp;
    private File fileTwoTmp;
    private String fileOneMd5Sum;
    private String fileOneSha1Sum;
    private String fileTwoMd5Sum;
    private String fileTwoSha1Sum;
    private Checksum checksumFileOneMd5;
    private Checksum checksumFileOneSha1;
    private Checksum checksumFileTwoMd5;
    private Checksum checksumFileTwoSha1;

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();
    
    @Before
    public void setup() throws Exception {

        underTest = new PackageChecksumServiceImpl();
        System.setProperty("line.separator", "\n");
        fileOneTmp = tmpfolder.newFile("testFileOne.txt");

        PrintWriter fileOneOut = new PrintWriter(fileOneTmp);

        fileOneOut.println("This is test file one");
        fileOneOut.close();

        fileOneMd5Sum = "07318f5ba9b5a9a437a4596364518f2b";
        fileOneSha1Sum = "5a6b55bb58e46952aece87f8cca46ba9b53c5628";

        fileTwoTmp = tmpfolder.newFile("testFileTwo.txt");

        PrintWriter fileTwoOut = new PrintWriter(fileTwoTmp);

        fileTwoOut.println("This is test file two");
        fileTwoOut.close();

        fileTwoMd5Sum = "81a2f2595ff8f53f0c9c731f0d0dc6f0";
        fileTwoSha1Sum = "ddfd1963496d51662436ddc7da8016e7e3070d8f";

        checksumFileOneMd5 = new ChecksumImpl("md5", fileOneMd5Sum);
        checksumFileOneSha1 = new ChecksumImpl("sha1", fileOneSha1Sum);
        checksumFileTwoMd5 = new ChecksumImpl("md5", fileTwoMd5Sum);
        checksumFileTwoSha1 = new ChecksumImpl("sha1", fileTwoSha1Sum);
    }

    @Test
    public void testSingleFileMD5Result(){
      List<Checksum> fileOneMD5List = new ArrayList<Checksum>();
      fileOneMD5List.add(checksumFileOneMd5);
      Map<File, List<Checksum>> expected = new HashMap<File, List<Checksum>>();
      expected.put(fileOneTmp, fileOneMD5List);

      Set<File> files = new HashSet<File>();
      files.add(fileOneTmp);

      List<String> algorithms = new ArrayList<String>();
      algorithms.add("md5");
      assertEquals(expected, underTest.generatePackageFileChecksums(files, algorithms));
    }

    @Test
    public void testSingleFileSha1Result(){
      List<Checksum> fileOneMD5List = new ArrayList<Checksum>();
      fileOneMD5List.add(checksumFileOneSha1);
      Map<File, List<Checksum>> expected = new HashMap<File, List<Checksum>>();
      expected.put(fileOneTmp, fileOneMD5List);

      Set<File> files = new HashSet<File>();
      files.add(fileOneTmp);

      List<String> algorithms = new ArrayList<String>();
      algorithms.add("sha1");
      assertEquals(expected, underTest.generatePackageFileChecksums(files, algorithms));
     }

    @Test
    public void testSingleFileTwoAlgorithmsResult(){
      List<Checksum> fileOneList = new ArrayList<Checksum>();
      fileOneList.add(checksumFileOneMd5);
      fileOneList.add(checksumFileOneSha1);
      Map<File, List<Checksum>> expected = new HashMap<File, List<Checksum>>();
      expected.put(fileOneTmp, fileOneList);

      Set<File> files = new HashSet<File>();
      files.add(fileOneTmp);

      List<String> algorithms = new ArrayList<String>();
      algorithms.add("md5");
      algorithms.add("sha1");
      assertEquals(expected, underTest.generatePackageFileChecksums(files, algorithms));
    }

    @Test
    public void testTwoFilesTwoAlgorithmsResult(){
      List<Checksum> fileOneList = new ArrayList<Checksum>();
      fileOneList.add(checksumFileOneMd5);
      fileOneList.add(checksumFileOneSha1);

      List<Checksum> fileTwoList = new ArrayList<Checksum>();
      fileTwoList.add(checksumFileTwoMd5);
      fileTwoList.add(checksumFileTwoSha1);

      Map<File, List<Checksum>> expected = new HashMap<File, List<Checksum>>();
      expected.put(fileOneTmp, fileOneList);
      expected.put(fileTwoTmp, fileTwoList);

      Set<File> files = new HashSet<File>();
      files.add(fileOneTmp);
      files.add(fileTwoTmp);

      List<String> algorithms = new ArrayList<String>();
      algorithms.add("md5");
      algorithms.add("sha1");
      assertEquals(expected, underTest.generatePackageFileChecksums(files, algorithms));
    }

    @Test
    public void testUnknownAlgorithmThrowsException(){
        List<Checksum> fileOneMD5List = new ArrayList<Checksum>();
        fileOneMD5List.add(checksumFileOneMd5);
        Map<File, List<Checksum>> expected = new HashMap<File, List<Checksum>>();
        expected.put(fileOneTmp, fileOneMD5List);

        Set<File> files = new HashSet<File>();
        files.add(fileOneTmp);

        List<String> algorithms = new ArrayList<String>();
        algorithms.add("bogusUnknownAlgorithm");
        boolean exception = false;
       try{
           underTest.generatePackageFileChecksums(files, algorithms);
       } catch (PackageToolException e){
           exception = true;
           assertEquals(PackagingToolReturnInfo.PKG_NO_SUCH_CHECKSUM_ALGORITHM_EXCEPTION.returnCode(), e.getCode());
           assertEquals(PackagingToolReturnInfo.PKG_NO_SUCH_CHECKSUM_ALGORITHM_EXCEPTION.stringMessage() + ": bogusUnknownAlgorithm", e.getMessage());
       }
        assertTrue(exception);
    }


    @Test
    public void testDeletedFileThrowsException(){
        List<Checksum> fileOneMD5List = new ArrayList<Checksum>();
        fileOneMD5List.add(checksumFileOneMd5);

        Set<File> files = new HashSet<File>();
        files.add(fileOneTmp);

        fileOneTmp.delete();

        List<String> algorithms = new ArrayList<String>();
        algorithms.add("md5");

        boolean exception = false;
        try{
            underTest.generatePackageFileChecksums(files, algorithms);
        } catch (PackageToolException e){
            exception = true;
            assertEquals(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION.returnCode(), e.getCode());
            assertEquals(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION.stringMessage() + ": " + fileOneTmp.getPath(), e.getMessage());
        }
        assertTrue(exception);
    }
}
