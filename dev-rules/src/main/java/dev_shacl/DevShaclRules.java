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

package dev_shacl;

import static dev_shacl.DevShaclRules.Setup.dataStr1;
import static dev_shacl.DevShaclRules.Setup.ruleSet1;
import static dev_shacl.RulesDev.node;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sys.JenaSystem;
import org.seaborne.jena.shacl_rules.ParserShaclRules;
import org.seaborne.jena.shacl_rules.RuleSet;
import org.seaborne.jena.shacl_rules.RulesEngine;
import org.seaborne.jena.shacl_rules.exec.RulesEngine1;
import org.seaborne.jena.shacl_rules.writer.ShaclRulesWriter;

public class DevShaclRules {

    static { JenaSystem.init(); LogCtl.setLogging(); }

    public static void main(String[] args) {
        if ( false ) {
            String ruleStr = """
                    PREFIX :     <http://example/>
                    DATA { :x :p 1 }
                    DATA { :x :q 2 }
                    RULE { ?x :sum ?z } WHERE { ?x :p ?v1 . ?x :q ?v2 . LET ( ?z := ?v1 + ?v2 ) }
                    """;
            RuleSet rules = ParserShaclRules.parseString(ruleStr);
            RulesDev.print(rules.getData());
            System.out.println("## DONE");

            ShaclRulesWriter.print(rules);

            System.exit(0);
        }

        String rulesStr = ruleSet1;
        String dataStr = dataStr1;

        if ( false ) {
            RuleSet rules = ParserShaclRules.parseString(rulesStr);
            ShaclRulesWriter.printBasic(rules);
            System.out.println("--------");

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ShaclRulesWriter.print(bout, rules, false);
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());

            RuleSet rules2 = ParserShaclRules.parse(bin, null);
            ShaclRulesWriter.print(rules2);
            System.exit(0);
        }

        if ( true ) {
            RuleSet rules = ParserShaclRules.parseString(rulesStr);
            ShaclRulesWriter.print(rules, true);
            System.out.println();
        }

        execute(rulesStr, dataStr);
        enrich(rulesStr, dataStr, null, null, null);

//        execute(ruleSet3, dataStr3);
//        enrich(ruleSet3, dataStr3, ":x", null, null);
    }

    private static void execute(String rulesStr, String baseGraphStr) {
        Graph baseGraph = RDFParser.fromString(baseGraphStr, Lang.TURTLE).toGraph();
        baseGraph = new GraphReadOnly(baseGraph);
        RuleSet rules = ParserShaclRules.parseString(rulesStr);
        execute(rules, baseGraph);

        // OLD
        //DevVersion0.execute0(rules, baseGraph);
    }

    private static void execute(RuleSet rules, Graph baseGraph) {
        // Evaluate.
        // Produce:
        //   Inferred graph
        //   Modified data graph (updated each round)

        Graph accGraph;
        if ( false ) {
            RulesEngine rulesEngine = RulesEngine1.build(rules);
            accGraph = rulesEngine.infer(baseGraph);
        } else {
            boolean verboseExecution = false;
            RulesEngine1.Evaluation e = ((RulesEngine1)RulesEngine1.build(rules)).eval(baseGraph, verboseExecution);
            accGraph = e.inferredTriples();
            System.out.println("## Rounds: "+e.rounds());
        }

        Graph totalGraph = GraphFactory.createGraphMem();
        totalGraph.getPrefixMapping().setNsPrefixes(baseGraph.getPrefixMapping());
        GraphUtil.addInto(totalGraph, baseGraph);
        GraphUtil.addInto(totalGraph, accGraph);

        System.out.println();

        if ( true ) {
            System.out.println("## Data graph");
            RulesDev.write(baseGraph);
            System.out.println();
        }

        if ( true ) {
            System.out.println("## Inferred:");
            RulesDev.write(accGraph);
            System.out.println();
        }

        if ( true ) {
            System.out.println("## Total graph");
            RulesDev.write(totalGraph);
            System.out.println();
        }
    }

    private static void enrich(String rulesStr, String dataStr, String subject, String predicate, String object) {
        Graph baseGraph = RDFParser.fromString(dataStr, Lang.TURTLE).toGraph();
        baseGraph = new GraphReadOnly(baseGraph);
        RuleSet rules = ParserShaclRules.parseString(rulesStr);
        Node s = node(subject);
        Node p = node(predicate);
        Node o = node(object);
        enrich(rules, baseGraph, s, p, o);
    }

    private static void enrich(RuleSet ruleSet, Graph graph, Node s, Node p, Node o) {
        RulesEngine rulesEngine = RulesEngine1.build(ruleSet);

        List<Triple> triples = rulesEngine.find(graph, s, p, o).toList();

        Graph answerGraph = GraphFactory.createGraphMem();
        answerGraph.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());
        GraphUtil.add(answerGraph, triples.iterator());
        System.out.println("=-=-=-=-=-=-=-=");
        RulesDev.print(answerGraph);
    }

    public static class Setup {

        public static String PREFIXES1 = """
                PREFIX :     <http://example/>
                """;

        public static String PREFIXES = PREFIXES1+"""
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                ##PREFIX sh:   <http://www.w3.org/ns/shacl#>
                ##PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>
                """;

        // Parser to triples.

        public static String dataStr1 = PREFIXES1+"""
                :s1 :p 123 .
                :s2 :p 456 .
                :s3 :q 'abc'.
                """;

        public static String ruleSet1 = PREFIXES1+"""
                RULE { ?s :xq ?o } WHERE { ?s :p ?o FILTER (?o < 400 ) }
                RULE { ?s :xxxx ?o } WHERE { ?s :xq ?o }

                ## RULE { << ?s :q ?o >> } WHERE { ?s :q ?o }
                ## IF { ?s :xq ?o } THEN { ?s :xxxx ?o }
                ## { ?s :xxxx ?o } :- { ?s :xq ?o }
                """;

        public static String ruleSet2 = PREFIXES+"""
                RULE { ?x rdfs:subClassOf ?y } WHERE { ?x rdfs:subClassOf ?Z . ?Z rdfs:subClassOf ?y }
                RULE { ?x rdf:type ?T } WHERE { ?x rdf:type ?Z . ?Z rdfs:subClassOf ?T }
                """;

        public static String dataStr2 = PREFIXES+"""
                :T rdfs:subClassOf :C1 .
                :C1 rdfs:subClassOf :TOP .
                ##:C2 rdfs:subClassOf :TOP .

                ##:C8 rdfs:subClassOf :C9 .
                ##:C9 rdfs:subClassOf :C8 .

                :x rdf:type :T .
                """;

        public static String ruleSet3 = PREFIXES1+"""
                RULE { ?x :sum ?z } WHERE { ?x :p ?v1 . ?x :q ?v2 . LET ( ?z := ?v1 + ?v2 ) }
                """;

        public static String dataStr3 = PREFIXES1+"""
                :x :p 1 .
                :x :q 2 .
                """;
    }
}
