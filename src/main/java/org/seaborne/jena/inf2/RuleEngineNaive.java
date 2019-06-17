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

package org.seaborne.jena.inf2;

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;

/** Naive algorithm - repeatedly iterate until no change */ 
public class RuleEngineNaive implements RuleEngine {
    private final Graph graph;
    
    public RuleEngineNaive(Graph g, RuleSet rules) {
        graph = GraphFactory.createGraphMem();
        GraphUtil.addInto(graph, g);
        Forwards.evalNaive(graph, rules.asList());
    }

    @Override
    public Iterator<Triple> match(Triple pattern) {
        return graph.find(pattern);
    }
}
