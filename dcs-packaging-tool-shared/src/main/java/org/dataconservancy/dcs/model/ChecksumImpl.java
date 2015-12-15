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
package org.dataconservancy.dcs.model;

public class ChecksumImpl implements Checksum {
    private String algorithm;
    private String checksum;
    
    public ChecksumImpl(String algorithm, String checksum) {
        this.algorithm = algorithm;
        this.checksum = checksum;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public String getValue() {
        return checksum;
    }
    
    public String toString() {
        return "Checksum{" +
                "algorithm='" + algorithm + '\'' +
                ", checksum='" + checksum + '\'' +
                '}';
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        return result;
    }
    /**
     * Returns true if a checksum has the same algorithm and checksum value
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof ChecksumImpl)) {
            return false;
        }

        ChecksumImpl other = (ChecksumImpl) o;

        if (algorithm == null) {
            if (other.algorithm != null) {
                return false;
            }
        } else if (!algorithm.equalsIgnoreCase(other.algorithm)) {
            return false;
        }
        
        if (checksum == null) {
            if (other.checksum != null) {
                return false;
            }
        } else if (!checksum.equals(other.checksum)) {
            return false;
        }
        
        return true;        
    }
    /**
     * Returns a checksum from the provided string. 
     * @param value The string representation of a checksum.
     * @return The checksum if one could be parsed, null otherwise.
     */
    public static Checksum parse(String value) {
        Checksum checksum = null;
        
        if (!value.contains("algorithm=") && !value.contains("checksum=")) {
            return null;
        }
        
        //Go to the start of the first algorithm character
        int startIndex = value.indexOf('=') + 2;
        String algorithm = "";
        for (int i = startIndex; i < value.length(); i++) {
            char car = value.charAt(i);
            if (car != '\'') {
                algorithm += car;
            } else {
                startIndex = i;
                break;
            }
        }
        
        startIndex = value.indexOf('=', startIndex) + 2;
        String fixity = "";
        for (int i = startIndex; i < value.length(); i++) {
            char car = value.charAt(i);
            if (car != '\'') {
                fixity += car;
            } else {
                break;
            }
        }
        
        if (!algorithm.isEmpty() && !fixity.isEmpty()) {
            checksum = new ChecksumImpl(algorithm, fixity);
        }
        
        return checksum;
    }
}