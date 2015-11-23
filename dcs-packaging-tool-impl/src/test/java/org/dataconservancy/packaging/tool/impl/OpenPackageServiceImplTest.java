package org.dataconservancy.packaging.tool.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
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
import org.junit.rules.TemporaryFolder;

public class OpenPackageServiceImplTest {
    private static final String PACKAGE_ZIP_RESOURCE = "fakebag.zip";
    private static final String PACKAGE_TAR_RESOURCE = "fakebag.tar";
    private static final String PACKAGE_TAR_GZ_RESOURCE = "fakebag.tar.gz";
    private static final String PACKAGE_STATE_RESOURCE = "fakebag/META-INF/org.dataconservancy.bagit/STATE";

    @Rule
    public TemporaryFolder tmpfolder = new TemporaryFolder();

    private OpenPackageServiceImpl service;
    private PackageStateSerializer serializer;
    private IpmRdfTransformService rdftrans;
    private Node fake_package_tree;
    private String fake_file_path;

    @Before
    public void setup() throws Exception {
        // Must mock serializer since its impl is in another moodule. Fake the
        // file data/test.txt being the root of the tree in state. This means
        // the state in the actual package being tested against is not used.

        rdftrans = new IpmRdfTransformService();
        serializer = mock(PackageStateSerializer.class);
        service = new OpenPackageServiceImpl(serializer, rdftrans);

        PackageState fakestate = new PackageState();
        fake_package_tree = new Node(URI.create("test:root"));

        fake_file_path = "data/test.txt";

        FileInfo info = new FileInfo(UriUtility.makeBagUriString(new File(fake_file_path), null), "test.txt");
        fake_package_tree.setFileInfo(info);
        fakestate.setPackageTree(rdftrans.transformToRDF(fake_package_tree));

        doAnswer(invocation -> {
            PackageState state = invocation.getArgumentAt(0, PackageState.class);

            state.setPackageTree(fakestate.getPackageTree());

            return null;
        }).when(serializer).deserialize(any(), any());

    }

    private void copy_resource(String res, File file) throws IOException {
        try (InputStream is = this.getClass().getResourceAsStream(res)) {
            FileUtils.copyInputStreamToFile(is, file);
        }
    }

    // Check that package is extracted to correct directory and that expected
    // data subdirectory is present
    private void test_extract(String res) throws Exception {
        try (InputStream is = this.getClass().getResourceAsStream(res)) {
            File base_dir = tmpfolder.newFolder();

            assertEquals("fakebag", service.extract(base_dir, is));
            assertTrue(new File(new File(base_dir, "fakebag"), "data").exists());
        }
    }

    @Test
    public void testExtractZip() throws Exception {
        test_extract(PACKAGE_ZIP_RESOURCE);
    }

    @Test
    public void testExtractTar() throws Exception {
        test_extract(PACKAGE_TAR_RESOURCE);
    }

    @Test
    public void testExtractTarGz() throws Exception {
        test_extract(PACKAGE_TAR_GZ_RESOURCE);
    }

    // Test opening a valid package which is available as a resource
    private void test_package(String res) throws Exception {
        File pkgfile = tmpfolder.newFile();
        File stage = tmpfolder.newFolder();

        copy_resource(res, pkgfile);

        File base_dir = new File(stage, "fakebag");

        OpenedPackage opened_pkg = service.openPackage(stage, pkgfile);

        assertNotNull(opened_pkg);
        assertEquals(base_dir, opened_pkg.getBaseDirectory());
        assertNotNull(opened_pkg.getPackageState());
        assertNotNull(opened_pkg.getPackageTree());
        assertEquals(fake_package_tree.getIdentifier(), opened_pkg.getPackageTree().getIdentifier());

        assertEquals(new File(base_dir, fake_file_path).toURI(),
                opened_pkg.getPackageTree().getFileInfo().getLocation());
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

    @Test
    public void openExplodedPackageTest() throws Exception {
        // Setup exploded package
        File extract_dir = tmpfolder.newFolder();
        File base_dir = new File(extract_dir, "fakebag");
        
        new File(extract_dir, "fakebag/data").mkdirs();
        File stateDir = new File(extract_dir, PACKAGE_STATE_RESOURCE);
        stateDir.mkdirs();
        System.out.println(stateDir.getPath());
        
        copy_resource(PACKAGE_STATE_RESOURCE, new File(stateDir, "state.bin"));
        
        OpenedPackage opened_pkg = service.openExplodedPackage(base_dir);
        
        assertNotNull(opened_pkg);
        assertEquals(base_dir, opened_pkg.getBaseDirectory());
        assertNotNull(opened_pkg.getPackageState());
        assertNotNull(opened_pkg.getPackageTree());
        assertEquals(fake_package_tree.getIdentifier(), opened_pkg.getPackageTree().getIdentifier());
    }

    @Test
    public void openStateFileTest() throws Exception {
        File statefile = tmpfolder.newFile();

        copy_resource(PACKAGE_STATE_RESOURCE, statefile);

        OpenedPackage opened_pkg = service.openPackageState(statefile);

        assertNotNull(opened_pkg);
        assertNull(opened_pkg.getBaseDirectory());
        assertNotNull(opened_pkg.getPackageState());
        assertNotNull(opened_pkg.getPackageTree());
        assertEquals(fake_package_tree.getIdentifier(), opened_pkg.getPackageTree().getIdentifier());
    }
}
