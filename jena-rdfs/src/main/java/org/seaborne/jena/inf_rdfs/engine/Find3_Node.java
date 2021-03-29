/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.seaborne.jena.inf_rdfs.engine;

import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.seaborne.jena.inf_rdfs.SetupRDFS;

public class Find3_Node extends Find3_X<Node, Triple> {

    private final Graph graph;

    public Find3_Node(SetupRDFS<Node> setup, Graph graph) {
        super(setup, Mappers.mapperNode);
        this.graph = graph;
    }

    @Override
    public Stream<Triple> sourceFind(Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s,p,o);
        Stream<Triple> stream = Iter.asStream(iter);
        return stream;
    }

    @Override
    protected boolean contains(Node s, Node p, Node o) {
        return graph.contains(s, p, o);
    }
}
