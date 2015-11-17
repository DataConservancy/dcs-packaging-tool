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

package org.dataconservancy.packaging.tool.model.ipm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Builds a {@link Stream} of {@code Node}.  There is probably a more efficient way to do this.
 */
class NodeStreamBuilder implements Stream.Builder<Node> {

    private List<Node> nodes = new ArrayList<>();

    @Override
    public void accept(Node node) {
        nodes.add(node);
    }

    @Override
    public Stream<Node> build() {
        return nodes.stream();
    }

}

