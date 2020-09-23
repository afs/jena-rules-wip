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

import static java.lang.String.format;

import java.util.*;
import java.util.function.Function;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

public class RelSolver {
    // Solving.
    // To match (a b c) we need to look in the data to if there are direct matches,
    // and also see if there are any rules that can be used to produce matches.
    //
    // Crude - does not handle:
    //   Left recursion.
    //   Cache intermediate and shared rule solving (tabling)

    /**
     * Solve a single rel pattern.
     * Produces bindings for the pattern.
     * Data is on argument {@code relStore}, rules in {@code ruleSet}.
     */
    public static Iterator<Binding> solver(Binding input, Rel pattern, RelStore relStore, RuleSet ruleSet) {
        IndentedWriter out = IndentedWriter.clone(IndentedWriter.stdout);
        out.setFlushOnNewline(true);
        try {
            return solver(out, 1, input, pattern, relStore, ruleSet);
        } finally { out.flush(); }
    }

    private static boolean DEBUG = false ;

    /** Worker */
    private static Iterator<Binding> solver(IndentedWriter out, int depth, Binding input, Rel pattern, RelStore relStore, RuleSet ruleSet) {

        if ( DEBUG ) out.println(">> Level "+depth);
        Rel rel1 = substitute(pattern, input);

        // Concatenation of all parts.
        Iterator<Binding> matches = null;

        for ( Rule rule: ruleSet.asList() ) {
            // Is this rule applicable?
            // "Compatible" means ground terms in the pattern match.
            if ( ! compatible(rule, pattern) )
                continue;
            if ( DEBUG ) out.println("Rule: "+rule);
            Iterator<Binding> chain = Iter.singleton(input);
            for ( Rel relBody : rule.getBody() ) {
                chain = Iter.flatMap(chain, binding->solver(out, depth+1, binding, relBody, relStore, ruleSet));
                if ( DEBUG )
                    chain = Iter.materialize(chain); // DEBUG
                //chain = debug(out, "rule "+relBody, chain);
            }
            //chain = debug(out, "rule "+rule, chain);
            // Rename from rule variable names to pattern variable names.
            // XXX Ignore ?x ?x
            // Rewrite version.
            Function<Var,Var> headProject = mapRelFromTo(rule.getHead(), rel1);
            Function<Binding, Binding> resultMapper = b1 -> {
                BindingMap b2 = BindingFactory.create(input);
                // @@ Binding.forEach(BiConsumer)
                b1.vars().forEachRemaining(v->{
                    Node n = b1.get(v);
                    Var v2 = headProject.apply(v);
                    if ( v2 != null )
                        b2.add(v2, n);
                });
                return b2;
            };

            chain = Iter.iter(chain).map(resultMapper);
            //chain = debug(out, "mapped ", chain);
            matches = Iter.concat(matches,  chain);
        }

        // Solve concretely.
        Iterator<Binding> concrete = solveData(input, pattern, relStore);
        //concrete = debug(out, "data "+pattern, concrete);
        if ( concrete != null && concrete.hasNext() ) {
            matches = Iter.concat(concrete, matches);
        }
        if ( DEBUG )
            matches = debug(depth, out, "pattern "+pattern, matches);
        if ( DEBUG ) out.println("<< Level "+depth);
        return matches;
    }

    /**
     * For two {@link #compatible(Rel,Rel)} rels,
     * create mapping from the first arg to the second arg.
     * This is also a projection (only dstRel vars will show up).
     * Returns null on "no mapping".
     */
    private static Function<Var,Var> mapRelFromTo(Rel srcRel, Rel dstRel) {
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
            if ( ! Var.isVar(src) && !Var.isVar(src) )
                continue;
            throw new InternalErrorException(format("mapRelFromTo: tuple not compatible %s %s", srcRel, dstRel));
        }
        //System.out.println(mapping+" "+srcRel+" -> "+dstRel);
        return v -> mapping.getOrDefault(v, null);
    }

    /**
     * Determine if the pattern (which may itself have variables) might match the rule head.
     * That is, any slots where each has a constant, match?
     * No consdieration of variable names.
     * @param patten
     * @param data
     * @return
     */
    private static boolean compatible(Rule rule, Rel pattern) {
        Rel targetRel = rule.getHead();
        Rel srcRel = pattern;
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


    /** Evaluate a pattern against ground data in a data RelStore. */
    public static Iterator<Binding> solveData(Binding input, Rel pattern, RelStore relStore) {
        Rel rel1 = substitute(pattern, input);
        Iterator<Rel> iter = relStore.find(rel1);
        Function<Rel, Binding> m = mapper(rel1, input);
        return Iter.iter(iter).map(m).removeNulls();
    }

    /**
     * Create mapping function that converts a rel pattern to a binding, given an existing parent.
     */
    private static Function<Rel, Binding> mapper(Rel pattern, Binding parent) {
        Tuple<Node> patternTuple = pattern.getTuple();
        int N = patternTuple.len();
        Function<Rel, Binding> m = r->{
            BindingMap b = BindingFactory.create(parent);
            for ( int i = 0 ; i < N ; i++ ) {
                Node n = patternTuple.get(i);
                if ( Var.isVar(n) ) {
                    Var var = Var.alloc(n);
                    Node value = r.getTuple().get(i);
                    if ( b.contains(var)) {
                        Node v = b.get(var);
                        if ( v.equals(value) )
                            continue;
                        else
                            return null;
                    } else {
                        b.add(var, value);
                    }
                }
            }
            if ( b.isEmpty() )
                return null;
            return b;
        };
        return m;
    }

    /** Calculate rel, from a pattenr, grounding any variables that are in the binding. */
    private static Rel substitute(Rel pattern, Binding binding) {
        // Some neat way to do rel->rel or tuple->tuple
        Tuple<Node> tuple = pattern.getTuple();
        Tuple<Node> tuple2 = map(tuple, x->substituteVar(x, binding));
        return new Rel(pattern.getName(), tuple2);
    }

    // ==> Tuple::map
    // Add to Tuple - do specials for Tuple 0,1,2,3,...
    private static <X,Y> Tuple<Y> map(Tuple<X> tuple, Function<X,Y> function) {
        //@SuppressWarnings("unchecked")
        //Y[] t = (Y[])new Object[len()];
        int N = tuple.len();
        List<Y> t = new ArrayList<>(N);
        for(int i = 0 ; i < N ; i++ ) {
            t.add(function.apply(tuple.get(i)));
        }
        return TupleFactory.create(t);
    }

    /**
     * Return the value for a node, which maybe a variable, given a binding.
     * If it is variable and the binding does not have a mapping for that variable,
     * return the variables as-is.
     */
    private static Node substituteVar(Node node, Binding binding)
    {
        if ( Var.isVar(node) ) {
            Node x = binding.get(Var.alloc(node)) ;
            if ( x != null )
                return x ;
        }
        return node ;
    }

    private static Iterator<Binding> debug(int depth, IndentedWriter out, String label, Iterator<Binding> iter) {
        List<Binding> c = Iter.toList(iter);
        out.println(depth+" "+label+" :: ");
        c.forEach(b->out.printf("%d   %s\n",depth, b));

        iter = c.iterator();
        //iter = Iter.debug(iter);
        return iter;
    }
}

