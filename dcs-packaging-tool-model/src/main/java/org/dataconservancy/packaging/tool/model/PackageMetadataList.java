package org.dataconservancy.packaging.tool.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by jrm on 10/21/15.
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
