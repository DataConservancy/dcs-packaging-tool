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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import org.junit.Before;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import static org.mockito.Mockito.mock;

/**
 * Provides access to common methods used for all XStream converter tests.  Subclasses may override any of these
 * methods.
 */
public abstract class AbstractConverterTest extends AbstractXstreamTest {

    /**
     * Returns a configured {@link UnmarshallingContext}.  Currently this implementation returns a mock.  Subclasses
     * should override this if they need to test behaviors of the {@code UnmarshallingContext}.
     *
     * @return a new, configured, {@code UnmarshallingContext}
     */
    public UnmarshallingContext getUnmarshallingContext() {
        return mock(UnmarshallingContext.class);
    }


    /**
     * Returns a configured {@link MarshallingContext}.  Currently this implementation returns a mock.  Subclasses
     * should override this if they need to test behaviors of the {@code MarshallingContext}.
     *
     * @return a new, configured, {@code MarshallingContext}
     */
    public MarshallingContext getMarshalingContext() {
        return mock(MarshallingContext.class);
    }

}
