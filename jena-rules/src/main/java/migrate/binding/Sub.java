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

package org.seaborne.jena.rules;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class Sub {

    // See "Substitute"

    /** Substitute for variables in a {@link Rule} */
    public static Rule substitute(Binding binding, Rule rule) {
        Rel h = substitute(binding, rule.getHead());
        List<Rel> b = rule.getBody().stream().map(rel->substitute(binding,rel)).collect(toList());
        return new Rule(h, b);
    }

    /** Substitute for variables in a {@link Rel} */
    public static Rel substitute(Binding binding, Rel rel) {
        // XXX Don't create new object if no change.
        if ( binding.isEmpty() )
            return rel;
        int N = rel.getTuple().len();
        Node v[] = new Node[rel.getTuple().len()];
        for(int i = 0 ; i < N ; i++) {
            v[i] = substitute(binding, rel.getTuple().get(i));
        }
        return new Rel(rel.getName(), TupleFactory.asTuple(v));
    }

    /** Substitute for a variable. */
    public static Node substitute(Binding binding, Node node) {
        return lookup(binding, node);
    }

    public static Node substitute(Node node, Binding binding) {
        if ( ! node.isNodeTriple() )
            return lookup(binding, node);
        if ( node.isConcrete() )
            return node;
        // Node_Triple with variables.
        Triple triple = node.getTriple();
        Node s = triple.getSubject();
        Node p = triple.getPredicate();
        Node o = triple.getObject();

        // New values.
        Node s1 = subTripleTermNode(s, binding);
        Node p1 = subTripleTermNode(p, binding);
        Node o1 = subTripleTermNode(o, binding);

        // No change - return original
        if ( s1 == s && o1 == o && p1 == p )
            return node;

        // Change. Create new.
        return NodeFactory.createTripleNode(s1, p1, o1);
    }

    /** Substitute for a node that makes up a triple in a Node_Triple. Recursively. */
    private static Node subTripleTermNode(Node n, Binding binding) {
        if ( n.isNodeTriple() ) {
            if ( ! n.isConcrete() )
                n = substitute(n, binding);
        } else if ( Var.isVar(n) ) {
            Var var = Var.alloc(n);
            n = lookup(binding, n);
        }
        return n;
    }

    /** Return the value in the binding (if node is a Var) or the node itself. */
    private static Node lookup(Binding binding, Node node) {
        if ( binding.isEmpty() )
            return node;
        if ( !Var.isVar(node) )
            return node;
        Var var = Var.alloc(node);
        Node n = lookup(binding, var);
        return n;
    }

    /** Return the value in the binding or the variable itself. */
    private static Node lookup(Binding binding, Var var) {
        Node n = binding.get(var);
        if ( n != null )
            return n;
        return var;
    }

}

