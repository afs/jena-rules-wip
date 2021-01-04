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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;

public class RuleOps {

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
        return rules.stream().flatMap(r -> r.getBody().stream()).map(RuleOps::unvar);
    }

    private static Function<Node, Node> fUnvar = (n) -> (n.isVariable() ? Node.ANY : n);

    /** {@link Rel} with ANY, not variables. */
    public static Rel unvar(Rel r) {
        if ( r.getTuple().stream().allMatch(RuleOps::check) )
            return r ;
        // Tuple::map??
        List<Node> x = r.getTuple().stream().map(n->fUnvar.apply(n)).collect(Collectors.toList());
        return new Rel(r.getName(), TupleFactory.create(x));
    }

    private static boolean check(Node node) {
        return Node.ANY.equals(node) || node.isConcrete();
    }

    /** Return the rules that have a head clause that provides the given relation. */
    public static Collection<Rule> provides(Rel rel, RuleSet ruleSet) {
        List<Rule> array = new ArrayList<>();
        // find all rules in the RuleSet that provide the rule
        // EFFICENCY NEEDED
        for ( Rule r : ruleSet ) {
            if ( provides(rel, r.getHead()) ) {
                array.add(r);
            }
        }
        return array;
    }

    /**
     * Does {@code target} help to solve {@code src}? True if the relation names
     * match, the arities match, and arguments match.
     */
    public static boolean provides(Rel target, Rel src) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(src);
        if ( ! target.getName().equals(src.getName()) )
            return false;
        Tuple<Node> tTarget = target.getTuple();
        Tuple<Node> tSrc = src.getTuple();
        if ( tTarget.len() != tSrc.len() )
            return false;
        for ( int i = 0 ; i < tTarget.len() ; i++ ) {
            Node targetNode = tTarget.get(i);
            Node srcNode = tSrc.get(i);
            if ( ! provides(targetNode, srcNode) )
                return false;
        }
        return true;
    }

    /**
     * Does {@code srcNode} (arg2) provide for {@code targetNode}.
     * <ul>
     * <li>Both variables.
     * <li>Target is a variable and src is a constant
     * <li>Target is a constant and src is a variable
     * <li>Same constants
     * </ul>
     */
    private static boolean provides(Node targetNode, Node srcNode) {
        if ( targetNode.isVariable() || srcNode.isVariable() )
            return true;
        // Ground terms match.
        return targetNode.equals(srcNode) ;
    }
}
