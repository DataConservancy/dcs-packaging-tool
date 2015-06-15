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
package org.dataconservancy.packaging.tool.model;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Provides methods to serialize and deserialize {@code PackageGenerationParameters} objects
 * to and from properties files using {@link org.apache.commons.configuration.PropertiesConfiguration}
 */
public class PropertiesConfigurationParametersBuilder  implements PackageGenerationParametersBuilder {

    @Override
    public PackageGenerationParameters buildParameters(InputStream inputStream) throws ParametersBuildException {
        PackageGenerationParameters parameters = new PackageGenerationParameters();
        PropertiesConfiguration props = new PropertiesConfiguration();
        try {
            props.load(new InputStreamReader(inputStream));
        } catch (ConfigurationException e) {
            throw new ParametersBuildException(e);
        }

        Iterator<String> keys = props.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            parameters.addParam(key, Arrays.asList(props.getStringArray(key)));

        }
        return parameters;
    }

    @Override
    public void buildParameters(PackageGenerationParameters params, OutputStream outputStream) throws ParametersBuildException {
        PropertiesConfiguration props = new PropertiesConfiguration();

        for (String key : params.getKeys()) {
            props.setProperty(key, params.getParam(key));
        }

        try {
            props.save(new OutputStreamWriter(outputStream));
        } catch (ConfigurationException e) {
            throw new ParametersBuildException(e);
        }
    }
}
