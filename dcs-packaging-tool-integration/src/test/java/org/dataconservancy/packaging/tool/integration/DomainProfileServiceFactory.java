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

package org.dataconservancy.packaging.tool.integration;

import org.apache.jena.rdf.model.Model;

import org.dataconservancy.packaging.tool.api.DomainProfileService;
import org.dataconservancy.packaging.tool.impl.DomainProfileObjectStoreImpl;
import org.dataconservancy.packaging.tool.impl.DomainProfileServiceImpl;
import org.dataconservancy.packaging.tool.impl.URIGenerator;
import org.springframework.beans.factory.annotation.Autowired;

public class DomainProfileServiceFactory {

    @Autowired
    URIGenerator uriGen;

    public DomainProfileService getProfileService(Model model) {
        DomainProfileObjectStoreImpl objectStore =
                new DomainProfileObjectStoreImpl(model, uriGen);
        return new DomainProfileServiceImpl(objectStore, uriGen);
    }
}
