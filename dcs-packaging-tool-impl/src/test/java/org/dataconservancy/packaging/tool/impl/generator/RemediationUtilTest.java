/*
 * Copyright 2017 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.impl.SimpleURIGenerator;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Test;

import static org.apache.commons.codec.digest.DigestUtils.shaHex;
import static org.dataconservancy.packaging.tool.api.generator.PackageResourceType.DATA;
import static org.dataconservancy.packaging.tool.impl.generator.PathTestingUtils.components;
import static org.dataconservancy.packaging.tool.impl.generator.PathTestingUtils.join;
import static org.dataconservancy.packaging.tool.impl.generator.PathTestingUtils.stringOf;
import static org.dataconservancy.packaging.tool.impl.generator.RemediationUtil.remediatePath;
import static org.dataconservancy.packaging.tool.impl.generator.RemediationUtil.unique;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class RemediationUtilTest {

    private static final String PROFILE_ID = "http://dataconservancy.org/formats/data-conservancy-pkg-1.0";

    private String dataPathComponent = join("/", DATA.getRelativePackageLocation());

    private URIGenerator uriGen = new SimpleURIGenerator();

    @Test
    public void testIllegalUtf8CharacterRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "hXllo wXrld!.txt"),
                remediatePath(join("/", dataPathComponent, "héllo wörld!.txt"), PROFILE_ID));
    }

    @Test
    public void testIllegalBlacklistedCharacterRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "README.txtX"),
                remediatePath(join("/", dataPathComponent, "README.txt~"), PROFILE_ID));
    }

    @Test
    public void testIllegalPathComponentLengthRemediation() throws Exception {
        final String illegalComponent = stringOf(256);
        final String illegalPath = join("/", dataPathComponent, illegalComponent);
        assertEquals(illegalPath.length() - 1, remediatePath(illegalPath, PROFILE_ID).length());
    }

    @Test
    public void testIllegalPathLengthRemediation() throws Exception {
        final String illegalComponents = components("/", 4, 255);
        final String illegalPath = join("/", dataPathComponent, illegalComponents);
        assertTrue(illegalPath.length() > 1024);
        assertEquals(1024, remediatePath(illegalPath, PROFILE_ID).length());
    }

    @Test
    public void testReservedWindowsFilenameRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXX"),
                remediatePath(join("/", dataPathComponent, "CON"), PROFILE_ID));
    }

    @Test
    public void testReservedWindowsPathRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXX", "file.txt"),
                remediatePath(join("/", dataPathComponent, "CON", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testReservedWindowsFilenameWithCounterRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXXX"),
                remediatePath(join("/", dataPathComponent, "LPT9"), PROFILE_ID));
    }

    @Test
    public void testReservedWindowsPathWithCounterRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXXX", "file.txt"),
                remediatePath(join("/", dataPathComponent, "LPT9", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testReservedWindowsFilenameWithExtensionRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXXXXXX"),
                remediatePath(join("/", dataPathComponent, "CON.txt"), PROFILE_ID));
    }

    @Test
    public void testReservedWindowsPathWithExtensionRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXXXXXX", "file.txt"),
                remediatePath(join("/", dataPathComponent, "CON.txt", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testNonGreedyWindowsPathRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXX", "file.txt"),
                remediatePath(join("/", dataPathComponent, "COM", "file.txt"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "COMPACT", "file.txt"),
                remediatePath(join("/", dataPathComponent, "COMPACT", "file.txt"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "MYCOM", "file.txt"),
                remediatePath(join("/", dataPathComponent, "MYCOM", "file.txt"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "RECOMPOSE", "file.txt"),
                remediatePath(join("/", dataPathComponent, "RECOMPOSE", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testNonGreedyWindowsFilenameRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XXX"),
                remediatePath(join("/", dataPathComponent, "COM"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "COMPACT"),
                remediatePath(join("/", dataPathComponent, "COMPACT"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "MYCOM"),
                remediatePath(join("/", dataPathComponent, "MYCOM"), PROFILE_ID));

        assertEquals(join("/", dataPathComponent, "RECOMPOSE"),
                remediatePath(join("/", dataPathComponent, "RECOMPOSE"), PROFILE_ID));
    }

    @Test
    public void testDotDotPathComponentRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XX", "file.txt"),
                remediatePath(join("/", dataPathComponent, "..", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testDotPathComponentRemediation() throws Exception {
        assertEquals(join("/", dataPathComponent, "X", "file.txt"),
                remediatePath(join("/", dataPathComponent, ".", "file.txt"), PROFILE_ID));
    }

    @Test
    public void testDotDotFilenameRemedation() throws Exception {
        assertEquals(join("/", dataPathComponent, "XX"),
                remediatePath(join("/", dataPathComponent, ".."), PROFILE_ID));

    }

    @Test
    public void testDotFilenameRemedation() throws Exception {
        assertEquals(join("/", dataPathComponent, "X"),
                remediatePath(join("/", dataPathComponent, "."), PROFILE_ID));
    }

    @Test
    public void testUniqueAbsolutePathWithFile() throws Exception {
        Node n = new Node(uriGen.generateNodeURI());
        String hint =     "/path/to/file.txt";
        String expected = "/path/to/" + shaHex(n.getIdentifier().toString());

        assertEquals(expected, unique(n, hint));
    }

    @Test
    public void testUniqueRelativePathWithFile() throws Exception {
        Node n = new Node(uriGen.generateNodeURI());
        String hint =     "path/to/file.txt";
        String expected = "path/to/" + shaHex(n.getIdentifier().toString());

        assertEquals(expected, unique(n, hint));
    }

    @Test
    public void testUniqueAbsolutePathWithDir() throws Exception {
        Node n = new Node(uriGen.generateNodeURI());
        String hint =     "/path/to/directory/";
        String expected = "/path/to/" + shaHex(n.getIdentifier().toString()) + "/";

        assertEquals(expected, unique(n, hint));
    }

    @Test
    public void testUniqueRelativePathWithDir() throws Exception {
        Node n = new Node(uriGen.generateNodeURI());
        String hint =     "path/to/directory/";
        String expected = "path/to/" + shaHex(n.getIdentifier().toString()) + "/";

        assertEquals(expected, unique(n, hint));
    }

    @Test
    public void testUniqueSingleFile() throws Exception {
        Node n = new Node(uriGen.generateNodeURI());
        String hint =     "file.txt";
        String expected = shaHex(n.getIdentifier().toString());

        assertEquals(expected, unique(n, hint));
    }
}
