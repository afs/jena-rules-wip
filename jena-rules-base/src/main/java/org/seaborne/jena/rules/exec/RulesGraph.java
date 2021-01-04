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

package org.seaborne.jena.rules.exec;

import java.util.Iterator;
import java.util.stream.Stream;

import migrate.binding.Binding;
import migrate.binding.Sub;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.RulesEngine;

public class RulesGraph extends GraphWrapper {

    private final RulesEngine rulesEngine;

    public RulesGraph(Graph graph, RulesEngine rulesEngine) {
        super(graph);
        this.rulesEngine = rulesEngine;
    }

    public RulesEngine getEngine() {
        return rulesEngine;
    }

    @Override
    public ExtendedIterator<Triple> find(Triple triple) {
        return find(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Node sq = queryNode(s, "sq");
        Node pq = queryNode(p, "pq");
        Node oq = queryNode(o, "oq");

        Rel query = new Rel("", sq, pq, oq);
        Stream<Binding> matches = rulesEngine.solve(query);

        Iterator<Triple> iter = matches.map(b->{
            Node s2 = Sub.substitute(b, sq);
            Node p2 = Sub.substitute(b, pq);
            Node o2 = Sub.substitute(b, oq);
            return Triple.create(s2,  p2,  o2);
        }).iterator();

        //iter = Iter.log(iter);

        return WrappedIterator.create(iter);
    }

    private static Node queryNode(Node n, String string) {
        return (n == null || n ==  Node.ANY) ? Var.alloc(string) : n ;
    }

    @Override
    public boolean contains(Node s, Node p, Node o) {
        return get().contains(s, p, o);
    }

    @Override
    public boolean contains(Triple t) {
        return get().contains(t);
    }

    // Updates.

}

