package org.dataconservancy.packaging.gui.services;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.dataconservancy.packaging.tool.model.PackageMetadata;
import org.dataconservancy.packaging.tool.model.ParametersBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by jrm on 10/20/15.
 */
public class PackageMetadataService {

    public PackageMetadataService(String packageMetadataParametersFile) throws IOException{
        this.packageMetadataParametersFile = packageMetadataParametersFile;
        init(createInputStream(packageMetadataParametersFile));
    }

    private List<PackageMetadata> required;
    private List<PackageMetadata> recommended;
    private List<PackageMetadata> optional;

    private String packageMetadataParametersFile;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<PackageMetadata> getRequiredPackageMetadata(){
        return required;
    }

    public List<PackageMetadata> getRecommendedPackageMetadata(){
        return recommended;
    }

    public List<PackageMetadata> getOptionalPackageMetadata(){
        return optional;
    }

     private void init(InputStream fileStream){
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
        else {
            System.out.println("Could not find classpath file: " + packageMetadataParametersFile);
            log.error("Error reading package metadata parameters file. Couldn't find classpath file: " + packageMetadataParametersFile);
        }

    }

    private InputStream createInputStream(String filePath) {
        InputStream fileStream = null;
        try {
            if (filePath.startsWith("classpath:")) {
                String path = filePath.substring("classpath:".length());
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                fileStream = this.getClass().getResourceAsStream(path);
               // URL url = this.getClass().getResource(path);
                //fileStream = url.openStream();
            } else {
                fileStream = new FileInputStream(filePath);
            }
        } catch(IOException e){
           log.error("Unable to open file " + filePath);
        }
        return fileStream;
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
            List<String> values =   Arrays.asList(props.getStringArray(key));
            packageMetadata.setName(key);
            packageMetadata.setEditable((values.contains("E")));
            packageMetadata.setMaxOccurrence((values.contains("X") ? Integer.MAX_VALUE : 1));

            if(values.contains("M")){
               packageMetadata.setMinOccurrence(0);
                recommended.add(packageMetadata);
            } else if(values.contains("Q")){
                packageMetadata.setMinOccurrence(1);
                required.add(packageMetadata);
            } else {
                packageMetadata.setMinOccurrence(0);
                optional.add(packageMetadata);
            }
        }

    }
}
