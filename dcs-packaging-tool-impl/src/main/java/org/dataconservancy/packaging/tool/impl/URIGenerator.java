package org.dataconservancy.packaging.tool.impl;

/*
 * Copyright 2015 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.net.URI;

import org.dataconservancy.packaging.tool.model.ipm.Node;

/**
 * Generate URIs for objects.
 */
public interface URIGenerator {
    /**
     * @param node
     *            Node holding domain object to create identifier for.
     * @return Unique identifier for a domain object with this node.
     */
    URI generateDomainObjectURI(Node node);

    /**
     * @return Unique identifier for a URI.
     */
    URI generateNodeURI();
}
