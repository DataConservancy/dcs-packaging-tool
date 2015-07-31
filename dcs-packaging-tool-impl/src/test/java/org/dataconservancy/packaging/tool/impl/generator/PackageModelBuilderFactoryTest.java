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

import org.dataconservancy.packaging.tool.api.OntologyIdentifiers;
import org.dataconservancy.packaging.tool.api.PackagingFormat;
import org.dataconservancy.packaging.tool.api.generator.PackageModelBuilder;
import org.dataconservancy.packaging.tool.model.GeneralParameterNames;
import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/test-applicationContext.xml"})
public class PackageModelBuilderFactoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testGetNewBuilder() throws InstantiationException, IllegalAccessException {
        PackageDescription desc = new PackageDescription();
        desc.setPackageOntologyIdentifier(OntologyIdentifiers.DCSBO.toString());
        PackageGenerationParameters params = new PackageGenerationParameters();
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, PackagingFormat.BOREM.toString());
        params.addParam(GeneralParameterNames.CONTENT_ROOT_LOCATION, folder.getRoot().getPath());
        PackageModelBuilder builder = PackageModelBuilderFactory.newBuilder(desc, params);

        assertNotNull(builder);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testGetNewBuilderFailsIfNoFormatIdParameterSet() throws InstantiationException, IllegalAccessException {
        PackageDescription desc = new PackageDescription();
        PackageGenerationParameters params = new PackageGenerationParameters();

        PackageModelBuilder builder = PackageModelBuilderFactory.newBuilder(desc, params);
    }


    @Test
    public void testGetNewAssemblerReturnsNullIfNoMatchingAssemblerFound() throws InstantiationException, IllegalAccessException {
        PackageDescription desc = new PackageDescription();
        PackageGenerationParameters params = new PackageGenerationParameters();
        params.addParam(GeneralParameterNames.PACKAGE_FORMAT_ID, "not-a-real-builder");

        PackageModelBuilder builder = PackageModelBuilderFactory.newBuilder(desc ,params);

        assertNull(builder);
    }
}
