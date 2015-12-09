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

package org.dataconservancy.packaging.tool.ser;import org.apache.jena.rdf.model.ModelFactory;

import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class JenaModelSerializerEncodingTest extends AbstractJenaEncodingTest {

    private JenaModelFactory modelFactory;

    @Override
    @Before
    public void setUp() throws Exception {
        // Mock the model factory to return the default Jena Model
        this.modelFactory = mock(JenaModelFactory.class);
        when(modelFactory.newModel()).thenReturn(ModelFactory.createDefaultModel());
        super.setUp();
    }

    @Override
    protected JenaModelSerializer getModelSerializerUnderTest() {
        return new JenaModelSerializer(modelFactory);
    }
}
