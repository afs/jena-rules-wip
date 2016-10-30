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

import java.util.List ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.iterator.QueryIterTriplePattern ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.seaborne.jena.inf.StreamTriple ;

public class Forwards {
    
    /** The Naive algorithm - loop until no change; don't let changes appear until the end of the round. */ 
    public static void evalNaive(Graph source, List<Rule> rules) {
        Graph acc = GraphFactory.createDefaultGraph() ;
        while(true) {
            acc.clear() ;
            rules.forEach(r->eval1(source, acc::add, r)) ;
            if ( acc.isEmpty() )
                return ;
            GraphUtil.addInto(source, acc);
        }
    }
    
    /** One round of rule evaluation */
    private static void eval1(Graph source, StreamTriple out, Rule rule) {
        BasicPattern pattern = BasicPattern.wrap(rule.getBody()) ;
        ExecutionContext execContext = new ExecutionContext(ARQ.getContext(), source, null, null) ; 
        // Create a chain of triple iterators.
        QueryIterator iter = match(source, pattern) ;
        iter.forEachRemaining(b->{
            Triple t = Substitute.substitute(rule.getHead(), b) ;
            if ( t.isConcrete() && ! source.contains(t) )
                out.triple(t);
        }) ;
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
