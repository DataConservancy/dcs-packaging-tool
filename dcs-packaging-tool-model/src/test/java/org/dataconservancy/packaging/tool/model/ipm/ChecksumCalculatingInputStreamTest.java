/*
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.dataconservancy.packaging.tool.model.ipm;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ChecksumCalculatingInputStreamTest {

    @Test
    public void testCorrectChecksum() throws Exception {
        final String expectedSha1 = "bae5ed658ab3546aee12f23f36392f35dba1ebdd";
        final String expectedMd5 = "ce90a5f32052ebbcd3b20b315556e154";
        final InputStream data = this.getClass().getResourceAsStream("/fox.txt");

        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        FileInfo.ChecksumCalculatingInputStream underTest =
                new FileInfo.ChecksumCalculatingInputStream(data, Arrays.asList(md5, sha1));

        IOUtils.copy(underTest, new NullOutputStream());

        underTest.close();

        Map<MessageDigest, byte[]> digests = underTest.digests();
        assertEquals(expectedSha1, Hex.encodeHexString(digests.get(sha1)));
        assertEquals(expectedMd5, Hex.encodeHexString(digests.get(md5)));
    }
}