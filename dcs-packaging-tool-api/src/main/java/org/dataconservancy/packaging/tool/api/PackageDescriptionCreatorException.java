/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.api;


public class PackageDescriptionCreatorException
        extends Exception {

    private static final long serialVersionUID = 1L;
    private boolean hasDetail = false;
    private String detail;
    
    public PackageDescriptionCreatorException(String message) {
        super(message);
    }
    
    public PackageDescriptionCreatorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PackageDescriptionCreatorException(String message, String resolution) {
        super(message);
        setDetail(resolution);
    }
    
    
    public PackageDescriptionCreatorException(String message, String resolution, Throwable cause) {
        super(message, cause);
        setDetail(resolution);
    }
    
    /** Determine if explanatory text containing a possible resolution is defined */
    public boolean hasDetail() {
        return hasDetail;
    }
    
    /** Get the optional explanatory text containing possible resolutions */
    public String getDetail() {
        return detail;
    }
    
    private void setDetail(String detail) {
        if (detail != null) {
            hasDetail = true;
            this.detail = detail;
        }
    }
}
