/*
 * Copyright 2015 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model;

import org.dataconservancy.packaging.tool.model.description.RulesSpec;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Serialize and deserialize RuleSpecs objects for package description rules
 */
public interface PackageDescriptionRulesBuilder {

    /**
     * Deserialize a suitable bytestream into a {@code RuleSpecs} object
     * @param in the bytestream which contains the serialized Rules
     * @return a deserialized {@code RuleSpecs} object
     */
    public RulesSpec buildPackageDescriptionRules(InputStream in);

    /**
     * Serialize a {@code RuleSpecs} object into a bytestream
     * @param ruleSpecs the RulesSpec
     * @param out the OutputStream
     */
    public void buildPackageDescriptionRules(RulesSpec ruleSpecs, OutputStream out);
}
