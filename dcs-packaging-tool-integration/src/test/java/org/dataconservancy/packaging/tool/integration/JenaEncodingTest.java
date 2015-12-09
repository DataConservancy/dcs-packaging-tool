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

package org.dataconservancy.packaging.tool.integration;

import org.dataconservancy.packaging.tool.ser.AbstractJenaEncodingTest;
import org.dataconservancy.packaging.tool.ser.JenaModelSerializer;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

/**
 * Tests ensuring that Jena is properly encoding unicode characters as UTF-8 using the RIOT codepaths.  RIOT is expected
 * to be used because {@code jena-arq} is on the classpath.
 */
@ContextConfiguration({
        "classpath*:org/dataconservancy/config/applicationContext.xml",
        "classpath*:org/dataconservancy/packaging/tool/ser/config/applicationContext.xml",
        "classpath*:applicationContext.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class JenaEncodingTest extends AbstractJenaEncodingTest {

    @Autowired
    @Qualifier("jenaMarshaller")
    private JenaModelSerializer underTest;

    @Override
    protected JenaModelSerializer getModelSerializerUnderTest() {
        return underTest;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        try {
            assertNotNull("Expected to find RIOT on the classpath!", Class.forName("org.apache.jena.riot.ReaderRIOT"));
        } catch (ClassNotFoundException e) {
            fail("Expected to find RIOT on the classpath!");
        }
        super.setUp();
    }
}
