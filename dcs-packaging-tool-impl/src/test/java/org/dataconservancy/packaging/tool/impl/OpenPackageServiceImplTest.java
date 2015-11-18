package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;;

public class OpenPackageServiceImplTest {
    private static final String PACKAGE_ZIP_RESOURCE = "fakebag.zip";
    private static final String PACKAGE_TAR_RESOURCE = "fakebag.tar";
    private static final String PACKAGE_TAR_GZ_RESOURCE = "fakebag.tar.gz";

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    private OpenPackageServiceImpl service;
    private PackageStateSerializer serializer;
    private IpmRdfTransformService rdftrans;

    @Before
    public void setup() {
        serializer = mock(PackageStateSerializer.class);
        rdftrans = new IpmRdfTransformService();

        service = new OpenPackageServiceImpl(serializer, rdftrans);
    }

    @Test
    public void testExtractZip() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream(PACKAGE_ZIP_RESOURCE)) {
            assertEquals("fakebag", service.extract(tmpfolder.newFolder(), is));
        }
    }

    @Test
    public void testExtractTar() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream(PACKAGE_TAR_RESOURCE)) {
            assertEquals("fakebag", service.extract(tmpfolder.newFolder(), is));
        }
    }

    @Test
    public void testExtractTarGz() throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream(PACKAGE_TAR_GZ_RESOURCE)) {
            assertEquals("fakebag", service.extract(tmpfolder.newFolder(), is));
        }
    }

    // Test opening a valid package which is available as a resource
    private void test_package(String res) throws Exception {
        File pkgfile = tmpfolder.newFile();
        File stage = tmpfolder.newFolder();

        try (InputStream is = this.getClass().getResourceAsStream(res)) {
            FileUtils.copyInputStreamToFile(is, pkgfile);
        }

        // Cannot load the package state because serializer impl is in a
        // different module. Instead, fake the file data/test.txt being the
        // root of the tree in state. This means the state in the actual package
        // being tested against is not used.

        PackageState fakestate = new PackageState();
        Node root = new Node(URI.create("test:root"));

        String file_path = "data/test.txt";

        FileInfo info = new FileInfo(UriUtility.makeBagUriString(new File(file_path), null), "test.txt");
        root.setFileInfo(info);
        fakestate.setPackageTree(rdftrans.transformToRDF(root));

        doAnswer(invocation -> {
            PackageState state = invocation.getArgumentAt(0, PackageState.class);

            state.setPackageTree(fakestate.getPackageTree());

            return null;
        }).when(serializer).deserialize(any(), any());

        File base_dir = new File(stage, "fakebag");

        OpenedPackage opened_pkg = service.openPackage(stage, pkgfile);

        assertEquals(base_dir, opened_pkg.getBaseDirectory());
        assertNotNull(opened_pkg.getPackageState());
        assertNotNull(opened_pkg.getPackageTree());
        assertEquals(root.getIdentifier(), opened_pkg.getPackageTree().getIdentifier());

        assertEquals(new File(base_dir, file_path).toURI(), opened_pkg.getPackageTree().getFileInfo().getLocation());
    }

    @Test
    public void openZipPackage() throws Exception {
        test_package(PACKAGE_ZIP_RESOURCE);
    }

    @Test
    public void openTarPackage() throws Exception {
        test_package(PACKAGE_TAR_RESOURCE);
    }

    @Test
    public void openTarGzPackage() throws Exception {
        test_package(PACKAGE_TAR_GZ_RESOURCE);
    }
}
