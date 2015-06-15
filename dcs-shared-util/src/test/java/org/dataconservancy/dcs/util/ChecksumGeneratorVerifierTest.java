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
package org.dataconservancy.dcs.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.String;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import junit.framework.Assert;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class ChecksumGeneratorVerifierTest {

    private InputStream file1;
    private byte[] file1Bytes;
    private String file1MD5checksum = "1e75d5298fd12184f34bad372a81b3e6";
    private String file1SHA1checksum = "428deee4e21cd468d81595597befdc9f21fdf8ee";
    private byte[] file1MD5checksumBytes;
    private byte[] file1SHA1checksumBytes;
    private InputStream modifiedFile1;

    /**
     * Sets up the objects needed to run the tests
     * @throws DecoderException
     * @throws IOException if the ByteArray cannot be constructed
     */
    @Before
    public void setUp() throws DecoderException, IOException {
        file1 = ChecksumGeneratorVerifierTest.class.getResourceAsStream("/file.txt");
        modifiedFile1 = ChecksumGeneratorVerifierTest.class.getResourceAsStream("/modifiedFile.txt");
        file1MD5checksumBytes = Hex.decodeHex(file1MD5checksum.toCharArray());
        file1SHA1checksumBytes = Hex.decodeHex(file1SHA1checksum.toCharArray());
        //byte array version of file
        file1Bytes = IOUtils.toByteArray(ChecksumGeneratorVerifierTest.class.getResourceAsStream("/file.txt"));
    }

    /**
     * Verify that we can generate a correct MD5 checksum hex string for a file, and that if the file is modified the checksum changes
     */
    @Test
    public void testGenerateMD5checksum() {
        String checksum = ChecksumGeneratorVerifier.generateMD5checksum(file1);
        Assert.assertEquals(file1MD5checksum, checksum);
        String modifiedChecksum = ChecksumGeneratorVerifier.generateMD5checksum(modifiedFile1);
        Assert.assertNotSame("The modified file should not have the same checksum.", file1MD5checksum, modifiedChecksum);
    }

    /**
     * Verify that we can generate a correct SHA1 checksum hex string for a file, and that if the file is modified the checksum changes
     */
    @Test
    public void testGenerateSHA1checksum() {
        String checksum = ChecksumGeneratorVerifier.generateSHA1checksum(file1);
        Assert.assertEquals(file1SHA1checksum, checksum);
        String modifiedChecksum = ChecksumGeneratorVerifier.generateSHA1checksum(modifiedFile1);
        Assert.assertNotSame("The modified file should not have the same checksum.", file1SHA1checksum,
                modifiedChecksum);
    }

    /**
     * Verify that we can generate a correct MD5 checksum byte array for a file
     */
    @Test
    public void testGenerateMD5checksumBytes() {
        byte[] checksum = ChecksumGeneratorVerifier.generateChecksumAsBytes(ChecksumGeneratorVerifier.ALGORITHM_MD5, file1);
        Assert.assertTrue(Arrays.equals(file1MD5checksumBytes, checksum));
    }

    /**
     * Verify that we can generate a correct SHA1 checksum byte array for a file
     */
    @Test
    public void testGenerateSHA1checksumBytes() {
        byte[] checksum = ChecksumGeneratorVerifier.generateChecksumAsBytes(ChecksumGeneratorVerifier.ALGORITHM_SHA1, file1);
        Assert.assertTrue(Arrays.equals(file1SHA1checksumBytes, checksum));
    }

    /**
     * Verify that we can generate a correct MD5 checksum byte array for a byte array
     */
    @Test
    public void testGenerateMD5checksumBytesFromByteArray(){
        byte[] checksum = ChecksumGeneratorVerifier.generateChecksumAsBytes("MD5", file1Bytes);
        Assert.assertTrue(Arrays.equals(file1MD5checksumBytes, checksum));
    }

    /**
     * Verify that we can generate a correct SHA1 checksum byte array for a byte array
     * @throws NoSuchAlgorithmException if the supplied algorithm is not recognized
     */
    @Test
    public void testGenerateSHA1checksumBytesFromByteArray() throws NoSuchAlgorithmException {
        byte[] checksum = ChecksumGeneratorVerifier.generateChecksumAsBytes(ChecksumGeneratorVerifier.ALGORITHM_SHA1, file1Bytes);
        Assert.assertTrue(Arrays.equals(file1SHA1checksumBytes, checksum));
    }

    /**
     * Verify that we get the expected exception when supplying a bogus algorithm
     * @throws RuntimeException if the supplied algorithm is not recognized
     */
    @Test(expected=RuntimeException.class)
    public void testBytesBadAlgorithm() {
         byte[] checksum = ChecksumGeneratorVerifier.generateChecksumAsBytes("BogusAlgorithm", file1Bytes);
    }

    /**
     * Verify that we get the expected exception when supplying a bogus algorithm
     * @throws NoSuchAlgorithmException if the supplied algorithm is not recognized
     */
    @Test(expected=NoSuchAlgorithmException.class)
    public void testBadAlgorithm() throws NoSuchAlgorithmException {
         String checksum = ChecksumGeneratorVerifier.generateChecksum("BogusAlgorithm", file1);
    }

    /**
     * Test that we can verify that a correct MD5 checksum matches a calculated checksum
     * @throws NoSuchAlgorithmException if the supplied algorithm is not recognized
     */
    @Test
    public void testVerifyMD5checksumWithOneFileAndOneString() throws NoSuchAlgorithmException {
        Assert.assertTrue("The checksums don't match.",
                ChecksumGeneratorVerifier.verifyChecksum(file1, ChecksumGeneratorVerifier.ALGORITHM_MD5, file1MD5checksum));
    }

    /**
     * Test that we can verify that a correct SHA1 checksum matches a calculated checksum
     * @throws NoSuchAlgorithmException if the supplied algorithm is not recognized
     */
    @Test
    public void testVerifySHA1checksumWithOneFileAndOneString() throws NoSuchAlgorithmException {
        Assert.assertTrue("The checksums don't match.",
                ChecksumGeneratorVerifier.verifyChecksum(file1, ChecksumGeneratorVerifier.ALGORITHM_SHA1, file1SHA1checksum));
    }
    
}
