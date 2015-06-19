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

/**
 * A util class representing a key value pair.
 *
 */
public class Pair<K, V> {
    private K key; //key member of pair
    private V value; //value member of pair

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    
    public String toString() {
        return "Pair{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
    
    /**
     * Returns true if a pair has the same key and value
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || !(o instanceof Pair)) {
            return false;
        }

        Pair<K,V> other = (Pair<K,V>) o;

        if (key == null) {
            if (other.key != null) {
                return false;
            } 
        } else if (!key.equals(other.key)) {
            return false;
        }
        
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        
        return true;        
    }
}