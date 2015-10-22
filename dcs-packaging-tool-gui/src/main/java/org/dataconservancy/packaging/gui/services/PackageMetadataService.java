package org.dataconservancy.packaging.gui.services;

import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.PackageMetadataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.List;

/**
 * A service for setting up the PackageMetadata objects from an xml file. The PackageMetadata objects will be processed in the
 * order they occur in the xml file. For convenience, methods are provided to retrieve Lists of PackageMetadata objects according
 * to their requiredness, as the GUI will handle each requiredness chunk separately.
 */
public class PackageMetadataService {

    private List<PackageMetadata> required;
    private List<PackageMetadata> recommended;
    private List<PackageMetadata> optional;
    private List<PackageMetadata> all;

    private String packageMetadataParametersFile;
    private String packageMetadataParametersFilePath;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public PackageMetadataService(Configuration configuration) throws IOException{
        packageMetadataParametersFile = configuration.getPackageMetadataParametersFile();
        packageMetadataParametersFilePath = configuration.resolveConfigurationFile(packageMetadataParametersFile);
        createPackageMetadata(configuration.getConfigurationFileInputStream(packageMetadataParametersFilePath));
    }

    /**
     * Getter for required package metadata
     * @return  a List of required PackageMetadata objects, in the order they occur in the xml file
     */
    public List<PackageMetadata> getRequiredPackageMetadata(){
        return required;
    }

    /**
     * Getter for recommended package metadata
     * @return  a List of recommended PackageMetadata objects, in the order they occur in the xml file
     */
    public List<PackageMetadata> getRecommendedPackageMetadata(){
        return recommended;
    }

    /**
     * Getter for optional package metadata
     * @return  a List of optional PackageMetadata objects, in the order they occur in the xml file
     */
    public List<PackageMetadata> getOptionalPackageMetadata(){
        return optional;
    }

    /**
     * Getter for all package metadata
     * @return  a List of all PackageMetadata objects, in the order they occur in the xml file
     */
    public List<PackageMetadata> getAllPackageMetadata(){ return all; }

    private void createPackageMetadata(InputStream inputStream) {
        required = new ArrayList<>();
        recommended = new ArrayList<>();
        optional = new ArrayList<>();
        all = new ArrayList<>();

        if (inputStream != null) {
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(PackageMetadataList.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                PackageMetadataList pmList = (PackageMetadataList) jaxbUnmarshaller.unmarshal(inputStream);

                for (PackageMetadata pm : pmList.getPackageMetadatalist()) {

                    if(pm.getValidationType() == null){
                        //make sure this field is set - invalid or missing value
                        //will get set to NONE
                        log.warn("Missing or invalid validationType for PackageMetadata " +
                        pm.getName() + ", setting validationType to NONE");
                        pm.setValidationType(PackageMetadata.ValidationType.NONE);
                    }

                    if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.REQUIRED)) {
                        required.add(pm);
                    } else if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.RECOMMENDED)) {
                        recommended.add(pm);
                    } else if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.OPTIONAL)){
                        optional.add(pm);
                    } else {
                        //make sure this field is set - invalid or missing value
                        //will get set to OPTIONAL
                        log.warn("Missing or invalid requiredness for PackageMetadata " +
                        pm.getName() + ", setting requiredness to OPTIONAL");
                        pm.setRequiredness(PackageMetadata.Requiredness.OPTIONAL);
                        optional.add(pm);
                    }
                    all.add(pm);
                }
            } catch (JAXBException e) {
                log.error("Error processing package metadata from file " + packageMetadataParametersFilePath);
            }
        }
    }
}
