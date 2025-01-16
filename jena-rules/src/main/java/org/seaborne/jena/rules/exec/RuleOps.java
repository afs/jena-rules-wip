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

import static org.apache.jena.sparql.core.Var.isVar;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.main.QC;
import org.seaborne.jena.rules.*;

public class RuleOps {

    /** RelStore.equals - unordered */
    public static boolean equals(RelStore relStore1, RelStore relStore2) {
        Set<Rel> set1 = StreamOps.toSet(relStore1.stream());
        Set<Rel> set2 = StreamOps.toSet(relStore2.stream());
        return set1.equals(set2);
    }

//    public static boolean isRecursive(Rel head, RuleSet ruleSet) {
//        // DependencyGraph
//    }

    /** Evaluate one rule using EDB 'data' and putting changes into 'acc' */
    public static void evalOne(RuleExecCxt rCxt, RelStore data, Collection<Rel> acc, Rule rule) {
        List<Rel> body = rule.getBody();
        if ( body.isEmpty() ) {
            // Assert head.
            emit(rCxt, acc, rule.getHead(), data);
            return;
        }
        Iterator<Binding> chain = Iter.singletonIterator(BindingFactory.root());
        for(Rel rel: body) {
            chain = step(data, rel, chain);
        }
        if ( rCxt.debug() )
            chain = debug("chain", chain);

        chain.forEachRemaining(soln->{
            Rel x = Sub.substitute(soln, rule.getHead());
            emit(rCxt, acc, x, data);
        });

    }

    /**
     * Map a stream of data items to a stream of bindings according to the {@code queryPattern}.
     */
    public static Stream<Binding> bindings(Stream<Rel> dataMatches, Rel queryPattern) {
        return bindings(dataMatches, queryPattern, Binding.noParent);
    }

    /**
     * Map a stream of data items to a stream of bindings according to the {@code queryPattern}.
     */
    public static Stream<Binding> bindings(Stream<Rel> dataMatches, Rel queryPattern, Binding parent) {
        Function<Rel, Binding> mapFunction = mapper(queryPattern, parent);
        return dataMatches.map(mapFunction).filter(Objects::nonNull);
    }

    /**
     * Map an iterator of data items to a stream of bindings according to the {@code queryPattern}.
     */
    public static Iterator<Binding> bindings(Iterator<Rel> dataMatches, Rel queryPattern) {
        return bindings(dataMatches, queryPattern, Binding.noParent);
    }

    /**
     * Map a iterator of data items to a stream of bindings according to the {@code queryPattern}.
     */
    public static Iterator<Binding> bindings(Iterator<Rel> dataMatches, Rel queryPattern, Binding parent) {
        Function<Rel, Binding> mapFunction = mapper(queryPattern, parent);
        return Iter.iter(dataMatches).map(mapFunction).filter(Objects::nonNull);
    }


    /**
     * Create mapping function that converts a rel pattern to a binding, given an existing parent.
     * The created function returns null if no bindings because of an attempt to bind twice with different values.
     * e.g. Pattern r(?x, ?x) and data atom r(1,2) -- {@code ?x} can not be bound consistently.
     */
    public static Function<Rel, Binding> mapper(Rel pattern, Binding parent) {
        int N = pattern.len();
        // Do as much work outside the function as possible.
        Tuple<Node> patternTuple = pattern.getTuple();
        Tuple<Var> vars = map(patternTuple, n -> (Var.isVar(n))?Var.alloc(n):null);
        BindingBuilder b = Binding.builder(parent);

        Function<Rel, Binding> m = r->{
            b.reset();
            for ( int i = 0 ; i < N ; i++ ) {
                Var var = vars.get(i);
                if ( var == null )
                    continue;
                Node value = r.get(i);
                if ( b.contains(var)) {
                    if ( var.equals(value) )
                        // Repeated use of variable, compatible values.
                        continue;
                    else
                        // Repeated use of variable incompatible values.
                        return null;
                } else {
                    b.add(var, value);
                }
            }
            return b.build();
        };
        return m;
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

    private static Iterator<Binding> step(RelStore data, Rel rel, Iterator<Binding> chain) {
        //Iter<Binding> foo = Iter.iter(chain);
        Iterator<Binding> bar = Iter.flatMap(chain, soln->{
            Rel rel2 = Sub.substitute(soln, rel);
            Iterator<Binding> chain2 = eval(data, soln, rel2);
            //chain2 = Iter.debug(chain2);
            return chain2;
        });
        return bar;
    }

    private static void emit(RuleExecCxt rCxt, Collection<Rel> acc, Rel fact, RelStore data) {
        if ( !fact.isConcrete() ) {
            if ( rCxt.debug() )
                rCxt.out().println("Not concrete: "+fact);
        }
        if ( ! data.contains(fact) && !acc.contains(fact) ) {
            if ( rCxt.debug() )
                rCxt.out().println("Emit:   "+fact);
            acc.add(fact);
        } //else { if ( DEBUG ) rCxt.out().println("Repeat: "+fact); }
    }

    private static Iterator<Binding> eval(RelStore data, Binding input, Rel rel) {
        Iterator<Rel> iter = data.find(rel);
        Iterator<Binding> iter2 = solutions(rel, input, iter);
        return iter2;
    }

    private static Iterator<Binding> solutions(Rel rel, Binding input, Iterator<Rel> iter) {
        return Iter.iter(iter).map((r)->solutions(rel, input, r)).removeNulls();
    }

    // RulesLib.mapper except immediate.
    private static Binding solutions(Rel rel, Binding input, Rel match) {
        BindingBuilder soln = Binding.builder(input);
        int N = rel.getTuple().len();
        for(int i = 0 ; i < N ;i++ ) {
            Node x = rel.getTuple().get(i);
            Node z = match.getTuple().get(i);
            if ( isVar(x) ) {
                Var v = Var.alloc(x);
                if ( ! soln.contains(v) ) {
                    soln.add(v, z);
                    continue;
                }
                // Binding exists
                Node node = soln.get(v);
                if ( !Objects.equals(x, z) )
                    // kill
                    return null ;
                // else leave as-is
            }
        }
        return soln.build();
    }

    /** Evaluate a BGP : encapsulate for a better/different version */
    private static QueryIterator match(Graph source, BasicPattern pattern) {
        ExecutionContext execContext =  ExecutionContext.createForGraph(source);
        // Create a chain of triple iterators.
        QueryIterator chain = QueryIterSingleton.create(org.apache.jena.sparql.engine.binding.BindingFactory.root(), execContext) ;
        for (Triple triple : pattern)
            chain = QC.executeFlat(chain, triple, execContext) ;
        return chain ;
    }

    private static IndentedWriter out = IndentedWriter.clone(IndentedWriter.stdout);
    static {
        out.setFlushOnNewline(true);
    }

    public static <X> Iterator<X> print(IndentedWriter out, Iterator<X> iter) {
        return print(out, null, iter);
    }

    public static <X> Iterator<X> print(IndentedWriter out, String prefix, Iterator<X> iter) {
        String prefixStr = (prefix == null ) ? "" : prefix;

        if ( iter == null ) {
            out.printf("%s%s\n",prefixStr, "Null");
            return iter;
        }

        List<X> c = Iter.toList(iter);

        if ( c.isEmpty() )
            out.printf("%s%s\n",prefixStr, "Empty");
        else
            c.forEach(b->out.printf("%s%s\n", prefixStr, b));
        return c.iterator();
    }

    // XXX Phase out debug()

    public static Iterator<Binding> debug(String label, Iterator<Binding> iter) {
        return debug(out, -1, label, iter);
    }

    public static Iterator<Binding> debug(IndentedWriter out, int depth, String label, Iterator<Binding> iter) {
        if ( depth < 0 ) {
            out.println(label+" :: ");
            if ( iter == null) {
                out.printf("  Null\n", depth);
                return iter;
            }
            List<Binding> c = Iter.toList(iter);
            out.printf("  Length = %d\n", c.size());
            if ( c.isEmpty() )
                out.printf("  Empty\n");
            else
                c.forEach(b->out.printf("  %s\n",b));
            out.flush();
            return c.iterator();
        }

        out.println(depth+" "+label+" :: ");
        if ( iter == null) {
            out.printf("%-2d  Null\n", depth);
            return iter;
        }
        List<Binding> c = Iter.toList(iter);
        if ( c.isEmpty() )
            out.printf("%-2d  Empty\n", depth);
        else
            c.forEach(b->out.printf("%d  %s\n",depth, b));

        iter = c.iterator();
        out.flush();
        //iter = Iter.debug(iter);
        return iter;
    }

//    /**
//     * Intensional rules = occurs in the head of a rule. Returns a set of Triples with
//     * "canonical relations": ANY for variables.
//     */
//    public static Set<Rel> intensionalRelations(Collection<Rule> rules) {
//        return heads(rules);
//    }
//
//    private static Set<Rel> heads(Collection<Rule> rules) {
//        return rules.stream().map(r -> r.getHead()).map(t -> unvar(t)).collect(Collectors.toSet());
//    }
//
//    /** Extensional relationship = relation occurring only in the body of rules. */
//    public static Set<Rel> extensionalRelations(Collection<Rule> rules) {
//        Set<Rel> headRel = heads(rules);
//        // Body rules filtered by head relationships.
//        return canonicalBodyRelationships(rules).filter(t -> !headRel.contains(t)).collect(Collectors.toSet());
//    }
//
//    /** The body relationships, with variables as ANY */
//    private static Stream<Rel> canonicalBodyRelationships(Collection<Rule> rules) {
//        return rules.stream().flatMap(r -> r.getBody().stream()).map(RuleOps::unvar);
//    }
//
//    private static Function<Node, Node> fUnvar = (n) -> (n.isVariable() ? Node.ANY : n);
//
//    /** {@link Rel} with ANY, not variables. */
//    public static Rel unvar(Rel r) {
//        if ( r.getTuple().stream().allMatch(RuleOps::check) )
//            return r ;
//        // Tuple::map??
//        List<Node> x = r.getTuple().stream().map(n->fUnvar.apply(n)).collect(Collectors.toList());
//        return new Rel(r.getName(), TupleFactory.create(x));
//    }
//
//    private static boolean check(Node node) {
//        return Node.ANY.equals(node) || node.isConcrete();
//    }
//

    /** Return the rules that have a head clause that provides the given relation. */
    public static Collection<Rule> provides(Rel rel, Iterable<Rule> rules) {
        List<Rule> array = new ArrayList<>();
        // find all rules in the RuleSet that provide the rule
        // XXX EFFICENCY NEEDED
        for ( Rule r : rules ) {
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

