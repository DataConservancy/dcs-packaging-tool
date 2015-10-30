/*
 * Copyright 2012 Johns Hopkins University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.dataconservancy.packaging.tool.model.builder.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.core.JsonLdTripleCallback;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JsonUtils;
import org.apache.jena.rdf.model.Model;

import org.dataconservancy.packaging.tool.model.PackageDescription;
import org.dataconservancy.packaging.tool.model.PackageDescriptionBuilder;
import org.dataconservancy.packaging.tool.model.PackageDescriptionRDFTransform;
import org.dataconservancy.packaging.tool.model.PackageToolException;
import org.dataconservancy.packaging.tool.model.PackagingToolReturnInfo;
import org.dataconservancy.packaging.tool.model.RDFTransformException;

/**
 * Converts from {@code PackageDescription} to JSON-ld and vice versa. 
 *
 * This class uses {@code PackageDescriptionRDFTransform} to convert a {@code PackageDescription} to RDF before serializing to json. 
 */
public class JSONPackageDescriptionBuilder implements  PackageDescriptionBuilder {
    
    
    public void serialize(PackageDescription description, OutputStream stream) throws PackageToolException {

        try {
            Model descriptionModel = PackageDescriptionRDFTransform.transformToRDF(description);
            JsonLdOptions opts = new JsonLdOptions();
            opts.setUseRdfType(true);
            Object json = JsonLdProcessor.fromRDF(descriptionModel, opts);
            
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("dc", "http://dataconservancy.org/ns/types/");

            // Must compact to turn into a graph and use context
            json = JsonLdProcessor.compact(json, context, opts);

            // Pretty print output to ease debugging
            
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8"); 
            JsonUtils.writePrettyPrint(writer, json);
            writer.flush();
        } catch (JsonLdError e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_JSON_PROCESSING_ERROR, e);
        } catch (UnsupportedEncodingException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_UNSUPPORTED_ENCODING_EXCEPTION, e);
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_IO_EXCEPTION, e);
        } catch (RDFTransformException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_RDF_TRANSFORM_EXCEPTION, e);
        }

    }
    
    public PackageDescription deserialize(InputStream stream) throws PackageToolException {
        PackageDescription description;
        try {
            Object json = JsonUtils.fromInputStream(stream);
            final JsonLdTripleCallback callback = new JsonLdTripleCallback() {
                @Override
                public Object call(RDFDataset rdfDataset) {
                    // Default method body
                    return null;
                }
            };

            JsonLdOptions opts = new JsonLdOptions();
            Model model = (Model) JsonLdProcessor.toRDF(json, callback, opts);
            description = PackageDescriptionRDFTransform.transformToPackageDescription(model);
        } catch (JsonLdError e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_JSON_PROCESSING_ERROR, e);
        } catch (IOException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_IO_EXCEPTION, e);
        } catch (RDFTransformException e) {
            throw new PackageToolException(PackagingToolReturnInfo.PKG_DESC_RDF_TRANSFORM_EXCEPTION, e);
        }
        return description;
    }
}
