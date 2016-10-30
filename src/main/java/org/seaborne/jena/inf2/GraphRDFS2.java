/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.inf2;

import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * RDFS graph over a plain base graph.
 */
public class GraphRDFS2 extends GraphWrapper {

    private final Graph g;
    private final Graph g1;
    private final Graph g2;

    public GraphRDFS2(Graph graph) {
        super(graph); // Does not trigger.
        g = graph;
        // New stuff ...
        g1 = GraphFactory.createDefaultGraph();
        g2 = new Union(g1, g);
        List<Rule> rules = Rules.rulesRDFS().asList();
        Forwards.evalNaive(graph, rules);

    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        return g2.find(s, p, o);
    }
}
