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

package org.dataconservancy.packaging.tool.impl.rules;

import java.util.HashMap;

import org.dataconservancy.packaging.tool.impl.rules.operations.File_Ancestors;
import org.dataconservancy.packaging.tool.impl.rules.operations.File_Children;
import org.dataconservancy.packaging.tool.impl.rules.operations.File_Descendents;
import org.dataconservancy.packaging.tool.impl.rules.operations.File_Parent;
import org.dataconservancy.packaging.tool.impl.rules.operations.File_Self;
import org.dataconservancy.packaging.tool.model.description.FileRel;
import org.dataconservancy.packaging.tool.model.description.FileSpec;

/** Produces properly configured {@link FileOperation} impls according to the given specification.*/
public class FileOperationFactory {

    @SuppressWarnings("serial")
    private static final HashMap<FileRel, Class<? extends FileOperation>> operationMap =
            new HashMap<FileRel, Class<? extends FileOperation>>() {

                {
                    put(FileRel.ANCESTORS, File_Ancestors.class);
                    put(FileRel.CHILDREN, File_Children.class);
                    put(FileRel.DESCENDENTS, File_Descendents.class);
                    put(FileRel.PARENT, File_Parent.class);
                    put(FileRel.SELF, File_Self.class);
                }
            };

    public static FileOperation getOperation(FileSpec spec) {
        FileOperation fileOp = null;
        try {
            fileOp = operationMap.get(spec.getRel()).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (spec.getTest() != null) {
            fileOp.setConstraints(TestOperationFactory.getOperation(spec
                    .getTest()));
        }

        return fileOp;
    }
}
