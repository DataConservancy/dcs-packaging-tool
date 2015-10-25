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
package org.dataconservancy.packaging.tool.impl.generator;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.dataconservancy.dcs.util.FilePathUtil;
import org.dataconservancy.dcs.util.UriUtility;
import org.dataconservancy.dcs.model.Checksum;
import org.dataconservancy.packaging.tool.api.PackageChecksumService;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.api.Package;
import org.dataconservancy.packaging.tool.impl.PackageChecksumServiceImpl;
import org.dataconservancy.packaging.tool.model.BagItParameterNames;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.dataconservancy.packaging.tool.model.SupportedMimeTypes;
import org.joda.time.DateTime;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Assembles a BagIt package. Bags assembled by this particular implementation will conform to BagIt specification
 * version V0.97
 * </p>
 * This implementation uses org.apache.commons.compress library to perform archiving and compression operations
 * on the resulting package. Thereore, it is limited to only support the archiving and compression formats specified in
 * org.apache.commons.compress.archivers.ArchiveStreamFactory and
 * org.apache.commons.compress.compressors.CompressorStreamFactory.
 *
 * If the archiving format is "exploded," the assembler will stage the package in the location normally specified for
 * the package file, and the assemblePackage() method will return a null package without deleting the staged content
 *
 */
public class BagItPackageAssembler implements PackageAssembler {


    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final static String requiredParametersMessage =  "These following parameters are required for the " +
            "operation of this Assembler: " +   BagItParameterNames.PACKAGE_NAME + ", " +
                                                BagItParameterNames.PACKAGE_LOCATION + ", " +
                                                BagItParameterNames.BAGIT_PROFILE_ID;

    private final static String ENCODING = "UTF-8";
    private final static String VERSION = "0.97";

    private static PackageChecksumService checksumService = new PackageChecksumServiceImpl();

    private PackageGenerationParameters params = null;

    private File bagBaseDir = null;
    private File payloadDir = null;
    private File packageLocationDir = null;

    private boolean isExploded = false;

    /**
     * Map of the Relative URIs to the Absolute resolvable URI of a file.
     */
    private Map<URI, URI> fileURIMap = new HashMap<>();

    /**
     * Set of resolvable URIs for data files kept track of by this Assembler
     */
    private Set<File> dataFiles = new HashSet<>();
    /**
     * Set of resolvable URIs for tag files kept track of by this Assembler
     */
    private Set<File> tagFiles = new HashSet<>();

    /**
     * List of checksums algs to be performed when creating bags
     */
    private List<String> checksumAlgs = new ArrayList<>();

    /**
     * Indicates archiving format which will be used to serialize the resulting package. Initialized with default value.
     */
    private String archivingFormat = ArchiveStreamFactory.TAR;

    /**
     * Compression format which will be used to compress serialized package as specified in the input parameter. If
     * none is specified, no compression will be done on the package content serialization.
     */
    private String compressionFormat = null;

    /**
     * Initializes the Assembler. Operations include:
     * <ul>
     *     <li>Checking for required parameters</li>
     *     <li>Creating a base directory for the bag</li>
     *     <li>Creating a payload directory</li>
     * </ul>
     *
     * Required parameters for this kind of Assembler include:
     * <ul>
     *     <li>package-name</li>
     *     <li>Bag-It-Profile-Identifier</li>
     *     <li>Contact-Name</li>
     *     <li>Contact-Phone</li>
     *     <li>Contact-Email</li>
     * </ul>
     *
     * Optional with defaults parameters:
     * <ul>
     *     <li> archiving-format: when not set, is defaulted to ".tar" </li>
     *     <li> compression-format: when not set, no compression will be performed on the serialized content. </li>
     *     <li> checksum-algs: when not set, is defaulted to "md5" </li>
     * </ul>
     *
     * NOTE: If this is called a second time, the first initialization will still take effect unless parameters are
     * specifically overridden in the second call.  It's probably not a good idea to call this more than once in most
     * cases; if you just need to add parameters after initialization, use the
     * {@link #addParameter(String, String) addParameter} method.
     * @param params The parameters object containing whatever is needed for the assembler.
     */
    @Override
    public void init(PackageGenerationParameters params) {
        //Checking for required parameters
        this.params = params;

        //validate parameters
        validateParams();

        //retrieve list of checksum algorithms to be performed
        checksumAlgs = params.getParam(BagItParameterNames.CHECKSUM_ALGORITHMS);

        if (checksumAlgs == null) {
            checksumAlgs = new ArrayList<>();
        }
        if (checksumAlgs.isEmpty()) {
            //Indicates the default checksum algorithm that would be used if none is provided.
            String defaultChecksumAlg = "md5";
            checksumAlgs.add(defaultChecksumAlg);
        }

        //retrieve archiving format, if it is set in the input parameters
        if (params.getParam(BagItParameterNames.ARCHIVING_FORMAT) != null &&
                !params.getParam(BagItParameterNames.ARCHIVING_FORMAT).isEmpty()) {
            archivingFormat = params.getParam(BagItParameterNames.ARCHIVING_FORMAT, 0);
            validateArchivingFormat();
        }

        if(archivingFormat.equals("exploded")){
            isExploded = true;
        }

        //retrieve compression format, if it is set in the input parameters
        if (params.getParam(BagItParameterNames.COMPRESSION_FORMAT) != null &&
                !params.getParam(BagItParameterNames.COMPRESSION_FORMAT).isEmpty()) {
            compressionFormat = params.getParam(BagItParameterNames.COMPRESSION_FORMAT, 0);
            validateCompressionFormat();
        }

        //Create a parent dir, must be in a user-controlled location. default will be set here as the users homeDir/packageStaging.
        //This can be overridden in the defaultGenerationParams file
        //This will help prevent deleting data if a user tries to create a package in place.
        String packageStagingLocationName = System.getProperty("user.home") + File.separator + "DCS-PackageToolStaging";
        String packageStagingLocationParameterValue = params.getParam(GeneralParameterNames.PACKAGE_STAGING_LOCATION, 0);
        String packageLocationParameterValue = params.getParam(GeneralParameterNames.PACKAGE_LOCATION,0);

        if(isExploded) {
            if (packageLocationParameterValue != null && !packageLocationParameterValue.isEmpty()) {
                packageStagingLocationName = packageLocationParameterValue;
            }
        } else {
            if (packageStagingLocationParameterValue != null && !packageStagingLocationParameterValue.isEmpty()) {
                packageStagingLocationName = packageStagingLocationParameterValue;
            }
        }
        packageLocationDir = new File(packageStagingLocationName);

        //Creating base directory for the bag based on specified package name
        if (!packageLocationDir.exists()) {
            log.info("Creating bag base dir: " + packageLocationDir.getPath());
            boolean isDirCreated = packageLocationDir.mkdirs();
            if (!isDirCreated) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_DIR_CREATION_EXP,
                        "\nAttempt to create staging directory for the package at \"" + packageLocationDir.getPath() +
                                "\" failed. Possible reasons include: \n" +
                                "- Permission restriction in creating the specified directory \n" +
                                "- \"" + packageLocationDir.getPath() + "\" is not a valid path. For more information about " +
                                "where this value can be set and what it is used for, see release documentation on " +
                                "Package Generation Parameters, Package-Staging-Location parameter in particular.");
            }
        }

        //retrieve package name from params
        //Name of the packageName as specified in the input parameters.
        String packageName = params.getParam(BagItParameterNames.PACKAGE_NAME, 0);

        //Creating base directory
        bagBaseDir = new File(packageLocationDir, packageName);
        //Creating base directory for the bag based on specified package name
        if (!bagBaseDir.exists()) {
            log.info("Creating bag base dir: " + bagBaseDir.getPath());
            boolean isDirCreated = bagBaseDir.mkdirs();
            if (!isDirCreated) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_DIR_CREATION_EXP,
                        "Attempt to create a base directory for bag at " + bagBaseDir.getPath() + " failed.");
            }
        } else {
            //If it exists remove everything in it and start fresh
            try {
                FileUtils.cleanDirectory(bagBaseDir);
            } catch (IOException e) {
                log.warn("Exception thrown when cleaning existing directory: " + e.getMessage());
            }
        }

        //Creating payload directory
        payloadDir = new File(bagBaseDir, "data");
        //Creating payloadDir
        if (!payloadDir.exists()) {
            log.info("Creating payload dir: " + payloadDir.getPath());
            boolean isDirCreated = payloadDir.mkdirs();
            if (!isDirCreated) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_DIR_CREATION_EXP,
                        "Attempt to create a payload directory for bag at " + payloadDir.getPath()+ " failed.");
            }
        }

    }


    /**
     * @param path
     *        Logical file path (including filename) of the resource relative to
     *        the package.
     * @param type the PackageResourceType
     * @return the URI
     */
    @Override
    public URI reserveResource(String path, PackageResourceType type) {
        String decodedPath;
        try {
            decodedPath = new String(URLCodec.decodeUrl(path.getBytes()));

            log.info(("Reserving " + path));

            String fileName = FilePathUtil.sanitizePath(decodedPath);
            File containingDirectory =  null;
            if (type.equals(PackageResourceType.DATA)) {
                containingDirectory = payloadDir;
            } else if (type.equals(PackageResourceType.METADATA)) {
                containingDirectory = bagBaseDir;
            }

            log.debug("Containing dir: " + containingDirectory);
            //Create file from given path
            File newFile = new File(containingDirectory, fileName);

            //Create folders in the path
            if (!newFile.getParentFile().exists()) {
                log.info("Creating parent folders");
                boolean isDirCreated = newFile.getParentFile().mkdirs();
                if (!isDirCreated) {
                    throw new PackageToolException(PackagingToolReturnInfo.PKG_DIR_CREATION_EXP);
                }
            }

            //Remove the package location directory and the slash trailing it.
            URI relativeURI = UriUtility.makeFileUriString(newFile, packageLocationDir);

            fileURIMap.put(relativeURI, newFile.toURI());
            if (type.equals(PackageResourceType.DATA)) {
                dataFiles.add(newFile);
            } else {
                tagFiles.add(newFile);
            }
            return relativeURI;

        } catch (DecoderException | URISyntaxException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_ASSEMBLER_URI_GENERATION_EXP, e);
        }
    }

    @Override
    public void putResource(URI uri, InputStream content) {
        URI resolvableURI = fileURIMap.get(uri);
        File newFile = new File(resolvableURI);
        try {
            OutputStream fileOS = new FileOutputStream(newFile);
            IOUtils.copy(content, fileOS);
            fileOS.close();
        } catch (FileNotFoundException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION, e);
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e);
        }
    }

    /**
     *
     * @param path
     *        Logical file path (including filename) of the resource relative to
     *        the package.
     * @param type
     *        Resource type (e.g. data, metadata, etc).
     * @param content The stream containing the resource's content
     * @return the URI
     */
    @Override
    public URI createResource(String path, PackageResourceType type, InputStream content) {
        URI resourceUri = reserveResource(buildPath(path), type);
        putResource(resourceUri, content);

        return resourceUri;
    }

    /**
     * {@inheritDoc}
     * The process of assembling a BagIt bag includes these following steps:
     * <ul>
     *     <li>Create bagit.txt file</li>
     *     <li>Create bag-info.text file</li>
     *     <li>Calculate checksums for each file in bag</li>
     *     <li>Write manifest files</li>
     *     <li>Write tag-manifest files</li>
     * </ul>
     *
     *
     * @return A {@link org.dataconservancy.packaging.tool.api.Package} object which encapsulate the following elements
     * regarding the package:
     * <ul>
     *     <li> serialize {@code InputStream} to the content of the produced package.</li>
     *     <li> packageName {@code String} value which is the suggested file name for the package file</li>
     *     <li> contentType {@code String} value which indicate the content type of the package content</li>
     * </ul>
     */
    @Override
    public Package assemblePackage() {
        File finalFile;
        Package pkg = null;
        try {

            //Write bag-info.txt
            File bagInfoFile = this.writeBagInfoTxt();
            tagFiles.add(bagInfoFile);

            //Write bagit.txt
            File bagItFile = this.writeBagItTxt();
            tagFiles.add(bagItFile);

            //calculate payload files checksums
            Map<File, List<Checksum>> payloadFileChecksums =
                    checksumService.generatePackageFileChecksums(dataFiles, checksumAlgs);

            //write manifest files.
            for (String alg : checksumAlgs) {
                String manifestName = "manifest-" + alg + ".txt";
                File manifestFile = this.writeManifestFile(alg, payloadFileChecksums, manifestName);
                tagFiles.add(manifestFile);
            }

            //calculate checksums for tag files, including the newly create manifest files.
            Map<File, List<Checksum>> tagFileChecksums =
                    checksumService.generatePackageFileChecksums(tagFiles, checksumAlgs);

            for (String alg: checksumAlgs) {
                String tagManifestName = "tagmanifest-" + alg + ".txt";
                File tagManifestFile = this.writeManifestFile(alg, tagFileChecksums, tagManifestName);
                tagFiles.add(tagManifestFile);
            }

            //if we are exploding the package, we don't create an archive bag
            if(!isExploded) {

                boolean useCompression = (compressionFormat != null && !compressionFormat.isEmpty() && !compressionFormat.equals("none"));

                File archivedBag = this.archiveBag();
                if (useCompression) {
                   finalFile = this.compressFile(archivedBag);
                } else {
                   finalFile = archivedBag;
                }


                String contentType;

                if (useCompression) {
                    contentType = SupportedMimeTypes.getMimeType(compressionFormat);
                } else {
                    contentType = SupportedMimeTypes.getMimeType(archivingFormat);
                }


                pkg = new org.dataconservancy.packaging.tool.impl.PackageImpl(finalFile, finalFile.getName(), contentType);

                //remove unneeded files
                FileUtils.deleteDirectory(this.bagBaseDir);
                if (useCompression) {
                    FileUtils.forceDelete(archivedBag);
                }
            }
        } catch (IOException e) {
            log.warn("Could not removed files and/or directory created during package generation." + e.getMessage());
        }

        return pkg;
    }

    @Override
    public void addParameter(String key, String value) {
        if (params == null) {
            params = new PackageGenerationParameters();
        }

        params.addParam(key, value);
    }

    private File writeManifestFile(String alg, Map<File, List<Checksum>> fileChecksums, String fileName)
            throws PackageToolException {
        File manifestFile = new File(bagBaseDir, fileName);
        try {
            FileWriter writer = new FileWriter(manifestFile);
            String newLine = System.getProperty("line.separator");
            String lineFormat = "%s  %s";

            Set<File> files = fileChecksums.keySet();

            for (File file : files) {
                List<Checksum> checksums = fileChecksums.get(file);

                final String filePath = FilenameUtils.separatorsToUnix(Paths.get(bagBaseDir.getPath()).relativize(Paths.get(file.getPath())).toString());
                for (Checksum checksum : checksums) {
                    if (checksum.getAlgorithm().equals(alg)) {
                        writer.write(String.format(lineFormat, checksum.getValue(),
                                                   filePath + newLine));
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e,
                    "Exception occurred when writing one of the manifest files.");
        }
        return manifestFile;
    }

    private File writeBagInfoTxt() throws PackageToolException {
        File bagInfoFile = new File(bagBaseDir, "bag-info.txt");
        try {
            FileWriter writer = new FileWriter(bagInfoFile);
            String newLine = System.getProperty("line.separator");
            String lineFormat = "%s: %s ";

            Set<String> bagInfoFields = params.getKeys();
            for (String field : bagInfoFields) {
                List<String> fieldValues = params.getParam(field);
                for (String value : fieldValues) {
                    writer.write(String.format(lineFormat, field, value) + newLine);
                }
            }

            writer.write(String.format(lineFormat, BagItParameterNames.BAG_SIZE,
                    FileUtils.byteCountToDisplaySize(FileUtils.sizeOfDirectory(bagBaseDir))) + newLine);
            writer.write(String.format(lineFormat, BagItParameterNames.PAYLOAD_OXUM,
                    FileUtils.sizeOfDirectory(payloadDir) + "." + dataFiles.size()) + newLine);
            writer.write(String.format(lineFormat, BagItParameterNames.BAGGING_DATE, (new DateTime().toDate()) + newLine));

            writer.close();
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e,
                    "Exception occurred when writing bag-info.txt file.");
        }

        return bagInfoFile;
    }

    private File writeBagItTxt() throws PackageToolException {
        File bagItFile = new File(bagBaseDir, "bagit.txt");
        try {
            FileWriter writer = new FileWriter(bagItFile);
            String newLine = System.getProperty("line.separator");
            String lineFormat = "%s: %s ";

            writer.write(String.format(lineFormat, BagItParameterNames.BAGIT_VERSION, VERSION) + newLine);
            writer.write(String.format(lineFormat, BagItParameterNames.TAG_FILE_CHAR_ENCODING, ENCODING) + newLine);

            writer.close();

        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e,
                    "Exception occurred when writing bagit.txt file.");
        }
        return bagItFile;
    }

    /**
     * According to Usecase DCS-TXT-10, BagIt bags that include ORE ReM will contain "PKG-BAG-DIR", which is "a string
     * consisting of a valid Unix directory name that, if specified, will be treated as the top-level bag directory
     * name." Base on this assertion, path used to create resources will be relative to this PKG-BAG-DIR.
     * @param string the string used to build the path
     * @return  a String representing the path
     */
    private String buildPath(String string) {

        String rootLocation = params.getParam(GeneralParameterNames.CONTENT_ROOT_LOCATION,0);

        File file;
        if(rootLocation != null) {
            String contentRoot = FilenameUtils.separatorsToUnix(rootLocation);
            file = new File(contentRoot, string);
        }
        else
        {
            file = new File(string);
        }

        if(!file.exists()){
               throw new PackageToolException(PackagingToolReturnInfo.PKG_ASSEMBLER_STRAY_FILE,
                    "Provided file path indicates that that file " + file.getPath() + " does not reside within the " +
                    "specified content root location of: " + params.getParam(GeneralParameterNames.CONTENT_ROOT_LOCATION,0));

        }

      //return file.getPath();
        return string;
    }

    private File archiveBag() throws PackageToolException {
        File archivedFile = new File(packageLocationDir, bagBaseDir.getName() + "." + archivingFormat);
        try {
            FileOutputStream fos = new FileOutputStream(archivedFile);
            ArchiveOutputStream aos = new ArchiveStreamFactory()
                    .createArchiveOutputStream(archivingFormat, new BufferedOutputStream(fos));
            if (aos instanceof TarArchiveOutputStream) {
                ((TarArchiveOutputStream) aos).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            }
            // Get to putting all the files in the compressed output file
            for (File f : bagBaseDir.listFiles()) {
                //To support the cancelling of package creation we check here to see if the thread has been interrupted.
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
                addFilesToArchive(aos, f);
            }
            aos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION, e,
                    "Exception occurred when serializing the bag.");
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e,
                    "Exception occurred when serializing the bag.");
        } catch (ArchiveException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_ASSEMBLER_ARCHIVE_EXP, e,
                    "Archiving format \"" + archivingFormat+ "\" is not supported.");
        }

        return archivedFile;
    }

    private void addFilesToArchive(ArchiveOutputStream taos, File file) throws IOException {
        // Create an entry for the file
        //taos.putArchiveEntry(new TarArchiveEntry(file, file.getParentFile().toURI().relativize(file.toURI()).toString()));
        switch (archivingFormat) {
            case ArchiveStreamFactory.TAR:
                taos.putArchiveEntry(new TarArchiveEntry(file, FilenameUtils.separatorsToUnix(Paths.get(packageLocationDir.getPath()).relativize(Paths.get(file.getPath())).toString())));
                break;
            case ArchiveStreamFactory.ZIP:
                taos.putArchiveEntry(new ZipArchiveEntry(file, FilenameUtils.separatorsToUnix(Paths.get(packageLocationDir.getPath()).relativize(Paths.get(file.getPath())).toString())));
                break;
            case ArchiveStreamFactory.JAR:
                taos.putArchiveEntry(new JarArchiveEntry(new ZipArchiveEntry(file, FilenameUtils.separatorsToUnix(Paths.get(packageLocationDir.getPath()).relativize(Paths.get(file.getPath())).toString()))));
                break;
            case ArchiveStreamFactory.AR:
                taos.putArchiveEntry(new ArArchiveEntry(file, FilenameUtils.separatorsToUnix(Paths.get(packageLocationDir.getPath()).relativize(Paths.get(file.getPath())).toString())));
                break;
            case ArchiveStreamFactory.CPIO:
                taos.putArchiveEntry(new CpioArchiveEntry(file, FilenameUtils.separatorsToUnix(Paths.get(packageLocationDir.getPath()).relativize(Paths.get(file.getPath())).toString())));
                break;
        }
        if (file.isFile()) {
            // Add the file to the archive
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(bis, taos);
            taos.closeArchiveEntry();
            bis.close();
        } else if (file.isDirectory()) {
            // close the archive entry
            taos.closeArchiveEntry();
            // go through all the files in the directory and using recursion, add them to the archive
            for (File childFile : file.listFiles()) {
                addFilesToArchive(taos, childFile);
            }
        }
    }

    /**
     * Produce a compressed file from input file.
     * The produced compressed file will be placed at the specified package-location.
     * @param file the input file
     * @return  the compressed file
     * @throws PackageToolException
     */
    private File compressFile(File file) throws PackageToolException {
        if (compressionFormat != null) {
            File compressedFile = new File(packageLocationDir, file.getName()+ "." + compressionFormat);
            try {
                CompressorOutputStream compressedStream = new CompressorStreamFactory()
                        .createCompressorOutputStream(compressionFormat, new FileOutputStream(compressedFile));
                IOUtils.copy(new FileInputStream(file), compressedStream);
                compressedStream.close();
            } catch (FileNotFoundException e) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_FILE_NOT_FOUND_EXCEPTION, e,
                        "Exception occurred when compressing the serialized bag.");
            } catch (IOException e) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_IO_EXCEPTION, e,
                        "Exception occurred when compressing the serialized bag.");
            } catch (CompressorException e) {
                throw new PackageToolException(PackagingToolReturnInfo.PKG_ASSEMBLER_COMPRESSION_EXP, e,
                        "Compression format \"" + compressionFormat + "\" is not supported.");
            }

            return compressedFile;
        } else return file;
    }


    /**
     * Returns a resolvable File URI given a package relative URI.
     * @param relativeURI  a package relative URI
     * @return  a resolvable File URI
     */
    public URI getResolvableURI(URI relativeURI) {
        return fileURIMap.get(relativeURI);
    }

    /**
     * Required params include :
     *
     * <ul>
     *     <li>package-name</li>
     *     <li>package-location</li>
     *     <li>Bag-It-Profile-Identifier</li>
     *     <li>If Packaging format is BOREM, then PKG-BAG-DIR param is expected (according to DCS-TXT-10)</li>
     *     <li>Contact-Name</li>
     *     <li>Contact-Phone</li>
     *     <li>Contact-Email</li>
     * </ul>
     */
    public void validateParams() {
        if (params == null) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_ASSEMBLER_PARAMS_NOT_INITIALIZED_EXP,
                  requiredParametersMessage);
        }

        Set<String> paramNames = params.getKeys();
        List<String> missingParams = new ArrayList<>();

        if (!paramNames.contains(BagItParameterNames.PACKAGE_NAME)) {
            missingParams.add(BagItParameterNames.PACKAGE_NAME);
        }

        if (!paramNames.contains(BagItParameterNames.PACKAGE_LOCATION)) {
            missingParams.add(BagItParameterNames.PACKAGE_LOCATION);
        }

        if (!paramNames.contains(BagItParameterNames.BAGIT_PROFILE_ID)) {
            missingParams.add(BagItParameterNames.BAGIT_PROFILE_ID);
        }

        if (params.getParam(BagItParameterNames.PACKAGE_FORMAT_ID, 0).equals(PackagingFormat.BOREM.toString())
                && !paramNames.contains(BagItParameterNames.PKG_BAG_DIR)) {
            missingParams.add(BagItParameterNames.PKG_BAG_DIR);
        }

        if (!missingParams.isEmpty()) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_REQUIRED_PARAMS_MISSING,
                    "Parameter(s) " + missingParams.toString() + " are missing. "
                            + requiredParametersMessage);
        }

    }

    private void validateArchivingFormat() {
        if (!archivingFormat.equals(ArchiveStreamFactory.CPIO)
                && !archivingFormat.equals(ArchiveStreamFactory.TAR)
                && !archivingFormat.equals(ArchiveStreamFactory.JAR)
                && !archivingFormat.equals(ArchiveStreamFactory.AR)
                && !archivingFormat.equals(ArchiveStreamFactory.ZIP)
                && !archivingFormat.equals("exploded")) {
            throw new PackageToolException ( PackagingToolReturnInfo.PKG_ASSEMBLER_INVALID_PARAMS,
                    String.format("Specified archiving format <%s> is not supported. The supported archiving " +
                            "formats are: %s, %s, %s, %s, %s, %s.",
                            archivingFormat,
                            ArchiveStreamFactory.AR, ArchiveStreamFactory.CPIO, ArchiveStreamFactory.JAR,
                            ArchiveStreamFactory.TAR, ArchiveStreamFactory.ZIP, "exploded"));
        }
    }
    private void validateCompressionFormat() {
        // convert gzip to gz
        if (compressionFormat.equals("gzip")) {
            compressionFormat = CompressorStreamFactory.GZIP;
        }

        if (!compressionFormat.equals(CompressorStreamFactory.BZIP2)
                && !compressionFormat.equals(CompressorStreamFactory.GZIP)
                && !compressionFormat.equals(CompressorStreamFactory.PACK200)
                && !compressionFormat.equals("none")) {
            throw new PackageToolException ( PackagingToolReturnInfo.PKG_ASSEMBLER_INVALID_PARAMS,
                    String.format("Specified compression format %s is not supported. The supported compression " +
                            "formats are: %s (or %s), %s, %s, none.",
                            compressionFormat,
                            CompressorStreamFactory.GZIP, "gzip", CompressorStreamFactory.BZIP2, CompressorStreamFactory.PACK200));
        }
    }
}
