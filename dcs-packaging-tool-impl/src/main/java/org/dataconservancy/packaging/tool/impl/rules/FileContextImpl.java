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

import java.io.File;

/** Simple file context impl */
public class FileContextImpl
        implements FileContext {

    private final File root;

    private final File file;

    private boolean ignored;

    public FileContextImpl(File file, File root, boolean ignored) {
        this.file = file;
        this.root = root;
        this.ignored = ignored;
    }

    public FileContextImpl(File file, boolean ignored) {
        this.file = file;
        this.root = null;
        this.ignored = ignored;
    }

    @Override
    public File getRoot() {
        return root;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean isIgnored() {
        return ignored;
    }

    @Override
    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    public int hashCode() {
        return file.hashCode() + (root == null ? 1 : root.hashCode()) + (ignored ? 0 : 1);
    }

    public boolean equals(Object o) {
        if (o != null && o instanceof FileContext) {
            FileContext cxt = (FileContext) o;

            if (file.equals(cxt.getFile())) {
                if (root == null && cxt.getRoot() == null || root != null
                        && root.equals(cxt.getRoot())) {
                    return ignored == cxt.isIgnored();
                }
            }
        }

        return false;
    }

}
