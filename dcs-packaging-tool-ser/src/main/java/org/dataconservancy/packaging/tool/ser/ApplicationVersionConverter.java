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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.dataconservancy.packaging.tool.model.ApplicationVersion;

/**
 *
 */
public class ApplicationVersionConverter extends AbstractPackageToolConverter {

    static final String E_APPLICATION_VERSION = "applicationVersion";

    static final String E_BUILDNO = "buildNumber";

    static final String E_BUILDREV = "buildRevision";

    static final String E_BUILDTS = "buildTs";

    @Override
    void marshalInternal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ApplicationVersion versionInfo = (ApplicationVersion) source;

        writer.startNode(E_APPLICATION_VERSION);

        writer.startNode(E_BUILDNO);
        writer.setValue(versionInfo.getBuildNumber());
        writer.endNode(); // E_BUILDNO

        writer.startNode(E_BUILDREV);
        writer.setValue(versionInfo.getBuildRevision());
        writer.endNode(); //E_BUILDREV

        writer.startNode(E_BUILDTS);
        writer.setValue(versionInfo.getBuildTimeStamp());
        writer.endNode(); // E_BUILDTS

        writer.endNode(); //E_APPLICATION_VERSION
    }

    @Override
    Object unmarshalInternal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        if (!E_APPLICATION_VERSION.equals(reader.getNodeName())) {
            throw new ConversionException(
                    String.format(ERR_MISSING_EXPECTED_ELEMENT, E_APPLICATION_VERSION, reader.getNodeName()));
        }

        ApplicationVersion versionInfo = new ApplicationVersion();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String nodeName = reader.getNodeName();
            String value = reader.getValue();

            if (E_BUILDNO.equals(nodeName)) {
                versionInfo.setBuildNumber(value);
            }

            if (E_BUILDREV.equals(nodeName)) {
                versionInfo.setBuildRevision(value);
            }

            if (E_BUILDTS.equals(nodeName)) {
                versionInfo.setBuildTimeStamp(value);
            }

            reader.moveUp();
        }

        return versionInfo;
    }

    @Override
    public boolean canConvert(Class type) {
        return ApplicationVersion.class.equals(type);
    }

}
