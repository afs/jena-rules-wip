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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;

public class Rules {

    /**
     * Intensional rules = occurs in the head of a rule. Returns a set of Triples with
     * "canonical relations": ANY for variables.
     */
    public static Set<Rel> intensionalRelations(Collection<Rule> rules) {
        return heads(rules);
    }

    private static Set<Rel> heads(Collection<Rule> rules) {
        return rules.stream().map(r -> r.getHead()).map(t -> unvar(t)).collect(Collectors.toSet());
    }

    /** Extensional relationship = relation occurring only in the body of rules. */
    public static Set<Rel> extensionalRelations(Collection<Rule> rules) {
        Set<Rel> headRel = heads(rules);
        // Body rules filtered by head relationships.
        return canonicalBodyRelationships(rules).filter(t -> !headRel.contains(t)).collect(Collectors.toSet());
    }

    /** The body relationships, with variables as ANY */
    private static Stream<Rel> canonicalBodyRelationships(Collection<Rule> rules) {
        return rules.stream().flatMap(r -> r.getBody().stream()).map(Rules::unvar);
    }

    private static Function<Node, Node> fUnvar = (n) -> (n.isVariable() ? Node.ANY : n);

    /** {@link Rel} with ANY, not variables. */
    public static Rel unvar(Rel r) {
        if ( r.getTuple().stream().allMatch(Rules::check) )
            return r ;
        // Tuple::map??
        List<Node> x = r.getTuple().stream().map(n->fUnvar.apply(n)).collect(Collectors.toList());
        return new Rel(r.getName(), TupleFactory.create(x));
    }

    private static boolean check(Node node) {
        return Node.ANY.equals(node) || node.isConcrete();
    }

//    public static Rule rule(String _head, String..._body) {
//        Triple head = SSE.parseTriple(_head) ;
//        List<Triple> body = new ArrayList<>(_body.length) ;
//        for ( String x : _body ) {
//            body.add(SSE.parseTriple(x)) ;
//        }
//        return new Rule(head, body) ;
//    }
//    
//    public static RuleSet rulesRDFS() {
//        List<Rule> rules = new ArrayList<>();
//        rulesRDFS1(rules);
//        rulesRDFS2(rules);
//        rulesRDFS3(rules);
//        return new RuleSet(rules);
//    }
//
//    // rdfs-min.rules : RDFS with no axioms, no rdf:Property.
//
//    // Range and Domain
//    private static void rulesRDFS1(List<Rule> rules) {
//        // Gulp. This is horrendous. 
//        Rule rule1 = rule("(?s rdf:type ?T)", "(?s ?p ?x)", "(?p rdfs:domain ?T)");
//        Rule rule2 = rule("(?o rdf:type ?T)", "(?s ?p ?o)", "(?p rdfs:range  ?T)");
//        rules.add(rule2);
//        rules.add(rule1);
//    }
//
//    // SubClass and SubProperty
//    private static void rulesRDFS2(List<Rule> rules) {
//        Rule rule1 = rule("(?s rdf:type ?T)", "(?s rdf:type ?TX )", "(?TX rdfs:subClassOf ?T)");
//        Rule rule2 = rule("(?t1 rdfs:subClassOf ?t2)", "(?t1 rdfs:subClassOf ?X)", "(?X rdfs:subClassOf ?t2)");
//
//        Rule rule3 = rule("(?s ?q ?o)", "(?s ?p ?o )", "(?p rdfs:subPropertyOf ?q)");
//        Rule rule4 = rule("(?p1 rdfs:subPropertyOf ?p2)", "(?p1 rdfs:subPropertyOf ?X)", "(?X rdfs:subPropertyOf ?p2)");
//
//        rules.add(rule1);
//        rules.add(rule2);
//        rules.add(rule3);
//        rules.add(rule4);
//    }
//
//    // Other
//    /* package */ static void rulesRDFS3(List<Rule> rules) {
//        Rule rule1 = rule("(?X rdfs:subClassOf ?X)", "(?Y rdfs:subClassOf ?X )");
//        Rule rule2 = rule("(?X rdfs:subClassOf ?X)", "(?X rdfs:subClassOf ?Y )");
//        Rule rule3 = rule("(?X rdfs:subClassOf ?X)", "(?Y rdf:type ?X)");
//        rules.add(rule1);
//        rules.add(rule2);
//        rules.add(rule3);
//        Rule rule4 = rule("(?X rdfs:subPropertyOf ?X)", "(?Y rdfs:subPropertyOf ?X )");
//        Rule rule5 = rule("(?X rdfs:subPropertyOf ?X)", "(?X rdfs:subPropertyOf ?Y )");
//        rules.add(rule4);
//        rules.add(rule5);
//    }

}
