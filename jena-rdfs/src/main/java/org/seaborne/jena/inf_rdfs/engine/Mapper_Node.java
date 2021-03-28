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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class Mapper_Node implements MapperX<Node, Triple> {
    static MapperX<Node, Triple> mapperSingleton = new Mapper_Node();

    private Mapper_Node() {}

    @Override
    public Node fromNode(Node n) {
        return n;
    }

    @Override
    public Node toNode(Node x) {
        return x;
    }

    @Override
    public Node subject(Triple triple) {
        return triple.getSubject();
    }

    @Override
    public Node predicate(Triple triple) {
        return triple.getPredicate();
    }

    @Override
    public Node object(Triple triple) {
        return triple.getObject();
    }

    @Override
    public Triple create(Node s, Node p, Node o) {
        return Triple.create(s, p, o);
    }
}
