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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class provides helper methods that generate and verify a checksum. Methods are provided to generate checksums
 * either as hex strings or as byte arrays. The verifier method can be used with either two String
 * checksums or an input stream and a stored checksum to check against.
 * 
 */
public final class ChecksumGeneratorVerifier {
    
    private final static Logger LOG = LoggerFactory.getLogger(ChecksumGeneratorVerifier.class);
    public final static String ALGORITHM_MD5 = "md5";    //same as definition in our own Checksum class
    public final static String ALGORITHM_SHA1 = "sha1";   //same as definition in our own Checksum class

    /**
     * Generates an MD5 checksum for a given file.
     * 
     * @param inputStream the file's InputStream
     * @return String hex checksum
     */
    public static String generateMD5checksum(InputStream inputStream) {
        try {
            if (inputStream != null) {

                byte[] mdBytes = generateChecksumAsBytes(ALGORITHM_MD5, inputStream);
                
                // Converting to hex string.
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdBytes.length; i++) {
                    sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                inputStream.close();
                return sb.toString();
            }
            else {
                LOG.error("Input stream is null!");
                throw new RuntimeException("Input stream is null!");
            }
        }
        catch (Exception e) {
            LOG.error("Could not generate the checksum.", e);
            throw new RuntimeException("Could not generate the checksum.", e);
        }
    }

    /**
     * Generates a SHA1 checksum for a given file.
     * 
     * @param inputStream the file's InputStream
     * @return String hex checksum
     */
    public static String generateSHA1checksum(InputStream inputStream) {
        try {
            if (inputStream != null) {

                byte[] mdBytes =  generateChecksumAsBytes(ALGORITHM_SHA1, inputStream);
                
                // Converting to hex string.
                StringBuffer sb = new StringBuffer("");
                for (int i = 0; i < mdBytes.length; i++) {
                    sb.append(Integer.toString((mdBytes[i] & 0xff) + 0x100, 16).substring(1));
                }

                inputStream.close();
                return sb.toString();
            }
            else {
                LOG.error("Input stream is null!");
                throw new RuntimeException("Input stream is null!");
            }
        }
        catch (Exception e) {
            LOG.error("Could not generate the checksum.", e);
            throw new RuntimeException("Could not generate the checksum.", e);
        }
    }


    /**
     * A convenience method to take a file and an algorithm (MD5 or SHA1) and generate a checksum
     * with the given algorithm.
     * @param inputStream the file's input stream
     * @param algorithm the string representing the algorithm; currently either MD5 or SHA1
     * @return String hex checksum
     * @throws NoSuchAlgorithmException if the algorthim string does not represent a known algorithm
     */
    public static String generateChecksum(String algorithm, InputStream inputStream) throws NoSuchAlgorithmException {

        if(algorithm.equalsIgnoreCase(ALGORITHM_MD5)){
            return generateMD5checksum(inputStream);
        } else if(algorithm.equalsIgnoreCase(ALGORITHM_SHA1)){
            return generateSHA1checksum(inputStream);
        } else {
            throw new NoSuchAlgorithmException("The given algorithm <" + algorithm + "> is not acceptable.");
        }
    }


    /**
     * A method for generating a checksum for a byte array. The returned value is a byte array, not converted to a
     * string.
     * @param algorithm  a string representation of the algorithm used
     * @param fileContents a byte array representing the contents of the file
     * @return byte[] checksum
     */
    public static byte[] generateChecksumAsBytes(String algorithm, byte[] fileContents) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to calculate checksum for byte array : " + e.getMessage(), e);
        }

        return md.digest(fileContents);
    }

    /**
     *  The method that does the actual work of generating checksums for InputStreams. The caller is responsible
     *  for closing the supplied Input Stream after this method returns the digest.
     * @param algorithm the string representing the algorithm; currently either MD5 or SHA1
     * @param inputStream the file's input stream
     * @return byte[] checksum
     */
    public static byte[] generateChecksumAsBytes(String algorithm, InputStream inputStream) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        int read = 0;
        int size = 1024;
        byte[] buf = new byte[size];
        try {
            while ((read = inputStream.read(buf, 0, size)) != -1) {
                md.update(buf, 0, read);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to calculate checksum for Input Stream : " +
                    e.getMessage(), e);
        }

        return md.digest();
    }

    /**
     * Takes a file and an algorithm (MD5 or SHA1) as well as a current checksum, generates checksum with the given
     * algorithm and verifies it against the given checksum.
     * 
     * @param inputStream the file's InputStream
     * @param currentChecksum the current checksum
     * @param algorithm the checksum algorithm
     * @return boolean
     * @throws NoSuchAlgorithmException if the algorthim string does not represent a known algorithm
     */
    public static boolean verifyChecksum(InputStream inputStream, String algorithm, String currentChecksum)
            throws NoSuchAlgorithmException {
        if(currentChecksum == null){
            throw new IllegalArgumentException("Checksum to be verified must not be null");
        }

        String newChecksum = null;
        if (algorithm.equalsIgnoreCase(ALGORITHM_MD5)) {
            newChecksum = generateMD5checksum(inputStream);
        }
        else if (algorithm.equalsIgnoreCase(ALGORITHM_SHA1)) {
            newChecksum = generateSHA1checksum(inputStream);
        }
        else {
            throw new NoSuchAlgorithmException("The given algorithm <" + algorithm + "> is not acceptable.");
        }

        return newChecksum.equals(currentChecksum);

    }

}
