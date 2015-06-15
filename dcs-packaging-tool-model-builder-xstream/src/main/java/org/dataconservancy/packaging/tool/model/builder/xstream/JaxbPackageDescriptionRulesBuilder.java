/*
 * Copyright 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.model.builder.xstream;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.dataconservancy.packaging.tool.model.PackageDescriptionRulesBuilder;
import org.dataconservancy.packaging.tool.model.description.ObjectFactory;
import org.dataconservancy.packaging.tool.model.description.RulesSpec;

public class JaxbPackageDescriptionRulesBuilder
        implements PackageDescriptionRulesBuilder {

    @SuppressWarnings("unchecked")
    @Override
    public RulesSpec buildPackageDescriptionRules(InputStream in) {
        try {
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return ((JAXBElement<RulesSpec>) unmarshaller.unmarshal(in)).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void buildPackageDescriptionRules(RulesSpec ruleSpecs,
                                             OutputStream out) {
        try {
            JAXBContext context = JAXBContext.newInstance(RulesSpec.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(ruleSpecs, out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
