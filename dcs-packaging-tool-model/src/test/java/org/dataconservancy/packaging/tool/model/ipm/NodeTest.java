package org.dataconservancy.packaging.tool.model.ipm;

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


import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.dataconservancy.packaging.tool.model.dprofile.NodeType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class NodeTest {

    private NodeType typeA;
    private NodeType typeB;

    @Before
    public void setup() throws URISyntaxException {
        typeA = new NodeType();
        typeA.setIdentifier(new URI("id:A"));
        typeB = new NodeType();
        typeB.setIdentifier(new URI("id:B"));
    }

    @Test
    public void equalsTest() throws URISyntaxException {
        EqualsVerifier
            .forClass(Node.class)
            .withPrefabValues(Node.class, new Node(new URI("uri:foo")), new Node(new URI("uri:bar")))
            .withPrefabValues(NodeType.class, typeA, typeB)
            .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS)
            .verify();
    }
}
