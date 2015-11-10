/*
 * Copyright 2015 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.dataconservancy.packaging.tool.model.ipm.Node;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class PackageStateTest {
    @Test
    public void PackageStateVerifier() throws URISyntaxException {
        EqualsVerifier
                .forClass(PackageState.class).allFieldsShouldBeUsed()
                .withPrefabValues(Node.class, new Node(new URI("uri:foo")), new Node(new URI("uri:bar")))
                .suppress(Warning.STRICT_INHERITANCE, Warning.NONFINAL_FIELDS, Warning.REFERENCE_EQUALITY)
                .verify();
    }
}
