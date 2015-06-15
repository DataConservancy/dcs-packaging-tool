/*
 * CopyrigFht 2014 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.lang.reflect.Method;

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.ValueOperationFactory;
import org.dataconservancy.packaging.tool.model.description.TestParam;
import org.dataconservancy.packaging.tool.model.description.TestSpec;

public abstract class TestUtil {

    public static String getParam(String paramName,
                                  TestSpec spec,
                                  FileContext cxt) {
        try {
            Method m = spec.getClass().getMethod("get" + capitalize(paramName));
            Object value = m.invoke(spec);
            if (value != null) {
                return value.toString();
            } else {
                return getValueFromOp(paramName, spec, cxt);
            }
        } catch (NoSuchMethodException e) {
            /* Probably won't ever happen */
            return getValueFromOp(paramName, spec, cxt);
        } catch (Exception x) {
            /* Should not happen */
            throw new RuntimeException(x);
        }
    }

    public static String getValueFromOp(String paramName,
                                        TestSpec spec,
                                        FileContext cxt) {
        if (spec.getParams() != null) {
            for (TestParam param : spec.getParams().getParam()) {
                if (paramName.equals(param.getName())) {
                    String[] values =
                            ValueOperationFactory
                                    .getOperation(param.getValue())
                                    .operate(cxt);

                    if (values != null && values.length > 0) {
                        return values[0];
                    }
                }
            }
        }
        return null;
    }

    public static String capitalize(String val) {
        return Character.toUpperCase(val.charAt(0)) + val.substring(1);
    }
}
