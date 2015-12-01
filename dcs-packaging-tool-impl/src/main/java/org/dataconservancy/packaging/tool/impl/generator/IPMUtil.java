/*
 * Copyright 2015 Johns Hopkins University
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

package org.dataconservancy.packaging.tool.impl.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import org.dataconservancy.packaging.tool.model.ipm.FileInfo;
import org.dataconservancy.packaging.tool.model.ipm.Node;

import static org.apache.commons.codec.digest.DigestUtils.shaHex;

public class IPMUtil {

    public static String path(Node node, String suffix) {
        Deque<String> pathStack = new ArrayDeque<>();

        pathStack.push(suffix);
        pathStack.push(name(node));

        while (!node.isRoot()) {
            node = node.getParent();
            if (node.getFileInfo() != null) {
                pathStack.push("/");
                pathStack.push(name(node));
            }
        }

        StringBuilder builder = new StringBuilder();
        pathStack.forEach(builder::append);
        return builder.toString();

    }

    public static String name(Node node) {
        return Optional.ofNullable(node.getFileInfo()).map(FileInfo::getName)
                .orElse(shaHex(node.getIdentifier().toString()));
    }

}
