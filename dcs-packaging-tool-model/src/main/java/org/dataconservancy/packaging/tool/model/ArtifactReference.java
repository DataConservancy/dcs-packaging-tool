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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *  A class to implement the functionality we need for a PackageArtifact's artifactRef. We sometimes need this to have a
 *  String, URL or URI aspect. This class localizes the translations required, to obviate conversions in calling classes.
 *  Constructor taking a String requires that the String be a valid URI. Additional constructors may be added in the future;
 *  all constructors should ensure that every getter will return a valid instance of its type.
 */
public class ArtifactReference {

    private final String refString;
    private final URI refURI;
    private final URL refURL;

    public ArtifactReference(String refString) throws IllegalArgumentException{
       this.refString=refString;
       try {
           refURI = new URI(refString);
           refURL = new URL(refString);
       } catch (MalformedURLException e){
           throw new IllegalArgumentException("Illegal argument: '" + refString + "' cannot be converted to a valid URL",e);
       } catch (URISyntaxException e) {
           throw new IllegalArgumentException("Illegal argument: '" + refString + "' cannot be converted to a valid URI",e);
       }
    }

    public ArtifactReference(URI uri) throws IllegalArgumentException{
        this.refURI = uri;
        try{
            refString = uri.toString();
            refURL=uri.toURL();
        } catch (MalformedURLException e){
           throw new IllegalArgumentException("Illegal argument: '" + uri.toString() + "' cannot be converted to a valid URL",e);
        }
    }

    public URI getRefURI(){ return refURI; }

    public String getRefString(){
        return refString;
    }

    public URL getRefURL(){
        return refURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArtifactReference)) return false;

        ArtifactReference artifactReference = (ArtifactReference) o;

        if(refString != null ? !refString.equals(artifactReference.refString) : artifactReference.refString != null)
            return false;
        if(refURI != null ? !refURI.equals(artifactReference.refURI) : artifactReference.refURI != null)
            return false;
        if(refURL!= null ? !refURL.equals(artifactReference.refURL) : artifactReference.refURL != null)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = refString != null ? refString.hashCode() : 0;
        result = 31 * result + (refString != null ? refString.hashCode() : 0);
        result = 31 * result + (refURI != null ? refURI.hashCode() : 0);
        result = 31 * result + (refURL != null ? refURL.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ArtifactReference{Reference String= " + refString + ", Reference URI= " + refURI.toString() + ", Reference URL= " + refURL.toString() + "}" ;
    }

}
