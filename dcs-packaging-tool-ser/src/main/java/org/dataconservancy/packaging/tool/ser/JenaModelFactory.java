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

/**
 * Simple interface responsible for creating new instances of a Jena {@code Model}.  Implementations of this interface
 * configure the concrete {@link Model} instances returned from this method.
 */
interface JenaModelFactory {

    /**
     * Answers a configured, ready-to-use {@link Model}.  Each invocation of this method should return a new instance.
     *
     * @return the {@code Model}
     */
    public Model newModel();

}
