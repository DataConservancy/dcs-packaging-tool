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

import org.dataconservancy.packaging.tool.model.ApplicationVersion;
import org.junit.Before;
import org.springframework.core.io.ClassPathResource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Base test class for all XStream related classes.
 */
public abstract class AbstractXstreamTest {

    private XmlPullParserFactory xppFactory;

    /**
     * Currently instantiates an XmlPullParserFactory.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        xppFactory = XmlPullParserFactory.newInstance();
    }

    /**
     * Obtain a new instance of an XmlPullParser, used when deserializing object instances.
     *
     * @return a new instance of XmlPullParser
     * @throws XmlPullParserException
     */
    public XmlPullParser getPullParser() throws XmlPullParserException {
        return xppFactory.newPullParser();
    }

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

        static {
            applicationVersion.setBuildNumber("1");
            applicationVersion.setBuildRevision("abcdefg");
            applicationVersion.setBuildTimeStamp("1234");
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
    }

}
