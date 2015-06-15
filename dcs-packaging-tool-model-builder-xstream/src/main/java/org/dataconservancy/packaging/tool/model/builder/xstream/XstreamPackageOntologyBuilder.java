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
import org.dataconservancy.packaging.tool.model.PackageOntology;
import org.dataconservancy.packaging.tool.model.PackageOntologyBuilder;

import java.io.InputStream;
import java.io.OutputStream;

public class XstreamPackageOntologyBuilder implements PackageOntologyBuilder {

    private XStream x;

    public XstreamPackageOntologyBuilder(XStream x) {
        this.x = x;
    }

    @Override
    public PackageOntology buildOntology(InputStream in) {
        return (PackageOntology)x.fromXML(in);
    }

    @Override
    public void buildOntology(PackageOntology ontology, OutputStream out) {
        x.toXML(ontology, out);
    }
}

