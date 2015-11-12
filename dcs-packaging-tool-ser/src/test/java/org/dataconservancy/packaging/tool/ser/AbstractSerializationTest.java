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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestResources.DOMAINOBJECTS_RDF_1;
import static org.dataconservancy.packaging.tool.ser.AbstractSerializationTest.TestResources.PACKAGE_TREE_RDF_1;

/**
 * Base test class of all serialization tests.
 */
public abstract class AbstractSerializationTest {

    /**
     * Test objects used with {@code dcs-packaging-tool-ser} module, and also shared with other modules using the
     * Maven {@code tests} classifier.
     */
    public static class TestObjects {
        public static ApplicationVersion applicationVersion = new ApplicationVersion();

        public static String packageName = "PackageName";

        public static LinkedHashMap<String, List<String>> packageMetadata = new LinkedHashMap<String, List<String>>() {
            {
                put("foo", Arrays.asList("bar", "biz"));
                put("baz", Arrays.asList("bar"));
            }
        };

        public static List<URI> domainProfileUris = new ArrayList<URI>() {
            {
                try {
                    add(new URI("http://example.org/domain/v1"));
                    add(new URI("http://example.org/properties/v1"));
                    add(new URI("http://other.org/properties"));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

            }
        };

        public static Model domainObjectsRDF;

        public static Model packageTreeRDF;

        public static HashMap<URI, List<Property>> userProperties = new HashMap<URI, List<Property>>() {
            {
                try {
                    PropertyType typeOne = new PropertyType();
                    typeOne.setDomainPredicate(new URI("pred:1"));
                    typeOne.setPropertyValueType(PropertyValueType.STRING);

                    PropertyType typeTwo = new PropertyType();
                    typeTwo.setDomainPredicate(new URI("pred:2"));
                    typeTwo.setPropertyValueType(PropertyValueType.URI);

                    Property propertyOne = new Property(typeOne);
                    propertyOne.setStringValue("foo");

                    Property propertyTwo = new Property(typeTwo);
                    propertyTwo.setUriValue(new URI("value:foo"));

                    Property propertyThree = new Property(typeOne);
                    propertyThree.setStringValue("bar");

                    put(new URI("node:1"), Arrays.asList(propertyOne, propertyTwo));
                    put(new URI("node:2"), Arrays.asList(propertyThree));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };

        static {
            applicationVersion.setBuildNumber("1");
            applicationVersion.setBuildRevision("abcdefg");
            applicationVersion.setBuildTimeStamp("1234");
            try {
                domainObjectsRDF = ModelFactory.createDefaultModel().read(DOMAINOBJECTS_RDF_1.getInputStream(), null);
                packageTreeRDF = ModelFactory.createDefaultModel().read(PACKAGE_TREE_RDF_1.getInputStream(), null);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Test resources used with {@code dcs-packaging-tool-ser} module, and also shared with other modules using the
     * Maven {@code tests} classifier.
     */
    public static class TestResources {
        public static ClassPathResource APPLICATION_VERSION_1 =
                new ClassPathResource("/org/dataconservancy/packaging/tool/ser/application-version-v1.ser");

        public static ClassPathResource PACKAGE_METADATA_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/package-metadata-v1.ser");

        public static ClassPathResource PACKAGE_NAME_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/package-name-v1.ser");

        public static ClassPathResource DOMAINPROFILE_URIS_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/domain-profile-uris-v1.ser");

        public static ClassPathResource DOMAINOBJECTS_RDF_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/domain-objects-rdf-v1.ser");

        public static ClassPathResource USER_PROPERTIES_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/user-property-v1.ser");

        public static ClassPathResource PACKAGE_TREE_RDF_1 =
                new ClassPathResource("org/dataconservancy/packaging/tool/ser/package-tree-rdf-v1.ser");

    }
}
