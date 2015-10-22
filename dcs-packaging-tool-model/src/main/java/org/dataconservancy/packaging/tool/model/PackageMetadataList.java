/*
* Copyright 2015 Johns Hopkins University
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
