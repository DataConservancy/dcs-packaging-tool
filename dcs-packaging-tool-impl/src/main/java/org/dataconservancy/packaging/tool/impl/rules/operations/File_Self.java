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

import org.dataconservancy.packaging.tool.impl.rules.FileContext;
import org.dataconservancy.packaging.tool.impl.rules.FileContextImpl;
import org.dataconservancy.packaging.tool.impl.rules.FileOperation;
import org.dataconservancy.packaging.tool.impl.rules.TestOperation;


public class File_Self implements FileOperation {
    private TestOperation<?>[] constraints = new TestOperation<?>[0];

    @Override
    public void setConstraints(TestOperation<?>... constraints) {
        this.constraints = constraints;
    }

    @Override
    public File[] operate(FileContext fileContext) {
        if (fileContext.getFile() == null || !fileContext.getFile().exists() || !fileContext.getFile().canRead()) {
            throw new OperationException("Pathname " + fileContext.getFile().getPath()
                    + " denotes a file that does not exist or cannot be read.");
        }

        if (constraints.length > 0) {
            for (TestOperation<?> constraint : constraints) {
                for (boolean truthValue : constraint
                        .operate(new FileContextImpl(fileContext.getFile(), fileContext
                                .getRoot(), fileContext.isIgnored()))) {
                    if (!truthValue) {
                        return new File[0];
                    }
                }
            }
        }

        return new File[] {fileContext.getFile()};
    }
}
