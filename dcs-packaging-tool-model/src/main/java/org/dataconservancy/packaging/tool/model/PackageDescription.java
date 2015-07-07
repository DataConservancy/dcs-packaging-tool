/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model;

import java.util.HashSet;import java.util.Set;

public class PackageDescription {
    
    private Set<PackageArtifact> packageArtifacts;
    private String packageOntologyIdentifier;
    private ArtifactReference rootArtifactRef;
    
    public PackageDescription() {
        packageArtifacts = new HashSet<>();
    }

    /**
     * A string identifier which point to a package ontology specification. 
     *
     * Package ontology specification is expected to specify requirements about the artifacts found in
     * the package. For example: a Project artifact has to have a title field, an id field, etc.
     * @return The string representing the package identifier.
     */
    public String getPackageOntologyIdentifier() {
        return packageOntologyIdentifier;
    }
 
    public void setPackageOntologyIdentifier(String identifier) {
        this.packageOntologyIdentifier = identifier;
    }
    
    /**
     * Collections of properties describing the data within the package
     * @return The set of package artifacts that make up the package.
     */
    public Set<PackageArtifact> getPackageArtifacts() {
        return packageArtifacts;        
    }
    
    public void setPackageArtifacts(Set<PackageArtifact> artifacts) {
        this.packageArtifacts = artifacts;
    }
    
    /**
     * Sets the ref of the root artifact of the package description using the ArtifactReference String constructor.
     * The string must represent a valid URI
     * @param rootArtifactString The reference pointing to the root artifact of the description.
     */
    public void setRootArtifactRef(String rootArtifactString) { this.rootArtifactRef = new ArtifactReference(rootArtifactString);}

    /**
     * Sets the root ArtifactReference of the package description.
     * @param rootArtifact
     */
    public void setRootArtifactRef(ArtifactReference rootArtifact) {this.rootArtifactRef = rootArtifact;}

    /**
     * Gets the reference of the root artifact of the package description if one exists.
     * @return The reference pointing to the root artifact of the description.
     */
    public ArtifactReference getRootArtifactRef() {
        return rootArtifactRef;
    }

    /**
     * Convenience method for fetching the root artifact.
     * @return The package artifact that is the root of the description, or null if the root isn't set or can't be found.
     */
    public PackageArtifact getRootArtifact() {
        PackageArtifact root = null;
        if (rootArtifactRef != null) {
            for (PackageArtifact artifact : packageArtifacts) {
                if (artifact.getArtifactRef().equals(rootArtifactRef)) {
                    root = artifact;
                    break;
                }
            }
        }
        return root;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackageDescription)) return false;

        PackageDescription other = (PackageDescription) o;
        
        if (packageOntologyIdentifier == null) {
            if (other.packageOntologyIdentifier != null)
                return false;
        } else if (!packageOntologyIdentifier.equals(other.packageOntologyIdentifier))
            return false;
        if (packageArtifacts == null) {
            if (other.packageArtifacts != null)
                return false;
        } else if (!packageArtifacts.equals(other.packageArtifacts))
            return false;
        if (rootArtifactRef == null) {
            if (other.rootArtifactRef != null)
                return false;
        } else if (!rootArtifactRef.equals(other.rootArtifactRef))
            return false;
      

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((packageOntologyIdentifier == null) ? 0 : packageOntologyIdentifier.hashCode());
        result = prime * result + ((packageArtifacts == null) ? 0 : packageArtifacts.hashCode());
        result = prime * result + ((rootArtifactRef == null) ? 0 : rootArtifactRef.hashCode());
        return result;
    }
    
    public String toString() {
        return "PackageDescription{" + "ontologyIdentifier='" + packageOntologyIdentifier + '\'' + ", packageArtifacts='" + packageArtifacts + ", rootArtifactRef='" + rootArtifactRef + '}';
    }
}
