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
import org.apache.jena.shared.PrefixMapping;
import org.dataconservancy.packaging.tool.ontologies.Ontologies;

import java.util.Map;

/**
 * Creates instances of the default Jena {@code Model} with support for prefix maps.
 */
public class DefaultModelFactory implements JenaModelFactory {

    /**
     * Prefix maps used when (de)serializing Model instances.
     */
    private PrefixMapping prefixMapping;

    /**
     * {@inheritDoc}
     * <p>
     * Creates a new instance of the Jena default model.
     * </p>
     *
     * @return a new {@code Model} instance
     * @see ModelFactory#createDefaultModel()
     */
    @Override
    public Model newModel() {
        return ModelFactory.createDefaultModel();
    }

    /**
     * The prefix map used when (de)serializing {@code Model} instances.
     *
     * @return the prefix mapping
     * @see Ontologies#PREFIX_MAP
     */
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    /**
     * The prefix map used when (de)serializing {@code Model} instances.
     *
     * @param prefixMapping the prefix mapping
     * @see Ontologies#PREFIX_MAP
     */
    public void setPrefixMapping(PrefixMapping prefixMapping) {
        ModelFactory.setDefaultModelPrefixes(prefixMapping);
    }

    /**
     * The prefix map used when (de)serializing {@code Model} instances.
     *
     * @param prefixMapping values are URIs keyed by their prefix
     * @see Ontologies#PREFIX_MAP
     */
    public void setPrefixMapping(Map<String, String> prefixMapping) {
        PrefixMapping mapping = PrefixMapping.Factory.create();
        mapping.setNsPrefixes(prefixMapping);
        setPrefixMapping(mapping);
    }
}
