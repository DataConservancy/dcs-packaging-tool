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
package org.dataconservancy.dcs.util.droid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.nationalarchives.droid.container.ContainerFileIdentificationRequestFactory;
import uk.gov.nationalarchives.droid.container.ContainerSignatureSaxParser;
import uk.gov.nationalarchives.droid.container.ole2.Ole2Identifier;
import uk.gov.nationalarchives.droid.container.ole2.Ole2IdentifierEngine;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifier;
import uk.gov.nationalarchives.droid.container.zip.ZipIdentifierEngine;
import uk.gov.nationalarchives.droid.core.BinarySignatureIdentifier;
import uk.gov.nationalarchives.droid.core.interfaces.*;
import uk.gov.nationalarchives.droid.core.interfaces.archive.*;
import uk.gov.nationalarchives.droid.core.interfaces.resource.FileSystemIdentificationRequest;
import uk.gov.nationalarchives.droid.core.interfaces.resource.RequestMetaData;
import uk.gov.nationalarchives.droid.core.interfaces.signature.SignatureManager;
import uk.gov.nationalarchives.droid.core.signature.FileFormat;
import uk.gov.nationalarchives.droid.core.signature.droid6.FFSignatureFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;

/**
 * Wrapper class for the DROID API. 
 *
 */
public class DroidIdentifier {
    
    private final Logger log = LoggerFactory.getLogger(DroidIdentifier.class);

    private String signatureFilePath;
    private String containerSignatureFilePath;
    private BinarySignatureIdentifier droid;
    private DroidSignatureFileManager fileManager;
    private ContainerIdentifierFactory containerIdentifierFactory;
    private SignatureManager signatureManager;
    private ArchiveFormatResolver containerFormatResolver;
    private IdentificationRequest identificationRequest;
    private boolean initialized;
    
    // This is set to -1 so the entire file is always scanned.
    private int maxBytes = -1;

    public DroidIdentifier() {
        if (!initialized) {
            initializeDroid();
        }
    }

    /**
     * Identifies the format of a given file.
     * 
     * @param file the file to be processed
     * @return collection of identification results
     */
    public IdentificationResultCollection detectFormat(File file) {

        openIdentificationRequest(file);
        droid.setMaxBytesToScan(maxBytes);
        IdentificationResultCollection results = droid.matchBinarySignatures(identificationRequest);
        results = processContainerResults(results);
        droid.removeLowerPriorityHits(results);
        if (results.getResults() != null && results.getResults().isEmpty()) {
            // last resort check via file extension.
            results = processExtensions(results);
        }
        droid.checkForExtensionsMismatches(results, identificationRequest.getExtension());
        closeIdentificationRequest();
        return results;
    }

    /**
     * Look up file formats applicable to the provided file extension
     * @param extension the file extension
     * @return List of applicable file formats
     */
    public List<FileFormat> getFileFormatByExtension(String extension) {
        try {
            return retrieveSigFileDetails().getFileFormatsForExtension(extension);
        } catch (Exception e) {
            log.error("Could not retrieve Droid Signature file to looking file format for given extension \""
                    + extension + "\"", e);
        }
        return null;
    }
    
    /**
     * Helper method that looks through the result and processes any container formats. If there are none, it just
     * returns the results as-is.
     * 
     * @param results  the collection of identification results
     * @return processed results
     */
    private IdentificationResultCollection processContainerResults(IdentificationResultCollection results) {
        IdentificationResultCollection containerResults = determineContainerFormats(results);
        
        if (containerResults == null) {
            return results;
        }
        else {
            for (IdentificationResult identificationResult : results.getResults()) {
                try {
                    FileFormat fileFormat = retrieveSigFileDetails().getFileFormat(identificationResult.getPuid());
                    IdentificationResultImpl result = new IdentificationResultImpl();
                    result.setMimeType(fileFormat.getMimeType());
                    result.setName(fileFormat.getName());
                    result.setVersion(fileFormat.getVersion());
                    result.setPuid(fileFormat.getPUID());
                    result.setMethod(IdentificationMethod.CONTAINER);
                    result.setRequestMetaData(identificationResult.getMetaData());
                    containerResults.removeResult(identificationResult);
                    containerResults.addResult(result);
                }
                catch (Exception e) {
                    log.error("Could not get the fileFormat.", e);
                }
                
            }
            return containerResults;
        }
    }
    
    /**
     * Identifies a container file's format.
     * 
     * @param results the collection of identification results
     * @return processed results
     */
    private IdentificationResultCollection determineContainerFormats(IdentificationResultCollection results) {
        
        for (IdentificationResult identificationResult : results.getResults()) {
            String format = containerFormatResolver.forPuid(identificationResult.getPuid());
            if (format != null) {
                try {
                    ContainerIdentifier containerIdentifier = containerIdentifierFactory.getIdentifier(format);
                    containerIdentifier.setMaxBytesToScan(maxBytes);
                    IdentificationResultCollection containerResults = containerIdentifier.submit(identificationRequest);
                    if (containerResults.getResults().size() > 0) {
                        droid.removeLowerPriorityHits(containerResults);
                        droid.checkForExtensionsMismatches(containerResults, format);
                        containerResults.setFileLength(identificationRequest.size());
                        containerResults.setRequestMetaData(identificationRequest.getRequestMetaData());
                        return containerResults;
                    }
                    else {
                        return null;
                    }
                }
                catch (IOException e) {
                    log.error("Could not determine the container formats.", e);
                    return null;
                }
            }
        }
        return null;
    }
    
    /**
     * Tries to identify the file using its extension as a last resort. This should only be called as a last resort.
     * 
     * @param results   the collection of identification results
     * @return processed results
     */
    private IdentificationResultCollection processExtensions(IdentificationResultCollection results) {
        return droid.matchExtensions(identificationRequest, false);
    }

    /**
     * Helper method that initializes DroidIdentifier and gets it ready for identification.
     */
    private void initializeDroid() {
        try {
            setSignatureFiles();
            droid = new BinarySignatureIdentifier();
            droid.setSignatureFile(signatureFilePath);
            droid.init();
            
            containerIdentifierFactory = new ContainerIdentifierFactoryImpl();
            containerFormatResolver = new ArchiveFormatResolverImpl();
            
            Ole2Identifier ole2Identifier = new Ole2Identifier();
            ZipIdentifier zipIdentifier = new ZipIdentifier();
            
            ole2Identifier.setSignatureFilePath(containerSignatureFilePath);
            ole2Identifier.setSignatureFileParser(new ContainerSignatureSaxParser());
            ole2Identifier.setContainerType("OLE2");
            ole2Identifier.setDroidCore(droid);
            ole2Identifier.setContainerIdentifierFactory(containerIdentifierFactory);
            ole2Identifier.setContainerFormatResolver(containerFormatResolver);
            Ole2IdentifierEngine ole2IdentifierEngine = new Ole2IdentifierEngine();
            ole2IdentifierEngine.setRequestFactory(new ContainerFileIdentificationRequestFactory());
            ole2Identifier.setIdentifierEngine(ole2IdentifierEngine);
            ole2Identifier.init();
            
            zipIdentifier.setSignatureFilePath(containerSignatureFilePath);
            zipIdentifier.setSignatureFileParser(new ContainerSignatureSaxParser());
            zipIdentifier.setContainerType("ZIP");
            zipIdentifier.setDroidCore(droid);
            zipIdentifier.setContainerIdentifierFactory(containerIdentifierFactory);
            zipIdentifier.setContainerFormatResolver(containerFormatResolver);
            ZipIdentifierEngine zipIdentifierEngine = new ZipIdentifierEngine();
            zipIdentifierEngine.setRequestFactory(new ContainerFileIdentificationRequestFactory());
            zipIdentifier.setIdentifierEngine(zipIdentifierEngine);
            zipIdentifier.init();
            initialized = true;
        }
        catch (Exception e) {
            log.error("Could not initialize Droid.", e);
        }
    }
    
    /**
     * Helper method to open an identification request.
     */
    private void openIdentificationRequest(File file) {
        try {
            URI resourceUri = file.toURI();
            RequestMetaData metadata = new RequestMetaData(file.length(), file.lastModified(), file.getName());
            RequestIdentifier requestIdentifier = new RequestIdentifier(resourceUri);
            identificationRequest = new FileSystemIdentificationRequest(metadata, requestIdentifier);
            FileInputStream inputStream = new FileInputStream(file);
            identificationRequest.open(inputStream);
        }
        catch (Exception e) {
            log.error("Could not open identification request.", e);
        }
    }
    
    /**
     * helper method to close an identification request.
     */
    private void closeIdentificationRequest() {
        try {
            identificationRequest.close();
        }
        catch (IOException e) {
            log.error("Could not close identification request.", e);
        }
    }

    /**
     * Helper method that initializes DroidSignatureFile and sets signature files.
     */
    private void setSignatureFiles() {
        fileManager = new DroidSignatureFileManager();
        
        File signatureFile = fileManager.getLatestSignatureFile();
        if (signatureFile != null) {
            signatureFilePath = signatureFile.getPath();
        }

        File containerFile = fileManager.getLatestContainerFile();
        if (containerFile != null) {
            containerSignatureFilePath = containerFile.getPath();
        }
    }

    private FFSignatureFile retrieveSigFileDetails() throws Exception {
        Field privateField = BinarySignatureIdentifier.class.getDeclaredField("sigFile");
        privateField.setAccessible(true);
        return (FFSignatureFile) privateField.get(droid);
    }
}
