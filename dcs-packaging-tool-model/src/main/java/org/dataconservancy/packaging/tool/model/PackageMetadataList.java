package org.dataconservancy.packaging.tool.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This is a wrapper class which allows us to pull in the configuration for the List of PackageMetadata elements
 * using jaxb in the PackageMetadataService
 */
@XmlRootElement(name="packageMetadataList")
@XmlAccessorType(XmlAccessType.FIELD)
public class PackageMetadataList {
    @XmlElement(name = "packageMetadata")
    private List<PackageMetadata> packageMetadatalist = null;

    public List<PackageMetadata>  getPackageMetadatalist(){
        return packageMetadatalist;
    }

    public void setPackageMetadatalist(List<PackageMetadata> packageMetadatalist){
          this.packageMetadatalist = packageMetadatalist;
    }
}
