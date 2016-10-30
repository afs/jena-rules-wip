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

import java.util.*;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.graph.compose.Union ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.sparql.graph.GraphFactory ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.vocabulary.RDF;

public class RuleMain {
    
    public static void main(String... argv) {
        devFwd();
        //dev();
    }
    
    public static void devFwd() {
        Graph g = RDFDataMgr.loadGraph("D.ttl") ;
//        Graph g1 = GraphFactory.createDefaultGraph() ;
//        Graph g2 = new Union(g, g1) ;
        RuleSet ruleSet = Rules.rulesRDFS() ;
        RuleEngine e = new RuleEngineNaive(g, ruleSet);
        Triple query = Triple.create(Node.ANY, RDF.Nodes.type, Node.ANY);
        e.matchStream(query).forEach((t)->{
            System.out.println(SSE.str(t));
        });
        System.out.println("DONE") ;
    }
        
    public static void dev() {
        Graph g = RDFDataMgr.loadGraph("D.ttl") ;
        Graph g1 = GraphFactory.createDefaultGraph() ;
        Graph g2 = new Union(g, g1) ;

//        Rule rule1 = rule("(?s :pq ?o)", "(?s :p ?x)", "(?x :q ?o)") ;
//        Rule rule2 = rule("(?s :PQ ?o)", "(?s :pq ?o)") ;
//        List<Rule> rules = new ArrayList<>() ;
//        rules.add(rule2) ;
//        rules.add(rule1) ;

        
        RuleSet ruleSet_ = Rules.rulesRDFS() ;
        List<Rule> x = new ArrayList<>(ruleSet_.asList()) ;
        rulesOther(x);
        RuleSet ruleSet = new RuleSet(x);
        
        System.out.println("Rules:");
        System.out.println(ruleSet.toMultilineString());
        System.out.println("----");
        // idb relation = occurs in the head of a rule
        // edb relation = relation occuring only in the body of rules
        // ??? Wrong.
        Set<Triple> intensional = ruleSet.getIntensional();
        System.out.println("Intensional (occurs in the head):");
        print(intensional); 
        System.out.println("----");
        
        Collection<Triple> extensional = ruleSet.getExtensional();
        System.out.println("Extensional (only in the body):");
        print(extensional); 
        System.out.println("----");
        
        Collection<Node> loops = ruleSet.loops();
        System.out.println("Loops:");
        loops.forEach(n->System.out.println("  "+SSE.str(n)));
        System.out.println("----");
        // NEEDS REWRITE/RETHINK
//        System.out.println("Chains:");
//        Map<Node, List<List<Rule>>> chains = ruleSet.loopsChains();
//        chains.forEach((p,x)->{
//            System.out.println("  "+SSE.str(p));
//            x.forEach(rl->System.out.println("    "+rl)) ;
//        });
//        System.out.println("----");
        //Forwards.evalNaive(g, rules);
        Triple t = SSE.parseTriple("(?s rdf:type ?o)") ;
        Backwards.eval(t, g2, ruleSet);
        
        //RDFDataMgr.write(System.out, g2, RDFFormat.TURTLE_BLOCKS) ;
    }
    
    // rdfs-min.rules : RDFS with no axioms, no rdf:Property.
    
    /*package*/ static void rulesOther(List<Rule> rules) {
        // Some extra test rules.
        if ( false ) {
            // Mutual recursion
            Rule rule1 = rule("(?s :foo ?T)", "(?s ?p ?x)", "(?p :bar ?T)") ;
            Rule rule2 = rule("(?s :bar ?T)", "(?s ?p ?x)", "(?p :foo ?T)") ;
            rules.add(rule1);
            rules.add(rule2);
        }
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
}
