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
 * Created by jrm on 10/20/15.
 */
public class PackageMetadataService {


    private List<PackageMetadata> required;
    private List<PackageMetadata> recommended;
    private List<PackageMetadata> optional;

    private String packageMetadataParametersFile;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public PackageMetadataService(Configuration configuration) throws IOException{
        packageMetadataParametersFile = configuration.getPackageMetadataParametersFile();
        createPackageMetadata(configuration.getConfigurationFileInputStream(packageMetadataParametersFile));
    }

    public List<PackageMetadata> getRequiredPackageMetadata(){
        return required;
    }

    public List<PackageMetadata> getRecommendedPackageMetadata(){
        return recommended;
    }

    public List<PackageMetadata> getOptionalPackageMetadata(){
        return optional;
    }

    public List<PackageMetadata> getAllPackageMetadata(){
        List<PackageMetadata> allPackageMetadata = new ArrayList<>();
        allPackageMetadata.addAll(required);
        allPackageMetadata.addAll(recommended);
        allPackageMetadata.addAll(optional);
        return allPackageMetadata;
    }

    private void createPackageMetadata(InputStream inputStream) {

        required = new ArrayList<>();
        recommended = new ArrayList<>();
        optional = new ArrayList<>();

        if (inputStream != null) {

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(PackageMetadataList.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                PackageMetadataList pmList = (PackageMetadataList) jaxbUnmarshaller.unmarshal(inputStream);

                for (PackageMetadata pm : pmList.getPackageMetadatalist()) {
                    if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.REQUIRED)) {
                        required.add(pm);
                    } else if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.RECOMMENDED)) {
                        recommended.add(pm);
                    } else if (pm.getRequiredness()!= null && pm.getRequiredness().equals(PackageMetadata.Requiredness.OPTIONAL)){
                        optional.add(pm);
                    } else {
                        pm.setRequiredness(PackageMetadata.Requiredness.OPTIONAL);
                        optional.add(pm);
                    }

                    if(pm.getValidationType() == null){
                        pm.setValidationType(PackageMetadata.ValidationType.NONE);
                    }
                }
            } catch (JAXBException e) {
                log.error("Error processing package metadata from file " + packageMetadataParametersFile);

            }
        }
    }
}
