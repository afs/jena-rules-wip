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

import static org.apache.jena.sparql.core.Var.isVar;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import migrate.binding.Binding;
import migrate.binding.BindingBuilder;
import migrate.binding.BindingFactory;
import migrate.binding.Sub;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.iterator.QueryIterTriplePattern;

public class RulesLib {

    /** RelStore.equals - unordered */
    public static boolean equals(RelStore relStore1, RelStore relStore2) {
        Set<Rel> set1 = StreamOps.toSet(relStore1.all());
        Set<Rel> set2 = StreamOps.toSet(relStore2.all());
        return set1.equals(set2);
    }

//    public static boolean isRecursive(Rel head, RuleSet ruleSet) {
//        // DependencyGraph
//    }

//    public static Map<String,RelStore> slots(RuleSet rules) {
//        Map<String,RelStore> work = new HashMap<>();
//        List<String> names = rules.getHeadNames();
//        names.stream().forEach(relName->{
//            if ( ! work.containsKey(relName) )
//                work.put(relName, RelStoreFactory.createMem());
//        });
//        return work;
//    }

    /** Evaluate one rule using EDB 'data' and putting changes into 'acc' */
    public static void evalOne(RuleExecCxt rCxt, RelStore data, RelStoreAcc acc, Rule rule) {
        List<Rel> body = rule.getBody();
        if ( body.isEmpty() ) {
            // Assert head.
            emit(rCxt, acc, rule.getHead(), data);
            return;
        }
        Iterator<Binding> chain = Iter.singleton(BindingFactory.root());
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
     * The function returns null is no bindings.
     */
    public static Function<Rel, Binding> mapper(Rel pattern, Binding parent) {
        int N = pattern.len();
        // Do as much work outside the function as possible.
        Tuple<Node> patternTuple = pattern.getTuple();
        Tuple<Var> vars = map(patternTuple, n -> (Var.isVar(n))?Var.alloc(n):null);
        BindingBuilder b = BindingFactory.create(parent);

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

    private static void emit(RuleExecCxt rCxt, RelStoreAcc acc, Rel fact, RelStore data) {
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
        BindingBuilder soln = BindingFactory.create(input);
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
        ExecutionContext execContext = new ExecutionContext(ARQ.getContext(), source, null, null) ;
        // Create a chain of triple iterators.
        QueryIterator chain = QueryIterSingleton.create(org.apache.jena.sparql.engine.binding.BindingFactory.root(), execContext) ;
        for (Triple triple : pattern)
            chain = new QueryIterTriplePattern(chain, triple, execContext) ;
        return chain ;
    }

    private static IndentedWriter out = IndentedWriter.clone(IndentedWriter.stdout);
    static {
        out.setFlushOnNewline(true);
    }

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

}

