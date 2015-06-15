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

/**
 * Retrieves the children of the given file.
 * <p>
 * If no operands are given this will return the children of the current file in
 * context. If a test operations are given as operands, these test operations
 * will be run over each individual child. If all test operations evaluate to
 * true, that child will be included in the return list.
 * </p>
 */
public class File_Children
        implements FileOperation {

    private TestOperation<?>[] constraints = new TestOperation<?>[0];

    @Override
    public void setConstraints(TestOperation<?>... constraints) {
        this.constraints = constraints;
    }

    @Override
    public File[] operate(FileContext fileContext) {

        ArrayList<File> childrenToReturn = new ArrayList<File>();

        if (!fileContext.getFile().isDirectory()) {
            return new File[0];
        }

        if (fileContext.getFile().listFiles() == null) {
            throw new OperationException("Pathname " + fileContext.getFile() + " denotes a directory which does not " +
                    "exist, cannot be read or is not a directory.");
        }

        if (constraints.length == 0) {
            return fileContext.getFile().listFiles();
        } else  {
            for (File file : fileContext.getFile().listFiles()) {
                boolean includeFile = true;
                for (TestOperation<?> constraint : constraints) {
                    for (boolean truthValue : constraint
                            .operate(new FileContextImpl(file, fileContext
                                    .getRoot(), fileContext.isIgnored()))) {
                        if (!truthValue) {
                            includeFile = false;
                            break;
                        }
                    }
                }

                if (includeFile) {
                    childrenToReturn.add(file);
                }
            }

        }

        return childrenToReturn.toArray(new File[0]);
    }
}
