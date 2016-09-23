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

import java.util.ArrayList ;
import java.util.Collection ;
import java.util.List ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.GraphUtil ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.compose.Union ;
import org.apache.jena.query.ARQ ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.sparql.core.BasicPattern ;
import org.apache.jena.sparql.core.Substitute ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton ;
import org.apache.jena.sparql.engine.iterator.QueryIterTriplePattern ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.jena.inf.StreamTriple ;

public class Backwards {
    public static void main(String... argv) {
        Graph g = RDFDataMgr.loadGraph("D.ttl") ;
        Graph g1 = GraphFactory.createDefaultGraph() ;
        Graph g2 = new Union(g, g1) ;
            
//        Rule rule1 = rule("(?s :pq ?o)", "(?s :p ?x)", "(?x :q ?o)") ;
//        Rule rule2 = rule("(?s :PQ ?o)", "(?s :pq ?o)") ;
//        List<Rule> rules = new ArrayList<>() ;
//        rules.add(rule2) ;
//        rules.add(rule1) ;
        
        List<Rule> rules = rulesRDFS() ;
        List<Triple> acc = new ArrayList<>() ;
        eval(g, rules);
        
        RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS) ;
    }
    
    /*package*/ static List<Rule> rulesRDFS() {
        List<Rule> rules = new ArrayList<>() ;
        rulesRDFS1(rules) ;
        rulesRDFS2(rules) ;
        rulesRDFS3(rules) ;
        return rules ;
    }
    
    // rdfs-min.rules : RDFS with no axioms, no rdf:Property.
    
    
    // Range and Domain
    /*package*/ static void rulesRDFS1(List<Rule> rules) {
        // Gulp. This is horrendous. Need to determine that B1 needs eval on diffs, B2 is unchanging.
        // I.e. table clauses. Or maybe just a cache/trigger Or rule ordering.
        Rule rule1 = rule("(?s rdf:type ?T)", "(?s ?p ?x)", "(?p rdfs:domain ?T)") ;
        Rule rule2 = rule("(?o rdf:type ?T)", "(?s ?p ?o)", "(?p rdfs:range  ?T)") ;
        rules.add(rule2) ;
        rules.add(rule1) ;
    }
    
    // SubClass and SubProperty
    /*package*/ static void rulesRDFS2(List<Rule> rules) {
        Rule rule1 = rule("(?s rdf:type ?T)", "(?s rdf:type ?TX )", "(?TX rdfs:subClassOf ?T)") ;
        Rule rule2 = rule("(?t1 rdfs:subClassOf ?t2)", "(?t1 rdfs:subClassOf ?X)", "(?X rdfs:subClassOf ?t2)") ;

        Rule rule3 = rule("(?s ?q ?o)", "(?s ?p ?o )", "(?p rdfs:subPropertyOf ?q)") ; 
        Rule rule4 = rule("(?p1 rdfs:subPropertyOf ?p2)", "(?p1 rdfs:subPropertyOf ?X)", "(?X rdfs:subPropertyOf ?p2)") ;

        rules.add(rule1) ;
        rules.add(rule2) ;
        rules.add(rule3) ;
        rules.add(rule4) ;
    }
    
    // Other
    /*package*/ static void rulesRDFS3(List<Rule> rules) {
        Rule rule1 = rule("(?X rdfs:subClassOf ?X)", "(?Y rdfs:subClassOf ?X )") ;
        Rule rule2 = rule("(?X rdfs:subClassOf ?X)", "(?X rdfs:subClassOf ?Y )") ;
        Rule rule3 = rule("(?X rdfs:subClassOf ?X)", "(?Y rdf:type ?X)") ;
        rules.add(rule1) ;
        rules.add(rule2) ;
        rules.add(rule3) ;
        Rule rule4 = rule("(?X rdfs:subPropertyOf ?X)", "(?Y rdfs:subPropertyOf ?X )") ;
        Rule rule5 = rule("(?X rdfs:subPropertyOf ?X)", "(?X rdfs:subPropertyOf ?Y )") ;
        rules.add(rule4) ;
        rules.add(rule5) ;
    }

        private static void print(Collection<Triple> acc) {
        acc.stream().map(SSE::str).forEach(System.out::println);
    }

    private static Rule rule(String _head, String..._body) {
        Triple head = SSE.parseTriple(_head) ;
        List<Triple> body = new ArrayList<>(_body.length) ;
        for ( String x : _body ) {
            body.add(SSE.parseTriple(x)) ;
        }
        return new Rule(head, body) ;
    }

    public static void eval(Graph source, List<Rule> rules) {
        Graph acc = GraphFactory.createDefaultGraph() ;
        while(true) {
            acc.clear() ;
            rules.forEach(r->eval1(source, acc::add, r)) ;
            if ( acc.isEmpty() )
                return ;
            GraphUtil.addInto(source, acc);
        }
    }
    
    public static void eval1(Graph source, StreamTriple out, Rule rule) {
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
    
    /** Evaluate a BGP : encpsulate for a better/different version */  
    private static QueryIterator match(Graph source, BasicPattern pattern) {
        ExecutionContext execContext = new ExecutionContext(ARQ.getContext(), source, null, null) ; 
        // Create a chain of triple iterators.
        QueryIterator chain = QueryIterSingleton.create(BindingFactory.root(), execContext) ; 
        for (Triple triple : pattern)
            chain = new QueryIterTriplePattern(chain, triple, execContext) ;
        return chain ;
    }
}
