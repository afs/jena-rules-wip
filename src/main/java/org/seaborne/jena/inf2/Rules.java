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

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class Rules {
    
    /** Intensional rules = occurs in the head of a rule.
     * Returns a set of Triples with "canonical relations": ANY for variables.
     */
    public static Set<Triple> intensionalRelations(Collection<Rule> rules) {
        return heads(rules) ;
    }
    
    private static Set<Triple> heads(Collection<Rule> rules) {
        return rules.stream()
            .map(r->r.getHead())
            .map(t->unvar(t))
            .collect(Collectors.toSet()) ;
    }
    
    /** Extensional relationship = relation occuring only in the body of rules.*/
    public static Set<Triple> extensionalRelations(Collection<Rule> rules) {
        Set<Triple> headRel = heads(rules);
        // Body rules filtered by head relationships.
        return 
            canonicalBodyRelationships(rules)
            .filter(t -> !headRel.contains(t))
            .collect(Collectors.toSet());
    }

    /** The body relationships, with variables as ANY */
    private static Stream<Triple> canonicalBodyRelationships(Collection<Rule> rules) {
        return rules.stream()
            .flatMap(r->r.getBody().stream())
            .map(Rules::unvar);
    }
    
    private static Function<Node, Node> fUnvar = (n)-> ( n.isVariable() ?  Node.ANY : n) ;
    
    /** Triple with ANY, not variables. */
    public static Triple unvar(Triple triple) {
        if ( check(triple.getSubject()) && check(triple.getPredicate()) && check(triple.getObject()) )
            return triple;
        
        return
            Triple.create(fUnvar.apply(triple.getSubject()),
                          fUnvar.apply(triple.getPredicate()),
                          fUnvar.apply(triple.getObject()) );
    }

    private static boolean check(Node node) {
        return Node.ANY.equals(node) || node.isConcrete();
    }
}
