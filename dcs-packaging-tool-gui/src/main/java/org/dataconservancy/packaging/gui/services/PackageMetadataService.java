package org.dataconservancy.packaging.gui.services;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.dataconservancy.packaging.gui.Configuration;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.ParametersBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
        createPackageModels(configuration.getConfigurationFileInputStream(packageMetadataParametersFile));
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

    private void createPackageModels(InputStream fileStream){
        required = new ArrayList<PackageMetadata>();
        recommended = new ArrayList<PackageMetadata>();
        optional = new ArrayList<PackageMetadata>();

        if (fileStream != null) {
            try {
                createPackageMetadata(fileStream);
            } catch (ParametersBuildException e) {
            log.error("Error building package metadata from file: " + packageMetadataParametersFile);
            }
        }
        else {;
            log.error("Error reading package metadata parameters file. Couldn't find classpath file: " + packageMetadataParametersFile);
        }

    }

    private void createPackageMetadata(InputStream inputStream) throws ParametersBuildException {

        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            props.load(new InputStreamReader(inputStream));
        } catch (ConfigurationException e) {
            throw new ParametersBuildException(e);
        }

        Iterator<String> keys = props.getKeys();
        while (keys.hasNext()) {
            PackageMetadata packageMetadata = new PackageMetadata();
            String key = keys.next();
            List<String> values = Arrays.asList(props.getStringArray(key));
            packageMetadata.setName(key);
            packageMetadata.setEditable((values.contains("Editable")));
            packageMetadata.setMaxOccurrence((values.contains("Repeatable") ? Integer.MAX_VALUE : 1));

            PackageMetadata.ValidationType type;
            if (values.contains("Phone")) {
                type = PackageMetadata.ValidationType.PHONE;
            } else if (values.contains("Email")) {
                type = PackageMetadata.ValidationType.EMAIL;
            } else if (values.contains("URL")) {
                type = PackageMetadata.ValidationType.URL;
            } else if (values.contains("Date")) {
                type = PackageMetadata.ValidationType.DATE;
            } else if (values.contains("File")) {
                type = PackageMetadata.ValidationType.FILENAME;
            } else {
                type = PackageMetadata.ValidationType.NONE;
            }
            packageMetadata.setValidationType(type);

            if (values.contains("Required")) {
                packageMetadata.setMinOccurrence(1);
                required.add(packageMetadata);
            } else if (values.contains("Recommended")) {
                packageMetadata.setMinOccurrence(0);
                recommended.add(packageMetadata);
            } else {
                packageMetadata.setMinOccurrence(0);
                optional.add(packageMetadata);
            }
        }
    }
}
