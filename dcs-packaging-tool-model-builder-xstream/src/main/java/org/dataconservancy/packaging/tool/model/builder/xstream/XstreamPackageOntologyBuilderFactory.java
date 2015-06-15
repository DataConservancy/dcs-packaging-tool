/*
 * Copyright 2015 Johns Hopkins University
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.QNameMap;
import org.dataconservancy.model.builder.xstream.DcsPullDriver;
import org.dataconservancy.packaging.tool.model.PackageOntology;

import javax.xml.namespace.QName;

/**
 * Created by hanh on 2/17/14.
 */
public class XstreamPackageOntologyBuilderFactory {

    public static XstreamPackageOntologyBuilder newInstance() {

        final QNameMap qnames = new QNameMap();

        final DcsPullDriver driver = new DcsPullDriver(qnames);

        final String defaultnsUri ="http://dataconservancy.org/schemas/pkg/1.0";
        qnames.setDefaultNamespace(defaultnsUri);

        // The XStream Driver
        final XStream x = new XStream(driver);
        x.setMode(XStream.NO_REFERENCES);

        x.registerConverter(new PackageOntologyConverter());
        qnames.registerMapping(new QName(defaultnsUri, PackageOntologyConverter.E_PACKAGE_ONTOLOGY), PackageOntology.class);

        return new XstreamPackageOntologyBuilder(x);
    }
}
