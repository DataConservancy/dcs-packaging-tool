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

package org.dataconservancy.packaging.tool.impl.generator;

import org.dataconservancy.packaging.tool.api.generator.PackageAssembler;
import org.dataconservancy.packaging.tool.api.generator.PackageResourceType;
import org.dataconservancy.packaging.tool.model.PackageGenerationParameters;
import org.dataconservancy.packaging.tool.ontologies.ModelResources;
import org.junit.Test;

import java.io.InputStream;

import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.REM_SERIALIZATION_FORMAT;
import static org.dataconservancy.packaging.tool.model.GeneralParameterNames.SERIALIZATION_FORMAT.JSONLD;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Simple test insuring that the BrainDeadModelIncluder invokes the assembler as expected.
 */
public class BrainDeadModelIncluderTest {

    private BrainDeadModelIncluder underTest = new BrainDeadModelIncluder();

    private PackageModelBuilderState state = new PackageModelBuilderState();


    @Test
    public void testFinish() throws Exception {
        state.assembler = mock(PackageAssembler.class);
        state.params = new PackageGenerationParameters();
        state.params.addParam(REM_SERIALIZATION_FORMAT, JSONLD.name());

        underTest.finish(state);

        verify(state.assembler, times(ModelResources.RESOURCE_MAP.size())).createResource(anyString(), eq(PackageResourceType.ONTOLOGY), any(InputStream.class));

        // verify one or two resource names, just to make sure resource name is being created property
        verify(state.assembler).createResource(eq("foaf.jsonld"), eq(PackageResourceType.ONTOLOGY), any(InputStream.class));
        verify(state.assembler).createResource(eq("pcdm.jsonld"), eq(PackageResourceType.ONTOLOGY), any(InputStream.class));
    }
}