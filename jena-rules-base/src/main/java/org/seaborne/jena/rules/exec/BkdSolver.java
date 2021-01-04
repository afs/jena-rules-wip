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

import static java.lang.String.format;

import java.util.*;
import java.util.function.Function;

import migrate.binding.Binding;
import migrate.binding.BindingBuilder;
import migrate.binding.BindingFactory;
import migrate.binding.Sub;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.seaborne.jena.rules.*;

public class BkdSolver {
    // Solving.
    // To match (a b c) we need to look in the data to if there are direct matches,
    // and also see if there are any rules that can be used to produce matches.
    //
    // Crude - does not handle:
    //   Left recursion.
    //   Shared rules

    /**
     * For a non-recursive ruleset, rewrite pattern to become an expression of {@link Rel Rels} that are database accesses.
     */
    public static void expand(Rel pattern, RuleSet ruleSet) {
        // Should be a conjunction-disjunction tree
        // Rename variables.
    }

    /**
     * Solve a single rel pattern.
     * Produces bindings for the pattern.
     * Data is on argument {@code relStore}, rules in {@code ruleSet}.
     */
    public static Iterator<Binding> solver(Rel pattern, RuleSet ruleSet, RelStore relStore) {
        RuleExecCxt rCxt = RuleExecCxt.global;
        if ( RuleExecCxt.global.debug() )
            rCxt.out().printf("SLD/NR(%s)\n", pattern);
        // XXX Check for recursion.
        //ruleSet.isRecursive();
        try {
            return solver(RuleExecCxt.global, 1, BindingFactory.root(), pattern, ruleSet, relStore);
        } finally { rCxt.out().flush(); }
    }

    /** The main solver - this is called recursively.
     * @param rCxt TODO*/
    private static Iterator<Binding> solver(RuleExecCxt rCxt, int depth, Binding input, Rel pattern, RuleSet ruleSet, RelStore relStore) {
        if ( rCxt.debug() ) {
            rCxt.out().println(">> Level "+depth);
            rCxt.out().println("   Pattern: "+pattern);
        }
        Rel pattern1 = Sub.substitute(input, pattern);

        // Non-recursive => at most one deep for each rule in the ruleset
        // Max depth is RuleSet.size

        if ( depth > ruleSet.size()+1 )
            throw new RulesException(format("Too deep: depth = %d, RuleSet size = %d", depth, ruleSet.size()));

        // Data/EDB
        Iterator<Binding> matches1 = dataSolver(rCxt, input, pattern1, relStore);
        if ( rCxt.debug() )
            matches1 = RulesLib.debug(rCxt.out(), depth, "Data", matches1);

        // Rules/IDB
        Iterator<Binding> matches2 = ruleSetSolver(rCxt, depth, input, pattern1, ruleSet, relStore);
        if ( rCxt.debug() )
            matches2 = RulesLib.debug(rCxt.out(), depth, "Rules", matches2);

        Iterator<Binding> matches = Iter.concat(matches1, matches2);
        if ( rCxt.debug() ) rCxt.out().println("<< Level "+depth);
        if ( matches == null )
            return Iter.nullIterator();
        return matches;
    }

    /** Find matching rules and resolve. */
    private static Iterator<Binding> ruleSetSolver(RuleExecCxt rCxt, int depth, Binding input, Rel pattern1, RuleSet ruleSet, RelStore relStore) {
        List<Rule> matchingRules = findCompatible(ruleSet, pattern1);

        Iterator<Binding> chain = null;
        for ( Rule rule: matchingRules ) {
            if ( rCxt.debug() )
                rCxt.out().println("Rule: "+rule);
            Iterator<Binding> chain1 = ruleSolver(rCxt, depth, input, pattern1, rule, ruleSet, relStore);
            chain = Iter.concat(chain,  chain1);
        }
        return chain;
    }

    /**
     * Find matching rules: A rule is compatible if it would contribute to the pattern.
     *  Variable names are not considered.
     */
    private static List<Rule> findCompatible(RuleSet ruleSet, Rel pattern) {
        List<Rule> compat = new ArrayList<>();
        for ( Rule rule: ruleSet.asList() ) {
            if ( compatible(rule, pattern) )
                compat.add(rule);
        }
        return compat;
    }

    /** Resolve one rule. */
    private static Iterator<Binding> ruleSolver(RuleExecCxt rCxt, int depth, Binding input, Rel pattern1, Rule rule, RuleSet ruleSet, RelStore relStore) {
        Binding mgu = MGU.mgu(pattern1, rule.getHead());
        if ( rCxt.debug() )
            rCxt.out().println("MGU: "+mgu);
        if ( mgu == null )
            return null;
        Rel pattern2 = MGU.applyMGU(mgu, pattern1);
        if ( ! compatible(rule, pattern2) )
            return null;
        // Start is the head binding.
        //XXX Input and mgu
        // Need combine input and mgu.

        Iterator<Binding> chain = Iter.singleton(input);

        for ( Rel relBody_ : rule.getBody() ) {
            if ( rCxt.debug() ) rCxt.out().println("Body: "+relBody_);
            Rel relBody = Sub.substitute(mgu, relBody_);
            if ( rCxt.debug() ) rCxt.out().println("Body: "+relBody);
            chain = Iter.flatMap(chain, binding->solver(null, depth+1, binding, relBody, ruleSet, relStore));
            if ( rCxt.debug() ) {
                rCxt.out().flush();
                chain = Iter.materialize(chain); // rCxt.debug()
            }
        }

        if ( rCxt.debug() )
            chain = RulesLib.debug(rCxt.out(), depth, "Rule chain", chain);
        // Rename from rule variable names to pattern variable names.
        // XXX Ignore ?x ?x
        // Rewrite version.

        Rel relHead = Sub.substitute(mgu, rule.getHead());
        Function<Var,Var> headProject = mapRelFromTo(relHead, pattern2);
        Function<Binding, Binding> resultMapper = b1 -> {
            BindingBuilder b2 = BindingFactory.create(input);
            // @@ Binding.forEach(BiConsumer)
            b1.forEach((v,n)->{
                Var v2 = headProject.apply(v);
                if ( v2 != null )
                    b2.add(v2, n);
            });
            return b2.build();
        };

        chain = Iter.iter(chain).map(resultMapper);
        // UGLY
        //Better? Add to binding early?
        chain = Iter.iter(chain).map(b->(new BindingBuilder(mgu)).addAll(b).build());
        return chain;
    }

    /**
     * For two {@link #compatible(Rel,Rel)} rels,
     * create mapping from the first arg to the second arg.
     * This is also a projection (only dstRel vars will show up).
     * Returns null on "no mapping".
     */
    private static Function<Var,Var> mapRelFromTo(Rel srcRel, Rel dstRel) {
        // MGU
        // XXX Testable!
        // XXX Overkill : by number of slots version.
        Map<Var, Var> mapping = new HashMap<>();
        if ( srcRel.len() != srcRel.len())
            throw new InternalErrorException(format("mapRelFromTo: %d / %d", srcRel.len() != dstRel.len()));
        int N = srcRel.len();
        //map(srcRel)

        for ( int i = 0 ; i<N ; i++ ) {
            Node src = srcRel.getTuple().get(i);
            Node dst = dstRel.getTuple().get(i);
            if ( Var.isVar(src) && Var.isVar(dst) ) {
                mapping.put(Var.alloc(src), Var.alloc(dst));
                continue;
            }
            if ( ! Var.isVar(src) && !Var.isVar(dst) )
                continue;
            throw new InternalErrorException(format("mapRelFromTo: tuple not compatible src=%s dst=%s", srcRel, dstRel));
        }
        //System.rCxt.out().println(mapping+" "+srcRel+" -> "+dstRel);
        return v -> mapping.getOrDefault(v, null);
    }

    /**
     * Determine if the pattern (which may itself have variables) might match the rule head.
     * That is, any slots where each has a constant, match?
     * No consideration of variable names.
     * @param patten
     * @param data
     * @return
     */
    private static boolean compatible(Rule rule, Rel pattern) {
        Rel targetRel = rule.getHead();
        Rel srcRel = pattern;
        if( targetRel.len() != srcRel.len() )
            return false;
        if( ! targetRel.getName().equals(srcRel.getName()) )
            return false;

//        return compatible(rule.getHead(), pattern);
//    }
//
//    /**
//     * Rel compatibility.
//     * <ul>
//     * <li> Ary-agrees
//     * <li> Do const-const match?
//     * </ul>
//     */
//    private static boolean compatible(Rel targetRel, Rel srcRel) {
        // To match:
        //   Do const-const match?
        //   Can we bind a data pattern to a rule const?
        //   Can we ground the rule use a const of the data?

        //return compatible(head, rel);
        Tuple<Node> tuple1 = targetRel.getTuple();
        Tuple<Node> tuple2 = srcRel.getTuple();

        if( tuple1.len() != tuple2.len() )
            return false;
        // Const-const match.
        for ( int i = 0 ; i < tuple1.len(); i++ ) {
            Node n1 = tuple1.get(i);
            Node n2 = tuple2.get(i);
            if ( ! constCompatible(n1,n2) )
                return false;
        }

        return true;
    }

    private static boolean constCompatible(Node n1 /*rule*/, Node n2 /*pattern*/) {
        if ( Var.isVar(n1) || Var.isVar(n2) )
            return true;
        // Both ground - must match.
        return n1.equals(n2);
    }

    /** Evaluate a pattern against ground data in a {@link RelStore}. */
    public static Iterator<Binding> dataSolver(RuleExecCxt rCxt, Binding input, Rel pattern1, RelStore relStore) {
        Iterator<Rel> iter = relStore.find(pattern1);
        return RulesLib.bindings(iter, pattern1);
    }
}

