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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *  A class to implement the functionality we need for a PackageArtifact's artifactRef.
 *
 */
public class ArtifactReference {

    /**
     * The fragment is the specifier generated for a virtual artifact. When this is not null
     * the reference does not point to an actual file system entity
     */
    private String fragment = null;

    /**
     * The String representing the artifact reference
     */
    private String refPath;

    public ArtifactReference(String refPath) {
       this.refPath=refPath;
    }

    /**
     *
     * @param contentRootFile The Content Root directory. This is the parent of the
     *                        package's root artifact location.
     * @return A String representing the absolute path pointing to the artifact's File
     */
    public String getResolvedAbsoluteRefString(File contentRootFile) {
        File absFile = new File(contentRootFile, getRefString());
        return absFile.getPath();
    }

    /**
     *
     * @param contentRootFile The Content Root directory. This is the parent of the
     *                        package's root artifact
     * @return The Path pointing to the artifact's File
     */
    public Path getResolvedAbsoluteRefPath(File contentRootFile) {
        return Paths.get(contentRootFile.getPath(), getRefString());
    }

    /**
     * Accessor for the refString.
     * @return  The String representing the artifact reference
     */
    public String getRefString(){
        if (fragment == null || fragment.isEmpty()){
            return refPath;
        } else {
            return refPath + "#" + fragment;
        }
    }

    /**
     * A convenience method to return a Path for the artifact Reference
     * @return Path for the artifact Reference
     */
    public String getRefPath(){
        return refPath;
    }

    /**
     * Setter for the fragment specifier
     * @param fragment the specifier
     */
    public void setFragment(String fragment){
        this.fragment = fragment;
    }

    /**
     * Accessor for the specifier fragment
     * @return The specifier fragment
     */
    public String getFragment(){
        return this.fragment;
    }

    /**
     * A convenience method to return just the last path component
     * plus any specifier
     * @return
     */
    public String getRefName(){
        File refFile = new File(refPath);
        if (fragment == null || fragment.isEmpty()){
            return refFile.getName();
        } else {
            return refFile.getName() + "#" + fragment;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactReference)) return false;

        ArtifactReference artifactReference = (ArtifactReference) o;

        if(refPath != null ? !refPath.equals(artifactReference.refPath) : artifactReference.refPath != null)
            return false;

       if(fragment != null ? !fragment.equals(artifactReference.fragment) : artifactReference.fragment != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = refPath != null ? refPath.hashCode() : 0;
        result = 31 * result + (fragment != null ? fragment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArtifactReference{Reference String= " + refPath + ", Fragment= " + fragment + "}" ;
    }

}
