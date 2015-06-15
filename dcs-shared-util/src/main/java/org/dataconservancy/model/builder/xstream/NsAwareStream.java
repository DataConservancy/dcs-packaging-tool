/*
 * Copyright 2014 Johns Hopkins University
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
package org.dataconservancy.model.builder.xstream;

import javax.xml.namespace.QName;

/**
 * An interface that namespace aware XStream {@link com.thoughtworks.xstream.io.HierarchicalStreamReader readers} and
 * {@link com.thoughtworks.xstream.io.HierarchicalStreamWriter writers} can implement.
 */
public interface NsAwareStream {

    /**
     * Obtain the {@link QName} of the current element.  This is the namespace-aware analog of
     * {@link com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()}.
     *
     * @return the QName of the current element
     * @see com.thoughtworks.xstream.io.HierarchicalStreamReader#getNodeName()
     */
    public QName getQname(); 

}
