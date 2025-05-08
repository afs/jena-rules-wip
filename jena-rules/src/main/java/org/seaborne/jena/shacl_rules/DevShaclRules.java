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

package org.seaborne.jena.shacl_rules;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.query.Query;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.RowSetRewindable;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphReadOnly;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.util.IsoMatcher;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.buffering.BufferingGraph;
import org.seaborne.jena.shacl_rules.lang.ElementRule;

public class DevShaclRules {

    static { JenaSystem.init(); LogCtl.setLogging(); }

    // Execution : No rule checking.
    // Triple generation and parse triples.

    // Grammar
    // [x] TriplesTemplateBlock -- "{" TriplesTemplate(acc) "}"

    public static void main(String[] args) {

        String PREFIXES = """
                PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
                PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                PREFIX sh:   <http://www.w3.org/ns/shacl#>
                PREFIX xsd:  <http://www.w3.org/2001/XMLSchema#>
                PREFIX :     <http://example/>
                """;

        // Parser to triples.

        String dataStr1 = PREFIXES+"""
                :s1 :p 123 .
                :s2 :p 456 .
                :s3 :q 'abc'.
                """;

        String ruleSet1 = PREFIXES+"""
                RULE { ?s :xq ?o } WHERE { ?s :p ?o FILTER (?o < 400 ) }
                RULE { ?s :xxxx ?o } WHERE { ?s :xq ?o }

                ## RULE { << ?s :q ?o >> } WHERE { ?s :q ?o }
                ## IF { ?s :xq ?o } THEN { ?s :xxxx ?o }
                ## { ?s :xxxx ?o } :- { ?s :xq ?o }
                """;

        execute(ruleSet1, dataStr1);
        System.exit(0);

        String ruleSet2 = PREFIXES+"""
                RULE { ?x rdfs:subClassOf ?y } WHERE { ?x rdfs:subClassOf ?Z . ?Z rdfs:subClassOf ?y }
                RULE { ?x rdf:type ?T } WHERE { ?x rdf:type ?Z . ?Z rdfs:subClassOf ?T }
                """;

        String dataStr2 = PREFIXES+"""
                :T rdfs:subClassOf :C1 .
                :C1 rdfs:subClassOf :TOP .
                ##:C2 rdfs:subClassOf :TOP .

                ##:C8 rdfs:subClassOf :C9 .
                ##:C9 rdfs:subClassOf :C8 .

                :x rdf:type :T .
                """;

        execute(ruleSet2, dataStr2);
    }

    private static void execute(String rulesStr, String baseGraphStr) {
        Graph baseGraph = RDFParser.fromString(baseGraphStr, Lang.TURTLE).toGraph();
        baseGraph = new GraphReadOnly(baseGraph);

        RuleSet rules = ParserShaclRules.parse(rulesStr);
        rules.getRules().forEach(r->{
            System.out.println(r);
        });

        execute(rules, baseGraph);
    }


    private static void execute(RuleSet rules, Graph baseGraph) {
        // Evaluate.
        // Produce:
        //   Inferred graph
        //   Modified data graph (updated each round)

        Graph graph = GraphFactory.createGraphMem();
        GraphUtil.addInto(graph, baseGraph);

        int round = 0;
        boolean verbose = false;
        BufferingGraph graph1 = new BufferingGraph(graph);
        // Accumulator graph
        Graph accGraph = GraphFactory.createGraphMem();
        accGraph.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());

        while(true) {
            round++;

            int sizeAtRoundStart =  graph1.getAdded().size();

            System.out.println("Round: "+round);

            for (ElementRule rule : rules.getRules() ) {
                if ( verbose )
                    System.out.println("Rule: "+rule);
                // graph1 vs graph
                RowSetRewindable rowset = evalRule(graph1, rules.getPrologue(), rule).rewindable();

                if ( verbose ) {
                    RowSetOps.out(rowset);
                    rowset.reset();
                }

                BasicPattern bgp = rule.getHead();
                rowset.forEach(row->{
                    BasicPattern bgp2 = Substitute.substitute(bgp, row);
                    bgp2.forEach(t->graph1.add(t));
                });
                if ( verbose )
                    System.out.println("Accumulator: "+graph1.getAdded().size());
            }

            int sizeAtRoundEnd = graph1.getAdded().size();
            if ( sizeAtRoundStart == sizeAtRoundEnd )
                //if ( graph1.getAdded().isEmpty())
                break;

            // END of round.

            // Record inferred.
            GraphUtil.addInto(accGraph, graph1.getAdded());
            // Write to working data graph.
            graph1.flush();

            if ( verbose )
                System.out.println();
            // Whether to write base graph and clear while running.
        }
        System.out.println();

        if ( true ) {
            System.out.println("## Data graph");
            RDFWriter.source(baseGraph).format(RDFFormat.TURTLE_FLAT).output(System.out);
            System.out.println();
        }

        if ( true ) {
            System.out.println("## Inferred:");
            RDFWriter.source(accGraph).format(RDFFormat.TURTLE_FLAT).output(System.out);
            System.out.println();
        }

        if ( true ) {
            // Graph by baseGraph + accGraph
            Graph totalGraph = GraphFactory.createGraphMem();
            totalGraph.getPrefixMapping().setNsPrefixes(graph.getPrefixMapping());
            GraphUtil.addInto(totalGraph, baseGraph);
            GraphUtil.addInto(totalGraph, accGraph);
            System.out.println("## Total graph");
            RDFWriter.source(totalGraph).format(RDFFormat.TURTLE_FLAT).output(System.out);
            System.out.println();

            if ( ! IsoMatcher.isomorphic(totalGraph, graph) ) {
                System.out.println("**** Total graph not the same as the final graph");
            }
        }

        System.out.println("## Final graph [rounds="+round+"]");
        RDFWriter.source(graph).format(RDFFormat.TURTLE_FLAT).output(System.out);
        System.out.println();
        System.out.println("------------------");
    }

    private static RowSet evalRule(Graph graph, Prologue prologue, ElementRule rule) {
        ElementGroup eltGroup = rule.getBody();
        Query query = asQuery(prologue, eltGroup);
        BasicPattern bgp = rule.getHead();
        RowSet rowset = QueryExec.graph(graph).query(query).select();
        return rowset;
    }

    private static Query asQuery(Prologue prologue, ElementGroup eltGroup) {
        Query query = new Query(prologue);
        query.setQuerySelectType();
        query.setQueryResultStar(true);
        query.setQueryPattern(eltGroup);
        return query;
    }

    private static void print(RuleSet ruleSet) {

    }

}
