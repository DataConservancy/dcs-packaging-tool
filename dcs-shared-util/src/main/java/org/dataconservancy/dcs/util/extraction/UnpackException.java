/*
 * Copyright 2012 Johns Hopkins University
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
package org.dataconservancy.dcs.util.extraction;

/**
 * This exception indicates exceptional condition or error encountered when a packaged file (zip file) is being unpacked.
 */
public class UnpackException extends Exception {

    private String filename;
    private String error;

    public UnpackException() {
        
    }

    public UnpackException(String message) {
        super(message);
    }

    public UnpackException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnpackException(Throwable cause) {
        super(cause);
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}