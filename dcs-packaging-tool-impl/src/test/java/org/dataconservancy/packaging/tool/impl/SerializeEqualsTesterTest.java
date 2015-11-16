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

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.ser.AbstractSerializationTest;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A test of SerializeEqualsTester to make sure it's doing the correct things.
 */
public class SerializeEqualsTesterTest {

    @Test
    public void testEqualPackageState() throws Exception {
        PackageState one = new PackageState();
        PackageState two = new PackageState();

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));

        one.setPackageName("foo");
        two.setPackageName("foo");

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));

        // Output directory is not annotated with @Serialize
        one.setOutputDirectory(new File("bar"));
        one.setOutputDirectory(new File("baz"));

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));

        Model m1 = ModelFactory.createDefaultModel();
        m1.read(AbstractSerializationTest.TestResources.DOMAINOBJECTS_RDF_1.getInputStream(), "TTL");

        Model m2 = ModelFactory.createDefaultModel();
        m2.read(AbstractSerializationTest.TestResources.DOMAINOBJECTS_RDF_1.getInputStream(), "TTL");

        one.setDomainObjectRDF(m1);
        two.setDomainObjectRDF(m2);

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));
    }

    @Test
    public void testNotEqualPackageState() throws Exception {
        PackageState one = new PackageState();
        PackageState two = new PackageState();

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));

        one.setPackageName("foo");

        try {
            SerializeEqualsTester.serializeEquals(one, two);
            fail("Expected assertion error.");
        } catch (AssertionError e) {
            // expected
        }

        two.setPackageName("foo");

        assertTrue(SerializeEqualsTester.serializeEquals(one, two));

        Model m1 = ModelFactory.createDefaultModel();
        m1.read(AbstractSerializationTest.TestResources.DOMAINOBJECTS_RDF_1.getInputStream(), "TTL");

        Model m2 = ModelFactory.createDefaultModel();
        m2.read(AbstractSerializationTest.TestResources.PACKAGE_TREE_RDF_1.getInputStream(), "TTL");

        one.setDomainObjectRDF(m1);
        two.setDomainObjectRDF(m2);

        try {
            SerializeEqualsTester.serializeEquals(one, two);
            fail("Expected assertion error.");
        } catch (AssertionError e) {
            // expected
        }
    }

    /**
     * Scratch test to insure that the {@link SerializeEqualsTester#assertModelEquals(Model, Model)} is doing the
     * right thing with blank nodes.
     *
     * @throws Exception
     */
    @Test
    public void testScratchBlankNodeEquality() throws Exception {
        Model one = ModelFactory.createDefaultModel();
        Model two = ModelFactory.createDefaultModel();

        one.add(one.createResource(AnonId.create()), one.createProperty("foo", "bar"),
                one.createResource("http://foo.bar.baz/"));

        two.add(two.createResource(AnonId.create()), two.createProperty("foo", "bar"),
                two.createResource("http://foo.bar.baz/"));

//        Statement sOne = one.listStatements().nextStatement();
//        Statement sTwo = two.listStatements().nextStatement();
//        System.err.println(sOne);
//        System.err.println(sTwo);

        SerializeEqualsTester.assertModelEquals(one, two);
    }
}