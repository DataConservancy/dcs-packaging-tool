package org.dataconservancy.packaging.tool.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.packaging.tool.api.OpenPackageService;
import org.dataconservancy.packaging.tool.model.OpenedPackage;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.RDFTransformException;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.dataconservancy.packaging.tool.ser.PackageStateSerializer;

/**
 * Service for opening a package.
 * 
 * A package is assumed to be a possibly compressed archive that contains a set
 * of files all under a base directory and that set of files includes a
 * serialized package state at a particular location.
 */
public class OpenPackageServiceImpl implements OpenPackageService {
    // Location of package state file in package base directory
    private final static String PACKAGE_STATE_PATH = "META-INF/org.dataconservancy.bagit/PKG-DESC/STATE";

    private PackageStateSerializer package_state_serializer;
    private IpmRdfTransformService ipm_transform_service;

    public OpenPackageServiceImpl(PackageStateSerializer package_state_serializer,
            IpmRdfTransformService ipm_transform_service) {
        this.package_state_serializer = package_state_serializer;
        this.ipm_transform_service = ipm_transform_service;
    }

    private PackageState load_package_state(File file) throws IOException {
        PackageState state = new PackageState();

        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            package_state_serializer.deserialize(state, is);
        } catch (UnsupportedOperationException e) {
            // Thrown when called on a non-zip file, wrap as IOException for consistency
            throw new IOException(e);
        }

        return state;
    }

    @Override
    public OpenedPackage openPackageState(File file) throws IOException {
        OpenedPackage result = new OpenedPackage();
        
        PackageState state = load_package_state(file);
        
        result.setPackageState(state);
        
        try {
            // No bag URIs to rewrite
            
            if (state.getPackageTree() != null) {
                result.setPackageTree(ipm_transform_service.transformToNode(state.getPackageTree()));
            }
        } catch (RDFTransformException e) {
            throw new IOException(e);
        }
        
        return result;
    }

    /**
     * Extract contents of an archive.
     * 
     * @param dest_dir
     *            Destination to write archive content.
     * @param is
     *            Archive file.
     * @return Name of package base directory in dest_dir
     */
    protected String extract(File dest_dir, InputStream is) throws ArchiveException, IOException {
        // Apache commons compress requires buffered input streams

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }

        // If file is compressed, uncompress.

        try {
            is = new CompressorStreamFactory().createCompressorInputStream(is);
        } catch (CompressorException e) {
        }

        // Extract entries from archive

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }

        String archive_base = null;

        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(is);
        ArchiveEntry entry;

        while ((entry = ais.getNextEntry()) != null) {
            File file = extract(dest_dir, entry, ais);

            String root = get_root_file_name(file);

            if (archive_base == null) {
                archive_base = root;
            } else if (!archive_base.equals(root)) {
                throw new IOException("Package has more than one base directory.");
            }
        }

        return archive_base;
    }

    private String get_root_file_name(File file) {
        String root;

        do {
            root = file.getName();
        } while ((file = file.getParentFile()) != null);

        return root;
    }

    private String extract(File dest_dir, File file) throws ArchiveException, IOException {
        try (InputStream is = new FileInputStream(file)) {
            return extract(dest_dir, is);
        }
    }

    // Extract entry in an archive and return relative file to extracted entry
    private File extract(File dest_dir, ArchiveEntry entry, ArchiveInputStream ais) throws IOException {
        String path = FilenameUtils.separatorsToSystem(entry.getName());

        File file = new File(dest_dir, path);

        if (entry.isDirectory()) {
            file.mkdirs();
        } else {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            try (OutputStream os = new FileOutputStream(file)) {
                IOUtils.copyLarge(ais, os, 0, entry.getSize());
            }
        }

        return new File(path);
    }

    @Override
    public OpenedPackage openPackage(File staging_dir, File file) throws IOException {
        try {
            String archive_base = extract(staging_dir, file);
            File archive_dir = archive_base == null ? staging_dir : new File(staging_dir, archive_base);

            return openExplodedPackage(archive_dir);
        } catch (ArchiveException e) {
            throw new IOException(e);
        }
    }

    private void update_file_info(Node n, File baseDir) {
        if (n.getFileInfo() != null && UriUtility.isBagUri(n.getFileInfo().getLocation())) {
            Path resolvedPath = UriUtility.resolveBagUri(baseDir.toPath(), n.getFileInfo().getLocation());
            n.getFileInfo().setLocation(resolvedPath.toUri());
        }
    }

    @Override
    public OpenedPackage openExplodedPackage(File dir) throws IOException {
        String path = FilenameUtils.separatorsToSystem(PACKAGE_STATE_PATH);

        PackageState state = load_package_state(new File(dir, path));

        // Load package tree and rewrite bag URIs to point to files in directory

        Node root;

        try {
            root = ipm_transform_service.transformToNode(state.getPackageTree());
        } catch (RDFTransformException e) {
            throw new IOException(e);
        }

        root.walk(n -> update_file_info(n, dir));

        OpenedPackage result = new OpenedPackage();

        result.setBaseDirectory(dir);
        result.setPackageTree(root);
        result.setPackageState(state);

        return result;
    }
}
