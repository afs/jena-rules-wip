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

package org.seaborne.jena.rules.naive;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.engine.iterator.QueryIterTriplePattern;
import org.seaborne.jena.rules.*;

/**
 * The <a href="">naïve</a> algorithm, done very naively. Hopefully, so simple it is easy
 * to verify as correct and then use in tests to compare results with other engines.
 */
public class RuleEngineNaive {
    
    private final RelStore data; // RelStore.setReadOnly.
    private final RuleSet rules;

    public RuleEngineNaive(RelStore data, RuleSet rules) {
        this.data = data;
        this.rules = rules;
    }

    // Temp name
    public RelStore exec() {
        RelStore  generation = RelStoreFactory.createMem();
        generation.add(data);
        // What about variable arity?
        //--
        // Algorithm
        
        while(true) {
            RelStore rs = RelStoreFactory.createMem();
            rules.asList().forEach(rule->{
                //System.out.println("==== "+rule);
                evalOne(generation, rs, rule);
            });
            // Changes?
            if ( rs.isEmpty() ) {
                // Diffs
                return generation;
            }
            generation.add(rs);
        }
    }
    
    private Map<String,RelStore> slots() {
        Map<String,RelStore> work = new HashMap<>(); 
        List<String> names = rules.getHeadNames();
        names.stream().forEach(relName->{
            if ( ! work.containsKey(relName) )
                work.put(relName, RelStoreFactory.createMem());
        });
        return work;
    }
    
    private static void evalOne(RelStore data, RelStore acc, Rule rule) {
        RelStore data2 = RelStoreFactory.combine(data, acc);
        List<Rel> body = rule.getBody();
        if ( body.isEmpty() ) {
            // Assert head.
            emit(acc, rule.getHead(), data);
            return;
        }
        Iterator<Solution> chain = Iter.singleton(new Solution());
        for(Rel rel: body) {
            chain = step(data2, rel, chain);
        }
        
        chain.forEachRemaining(soln->{
            Rel x = substitute(soln, rule.getHead());
            emit(acc, x, data);
        });
    }

    private static Iterator<Solution> step(RelStore data, Rel rel, Iterator<Solution> chain) {
        // XXX
        Stream<Solution> foo = Iter.asStream(chain);
        Stream<Solution> bar = foo.flatMap(soln->{
            Rel rel2 = substitute(soln, rel);
            Iterator<Solution> chain2 = eval(data, soln, rel2);
            //chain2 = Iter.debug(chain2);
            return Iter.asStream(chain2);
        });
        return bar.iterator();
    }

    private static void emit(RelStore acc, Rel fact, RelStore data) {
        if ( !fact.isConcrete() )
            System.out.println("Not concrete: "+fact);
        
        if ( ! data.contains(fact) && !acc.contains(fact) ) {
            System.out.println("Emit:   "+fact);
            acc.add(fact);
        } //else System.out.println("Repeat: "+fact);
    }

    private static Iterator<Solution> eval(RelStore data, Solution input, Rel rel) {
        Iterator<Rel> iter = data.find(rel);
        Iterator<Solution> iter2 = solutions(rel, input, iter);
        return iter2;
    }

    private static Iterator<Solution> solutions(Rel rel, Solution input, Iterator<Rel> iter) {
        return Iter.iter(iter).map((r)->solutions(rel, input, r)).removeNulls();
    }

    private static Solution solutions(Rel rel, Solution input, Rel match) {
        Solution soln = new Solution(input);
        int N = rel.getTuple().len();
        for(int i = 0 ; i < N ;i++ ) {
            Node x = rel.getTuple().get(i);
            Node z = match.getTuple().get(i);
            if ( Var.isVar(x) ) {
                Var v = Var.alloc(x);
                if ( soln.containsKey(v) ) {
                    // Binding exists
                    Node node = soln.get(v);
                    if ( !Objects.equals(x, z) )
                        // kill
                        return null ;
                    // else leave as-is
                } else
                    soln.put(v, match.getTuple().get(i));
            }
        }
        return soln;
    }

    private static Rule substitute(Map<Var, Node> values, Rule rule) {
        Rel h = substitute(values, rule.getHead());
        List<Rel> b = rule.getBody().stream().map(rel->substitute(values,rel)).collect(toList());
        return new Rule(h, b);
    }

    private static Rel substitute(Map<Var, Node> values, Rel rel) {
        int N = rel.getTuple().len();
        Node v[] = new Node[rel.getTuple().len()];
        for(int i = 0 ; i < N ; i++) {
            v[i] = substitute(values, rel.getTuple().get(i));
        }
        return new Rel(rel.getName(), TupleFactory.asTuple(v));
    }

    private static Node substitute(Map<Var, Node> values, Node node) {
        if ( !Var.isVar(node) )
            return node;
        return values.getOrDefault(Var.alloc(node), node);
    }

    /** Evaluate a BGP : encapsulate for a better/different version */  
    private static QueryIterator match(Graph source, BasicPattern pattern) {
        ExecutionContext execContext = new ExecutionContext(ARQ.getContext(), source, null, null) ; 
        // Create a chain of triple iterators.
        QueryIterator chain = QueryIterSingleton.create(BindingFactory.root(), execContext) ; 
        for (Triple triple : pattern)
            chain = new QueryIterTriplePattern(chain, triple, execContext) ;
        return chain ;
    }
}

