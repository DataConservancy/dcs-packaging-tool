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

package org.dataconservancy.packaging.tool.ser;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * Simple convenience methods for XStream converters.
 */
class XstreamUtil {

    /**
     * Returns true if the reader has the named attribute at the current node.
     *
     * @param attName The attribute name to look for in the provided node.
     * @param reader The reader of the node to check for the attribute.
     * @return True if the node has passed in attribute name, false otherwise.
     */
    static boolean hasAttribute(String attName, HierarchicalStreamReader reader) {
        if (reader.getAttribute(attName) == null) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the reader has the named attribute with the specified value at the current node.
     *
     * @param attName The attribute name to look for in the provided node.
     * @param attValue The attribute value to look for in the provided node.
     * @param reader The reader of the node to check for the attribute.
     * @return True if node has the attribute name and value, false otherwise.
     */
    static boolean hasAttribute(String attName, String attValue, HierarchicalStreamReader reader) {
        if (attValue.equals(reader.getAttribute(attName))) {
            return true;
        }

        return false;
    }

}
