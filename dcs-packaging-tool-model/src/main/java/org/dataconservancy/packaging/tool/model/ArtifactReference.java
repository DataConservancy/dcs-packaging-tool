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
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *  A class to implement the functionality we need for a PackageArtifact's artifactRef. We sometimes need this to have a
 *  String,  URI aspect. This class localizes the translations required, to obviate conversions in calling classes.
 *  Constructor taking a String requires that the String be a valid relative URI. Additional constructors may be added in the future;
 *  all constructors should ensure that every getter will return a valid instance of its type. The getRefURI method takes
 *  the content root directory (or its URI) as an argument, and returns an absolute URI for the artifact reference, resolved against the content root.
 */
public class ArtifactReference {

    private String fragment = null;
    private String refPath;

    public ArtifactReference(String refPath) {
       this.refPath=refPath;
    }

    public String getResolvedAbsoluteRefString(File contentRootFile) {
        File absFile = new File(contentRootFile, getRefString());
        return absFile.getPath();
    }

    public Path getResolvedAbsoluteRefPath(File contentRootFile) {
        return Paths.get(contentRootFile.getPath(), getRefString());
    }

    public String getRefString(){
        if (fragment == null || fragment.isEmpty()){
            return refPath;
        } else {
            return refPath + "#" + fragment;
        }
    }

    public String getRefPath(){
        return refPath;
    }

    public void setFragment(String fragment){
        this.fragment = fragment;
    }

    public String getFragment(){
        return this.fragment;
    }

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
        return "ArtifactReference{Reference String= " + refPath + "}" ;
    }

}
