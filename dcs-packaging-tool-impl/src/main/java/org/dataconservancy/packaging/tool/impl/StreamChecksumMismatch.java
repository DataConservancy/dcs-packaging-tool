/*
 *
 *  * Copyright 2015 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.packaging.tool.impl;

/**
 * Exception thrown when a stream is read from a package state zip file, and the stream's expected checksum value does
 * not match the calculated checksum.
 */
public class StreamChecksumMismatch extends RuntimeException {

    public StreamChecksumMismatch() {
        super();
    }

    public StreamChecksumMismatch(Throwable cause) {
        super(cause);
    }

    public StreamChecksumMismatch(String message) {
        super(message);
    }

    public StreamChecksumMismatch(String message, Throwable cause) {
        super(message, cause);
    }

}
