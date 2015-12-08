/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.junit.Before;
import org.junit.Test;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;

import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class PackageAssemblerFactoryTest {

    @Autowired
    private PackageAssemblerFactory underTest;

    @Before
    public void setUp() throws Exception {
        assertNotNull("No PackageAssembler instance was autowired!", underTest);

    }

    @Test
    public void testGetNewAssembler() throws InstantiationException, IllegalAccessException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, "test");

        PackageAssembler assembler = underTest.newAssembler(params, new HashMap<>());

        assertNotNull(assembler);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testGetNewAssemblerFailsIfNoFormatIdParameterSet() throws InstantiationException, IllegalAccessException {
        PackageGenerationParameters params = new PackageGenerationParameters();

        underTest.newAssembler(params, new HashMap<>());
    }


    @Test
    public void testGetNewAssemblerReturnsNullIfNoMatchingAssemblerFound() throws InstantiationException, IllegalAccessException {
        PackageGenerationParameters params = new PackageGenerationParameters();
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, "not-a-real-assembler");

        PackageAssembler assembler = underTest.newAssembler(params, new HashMap<>());

        assertNull(assembler);
    }
}
