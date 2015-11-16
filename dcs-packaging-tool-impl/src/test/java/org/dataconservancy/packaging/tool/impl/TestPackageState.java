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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.dataconservancy.packaging.tool.model.PackageState;
import org.dataconservancy.packaging.tool.model.dprofile.Property;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyType;
import org.dataconservancy.packaging.tool.model.dprofile.PropertyValueType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Test objects and resources for integration testing the production configuration (and implementation) of
 * PackageStateSerializer.
 */
public class TestPackageState {

    /**
     * Serialization version 1 objects.
     */
    public static class V1 {

        /**
         * Textual (i.e. serialized) form of streams in the Package State.
         */
        public static class Resources {
            public static Resource APPLICATION_VERSION = new ClassPathResource("org/dataconservancy/packaging/tool/impl/appver-v1.ser");
            public static Resource PACKAGE_METADATA = new ClassPathResource("org/dataconservancy/packaging/tool/impl/metadata-v1.ser");
            public static Resource PACKAGE_TREE = new ClassPathResource("org/dataconservancy/packaging/tool/impl/tree-v1.ser");
            public static Resource PACKAGE_NAME = new ClassPathResource("org/dataconservancy/packaging/tool/impl/name-v1.ser");
            public static Resource DOMAIN_OBJECTS = new ClassPathResource("org/dataconservancy/packaging/tool/impl/objects-v1.ser");
            public static Resource DOMAIN_PROFILES = new ClassPathResource("org/dataconservancy/packaging/tool/impl/profiles-v1.ser");
            public static Resource USER_PROPS = new ClassPathResource("org/dataconservancy/packaging/tool/impl/userprops-v1.ser");
            public static Resource FULL_STATE = new ClassPathResource("org/dataconservancy/packaging/tool/impl/fullstate-v1.ser");

            public static Resource PACKAGE_TREE_RDF = new ClassPathResource("org/dataconservancy/packaging/tool/impl/package-tree-v1.rdf");
            public static Resource DOMAIN_OBJECTS_RDF = new ClassPathResource("org/dataconservancy/packaging/tool/impl/domain-objects-v1.rdf");
        }

        /**
         * Object (i.e. deserialized) form of streams in the Package State.
         */
        public static class Objects {
            public static ApplicationVersion appVersion = new ApplicationVersion();
            public static LinkedHashMap<String, List<String>> metadata = new LinkedHashMap<>();
            public static Model tree = ModelFactory.createDefaultModel();
            public static String name = "PackageName";
            public static Model objects = ModelFactory.createDefaultModel();
            public static List<URI> profiles = new ArrayList<>();
            public static Map<URI, List<Property>> userProps = new HashMap<>();
            public static PackageState fullState = new PackageState();

            static {
                Objects.appVersion.setBuildNumber("1");
                Objects.appVersion.setBuildRevision("abcdefg");
                Objects.appVersion.setBuildTimeStamp("1234");

                Objects.metadata.put("foo", Arrays.asList("bar", "biz"));
                Objects.metadata.put("baz", Arrays.asList("bar"));

                try {
                    Objects.tree.read(Resources.PACKAGE_TREE_RDF.getInputStream(), null);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                try {
                    Objects.objects.read(Resources.DOMAIN_OBJECTS_RDF.getInputStream(), null);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                try {
                    Objects.profiles.add(new URI("http://example.org/domain/v1"));
                    Objects.profiles.add(new URI("http://example.org/properties/v1"));
                    Objects.profiles.add(new URI("http://other.org/properties"));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                try {
                    URI node1 = new URI("node:1");
                    URI node2 = new URI("node:2");
                    Property pred1 = new Property(new PropertyType());
                    pred1.getPropertyType().setDomainPredicate(new URI("pred:1"));
                    pred1.getPropertyType().setPropertyValueType(PropertyValueType.STRING);
                    pred1.setStringValue("foo");
                    Property pred2 = new Property(new PropertyType());
                    pred2.getPropertyType().setDomainPredicate(new URI("pred:2"));
                    pred2.getPropertyType().setPropertyValueType(PropertyValueType.URI);
                    pred2.setUriValue(new URI("value:foo"));
                    List<Property> node1Props = new ArrayList<>();
                    node1Props.add(pred1);
                    node1Props.add(pred2);
                    Objects.userProps.put(node1, node1Props);
                    Property pred3 = new Property(new PropertyType());
                    pred3.getPropertyType().setDomainPredicate(new URI("pred:3"));
                    pred3.getPropertyType().setPropertyValueType(PropertyValueType.STRING);
                    pred3.setStringValue("bar");
                    List<Property> node2Props = new ArrayList<>();
                    node2Props.add(pred3);
                    Objects.userProps.put(node2, node2Props);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }

                fullState.setCreationToolVersion(appVersion);
                fullState.setDomainObjectRDF(objects);
                fullState.setPackageTree(tree);
                fullState.setPackageName(name);
                fullState.setPackageMetadataList(metadata);
                fullState.setUserSpecifiedProperties(userProps);
                fullState.setDomainProfileIdList(profiles);
            }
        }

    }
}
