
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

package org.dataconservancy.packaging.tool.impl.rules.operations;

import java.io.File;

import java.util.ArrayList;

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.FileOperation;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;

/** Returns the ancestors of a given file, context root if given. */
public class File_Ancestors
        implements FileOperation {

    private final File_Parent parentOp = new File_Parent();

    @Override
    public void setConstraints(TestOperation<?>... constraints) {
        parentOp.setConstraints(constraints);
    }

    @Override
    public File[] operate(FileContext fileContext) {
        if (fileContext.getFile() == null || !fileContext.getFile().exists() || !fileContext.getFile().canRead()) {
            throw new OperationException("Pathname " + fileContext.getFile().getPath()
                    + " denotes a file that does not exist or cannot be read.");
        }
        ArrayList<File> fileTraversal = new ArrayList<File>();

        do {
            File[] traverseFiles = parentOp.operate(fileContext);
            if (traverseFiles.length == 1) {
                fileContext =
                        new FileContextImpl(traverseFiles[0],
                                            fileContext.getRoot(), fileContext.isIgnored());
                fileTraversal.add(fileContext.getFile());
            } else {
                break;
            }

        } while (!fileContext.getFile().equals(fileContext.getRoot()));

        return fileTraversal.toArray(new File[0]);
    }

}
