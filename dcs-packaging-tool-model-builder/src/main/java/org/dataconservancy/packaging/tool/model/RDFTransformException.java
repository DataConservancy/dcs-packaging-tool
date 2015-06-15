/*
 * Copyright 2015 Johns Hopkins University
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

/**
 * Indicates an exceptional condition while doing an RDF Transform.
 * TODO Move this somewhere or maybe find a better exception
 */
public class RDFTransformException extends Exception {
    public RDFTransformException() {
        super();
    }

    public RDFTransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public RDFTransformException(String message) {
        super(message);
    }

    public RDFTransformException(Throwable cause) {
        super(cause);
    }
}
