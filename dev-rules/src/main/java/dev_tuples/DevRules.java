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

package dev_tuples;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.sse.SSE;
import org.seaborne.rules.*;
import org.seaborne.rules.exec.MGU;
import org.seaborne.rules.exec.Renamer;
import org.seaborne.rules.lang.RulesParser;
import org.seaborne.rules.store.RelStoreGraph;
import org.seaborne.rules.store.RelStoreSimple;

public class DevRules {
    // Grammar update/rewrite

    // [ ] DOCUMENT CODE
    // [ ] TESTS
    //     RuleEngine interface.
    //        stream()
    //        materialize()
    // [ ]  Parser - test for PREFIX, BASE.
    // [ ] RuleOps -- internal-> engine
    // [ ] Naive -> exec.naive

    // [ ] Clearup.
    // [ ] Recursion.
    // [ ] Shared sub-rules. - later
    // [ ] See jena InfEngine @ LPBackwardRuleInfGraph

    // [] Add incIndent/decIndent try-finally function to IndentedWriter

    // [ ] RuleSet - rename apart early when RuleSte built. RuleSet -> getExecRuleSet
    // [ ] Clean up back to rule to query results.
    // [ ] Prefix Map for ruleset

    // -----
    // MGU: Check variants; run again "pure".

    /*
     * subst query with input (keep original query)
     * match query/gnd to head => mgu
     * apply MGU to each rule body - chain - start with a root, not input.
     * solve rule
     * map back to query vars, input as parent.
     */

    /*
     * SPARQL/Rules
     *
     * { ?s ?p ?o } <= { ?s ?p ?o . FILTER ( ?o = 5 ) }
     *
     * { :s :p1 ?o1 . :s :p2 ?o2 .} <- [ ?o1 ?o2 ] <- { :s ?p ?o . FILTER ( ?o = 5 ) . BIND(1 AS ?o1) . BIND(4 AS ?o2) }
     *
     * BIND in result?
     * Rule -> result -> bind -> triples.
     * Rule -> result :: datalog
     * Output rules: not part of solving.
     * (?s ?p ?o) -> { BIND ... ?s ?p ?o }
     */

    public static void main(String...a) {
        //rules.main("rules.txt");
        //main0();
        mainGraphAndRules();
    }

    public static void mainGraphAndRules(String...a) {
        Graph graph = RDFParser.source("data.ttl").toGraph();
        RelStore baseData = RelStoreFactory.create(graph);

//        -> table(rdfs:subClassOf).
//        [ (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b), (?b rdfs:subClassOf ?c)]
//        ##[ (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y), (?a rdf:type ?x) ]
//        #<http://example.com/condition0>
//        [ (<http://example.com/condition0> rdf:type ?y) <-  (<http://example.com/condition0> rdf:type ?x) , (?x rdfs:subClassOf ?y) ]

        // Recursion.
        String rulesString = """
                PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#>
                (?a rdfs:subClassOf ?c) <- (?a rdfs:subClassOf ?b) (?b rdfs:subClassOf ?c) .
               (?a rdf:type ?y) <- (?x rdfs:subClassOf ?y) (?a rdf:type ?x) .
              """;


        RuleSet ruleSet = RulesParser.parseRuleSet(rulesString);


        String queryStr = "(<http://example.com/s> rdf:type ?y)";
        Rel queryRel = RulesParser.parseAtom(queryStr);
        RulesEngine engine = RulesGraphBuilder.create(EngineType.BKD_NON_RECURSIVE_SLD, ruleSet, baseData);
        Stream<Binding> x = engine.solve(queryRel);
        x.forEach(System.out::println);
    }

    public static void main0(String...a) {

        if ( true )
        {
            // Materialize/stream
            String relStoreStr = "(:s :p :x) (:x :q :o)";
            RelStore baseData = RulesParser.parseData(relStoreStr);
            System.out.println("DATA");
            baseData.stream().forEach(System.out::println);

            RuleSet ruleSet = RulesParser.rules(
                "(?s1 :r ?o1) <- (?s1 :q ?o1)",
                "(?s2 :r ?o2) <- (?s2 :p ?o2)",
                "(:X :P :Z)   <- (?s3 :r ?x) (?x :r ?o3)");
            String queryStr = "(:X :P :Z)";
            //String queryStr = "(?a ?b ?c)";

            Rel queryRel = RulesParser.parseAtom(queryStr);

//            //RuleExecCxt.global.TRACE = true ;
//            execAsQuery(relStoreStr, ruleSetStr, EngineType.FWD_NAIVE, queryStr);
            //RuleExecCxt.global.TRACE = true ;
            RulesEngine engine = RulesGraphBuilder.create(EngineType.BKD_NON_RECURSIVE_SLD, ruleSet, baseData);
            System.out.println("STREAM");
            engine.stream().forEach(System.out::println);
            System.out.println("MATERIALIZE");
            RelStore m = engine.materialize();
            m.stream().forEach(System.out::println);
            System.out.println("DONE");
            //System.exit(0);
            System.out.println();
        }

        // execAsGraph
        if ( true )
        {
            Graph baseGraph = SSE.parseGraph("(graph (:s :p :x) (:x :q :o))");
            //Graph baseGraph = SSE.parseGraph("(graph)");
            RuleSet ruleSet = RulesParser.rules("(:X :P :Z) <-");

            String queryTripleStr = "(:X :P ?A)";
            Triple queryTriple = SSE.parseTriple(queryTripleStr);
            //RuleExecCxt.global.DEBUG = true ;
            execAsGraph(baseGraph, ruleSet, EngineType.BKD_NON_RECURSIVE_SLD, queryTriple);

            System.out.println("DONE");
            System.exit(0);
        }

        System.out.println("** NOTHING **");
        System.exit(0);
    }

    // ----
    // TestRuleGraph.testTwoRulesDirect


    private static void testTwoRulesDirect(String pathRuleBody) {
        RelStore baseData = RulesParser.parseData("(:s :p :x) (:x :q :o)");
        RuleSet ruleSet = RulesParser.rules("(?s :r ?o) <- (?s :q ?o)",
                                            "(?s :r ?o) <- (?s :p ?o)",
                                            "(:X :P :Z) <- "+pathRuleBody);
        Rel queryRel = RulesParser.parseAtom("(:X :P :Z)");
        //-- Add to Rules builder?
        RuleExecCxt.global.TRACE = true ;
        RulesEngine engine = RulesGraphBuilder.create(EngineType.BKD_NON_RECURSIVE_SLD, ruleSet, baseData);
        Stream<Binding> results = engine.solve(queryRel);

        List<Binding> resultsList = results.toList();
        System.out.println(resultsList);
        results = resultsList.stream();

        //engine.stream(queryRel);
        List<Rel> x = results.map(b->Sub.substitute(b, queryRel)).toList();
        //-
        Rel answer = RulesParser.parseRel("(:X :P :Z)");
        assertTrue("Rel", x.contains(answer));
    }


    // ----
    // This is TestRuleSolve
    private static void testRuleSolve(String dataStr, String rulesStr, String queryStr, String result) {
        RelStore data = RulesParser.parseData(dataStr);
        RuleSet ruleSet = RulesParser.parseRuleSet(rulesStr);

        Rel query = RulesParser.parseAtom(queryStr);

        RelStore expected = RulesParser.data(result);
        RuleExecCxt.global.TRACE = true ;
        RulesEngine engine = RulesGraphBuilder.create(EngineType.BKD_NON_RECURSIVE_SLD, ruleSet, data);
        List<Binding> x = engine.solve(query).toList();
        System.out.println("--");
        System.out.println(x); // ?x = ?v, not ?v = 1

        RelStoreBuilder storeBuilder = RelStoreSimple.create();
        x.stream().map(b->Sub.substitute(b, query)).forEach(r->storeBuilder.add(r));
        RelStore results = storeBuilder.build();
        System.out.println("--");
        System.out.println(results); // ?x = ?v, not ?v = 1
        System.out.println("--");

//        RelStore actual = Rules.eval(data, ruleSet, , query);
//        System.out.println(actual);
    }

    // ---- Exec functions

    public static void execAsGraph(Graph baseGraph, RuleSet ruleSet, EngineType type, Triple query) {
        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(type).build();
        // Try it out.
        System.out.println("---");
        RDFDataMgr.write(System.out, graph, Lang.NT);
        System.out.println("---");
        assertTrue("Triple1", graph.contains(query));
    }

    public static void execAsQuery(String relStoreStr, String ruleSetStr, EngineType type, String queryStr) {
        RelStore baseData = RulesParser.parseData(relStoreStr);
        RuleSet ruleSet = RulesParser.parseRuleSet(ruleSetStr);
        Rel queryRel = RulesParser.parseAtom(queryStr);
        //RuleExecCxt.global.DEBUG = true ;
        execAsQuery(baseData, ruleSet, type, queryRel);
    }

    public static void execAsQuery(Graph baseGraph, RuleSet ruleSet, EngineType type, Rel query) {
        RelStore baseData = new RelStoreGraph(baseGraph);
        execAsQuery(baseData, ruleSet, type, query);
    }

    public static void execAsQuery(RelStore baseData, RuleSet ruleSet, EngineType type, Rel query) {
        boolean verbose = true;
        if ( verbose ) {
            System.out.println("Query: "+query);
            System.out.println("Data:");
            System.out.println(RelStore.toMultiLineString(baseData));
            System.out.println("Rules:");
            System.out.println(ruleSet.toMultilineString());
            System.out.println();
        }
        RulesEngine engine = RulesGraphBuilder.create(type, ruleSet, baseData);

        Stream<Binding> results = engine.solve(query);

        // Map query to result rels.
        List<Rel> x = results.map(b->Sub.substitute(b, query)).toList();
        if ( verbose ) {
            System.out.println();
            System.out.println("Result: "+x);
            System.out.println();
        }
    }

    public static void mainRename() {
        Rule rule1 = RulesParser.parseRule("(?x :a ?z) :- (?x :b ?Y) (?Y :b ?z)");
        Rule rule2 = RulesParser.parseRule("(?x :b ?z) :- c(?x ?z)");
        RuleSet ruleSet = RuleSet.create(rule1, rule2);

        Map<Rule, Map<Var, Var>> ruleSetMap = new HashMap<>();
        List<Rule> ruleSet2 = Renamer.rename("v", ruleSetMap, ruleSet.rules());
        ruleSetMap.forEach((r,m)->System.out.printf("%% %-15s  %s\n", r.getHead(), m));
        System.out.println(ruleSet2);
    }

    public static void mainMGU(String...a) {
        dwimMGU("(?x, 'a', ?z)", "(:x, ?a ?b)");
        dwimMGU("no(?x, 'x', ?x, 'b')", "no(?y, 'x', 'a', ?y)");
        dwimMGU("yes(?x, 'x', ?x, 'a')", "yes(?y, 'x', 'a', ?y)");
        dwimMGU("yes(?x, 'x', ?x, 'a')", "yes('a', 'x', ?y, ?y)");
    }

    static void dwimMGU(String xRel, String yRel ) {
        Rel rel1 = RulesParser.parseAtom(xRel);
        Rel rel2 = RulesParser.parseAtom(yRel);
//        Unifier unifier = mgUnifier(rel1, rel2);
//        System.out.printf("%s %s ==> %s\n", rel1, rel2, unifier);
        Binding b = MGU.mgu(rel1, rel2);
        b.toString();
        System.out.printf("%s %s --> %s\n", rel1, rel2, b);
        System.out.println();
    }

    // Returns the left-to-right mapping biased MGU.
    // i.e. use on a head or body atom to get it grounded and expressed in vars of the query.
    // (Assumes no body atom var name clash).
    // i.e. var-var mappings are from right var to left var
    // i.e. output is in LHS namespace.
    // Whhat about ?a = ?a on RHS?

    public static void pureMGU(String relStr1, String relStr2) {
        Rel rel1 = RulesParser.parseAtom(relStr1);
        Rel rel2 = RulesParser.parseAtom(relStr2);
        Binding mgu = MGU.mguAlg2(rel1, rel2);
        Rel rel1a = MGU.applyMGU(mgu, rel1);
        Rel rel2a = MGU.applyMGU(mgu, rel2);

        System.out.printf("%s  %s\n", rel1, rel2);
        System.out.printf("mgu %s\n",mgu);
        System.out.printf("%s  %s\n", rel1a, rel2a );
        System.out.println();
    }

    public static void applyMGU(String relStr1, String relStr2) {
        Rel rel1 = RulesParser.parseAtom(relStr1);
        Rel rel2 = RulesParser.parseAtom(relStr2);
        Binding mgu = MGU.mgu(rel1, rel2);
        Rel rel1a = MGU.applyMGU(mgu, rel1);
        Rel rel2a = MGU.applyMGU(mgu, rel2);

        System.out.printf("%s  %s\n", rel1, rel2);
        System.out.printf("mgu %s\n",mgu);
        System.out.printf("%s  %s\n", rel1a, rel2a );

        System.out.println("DONE");

    }

//    pureMGU("yes(?x, ?x)", "yes(?a, ?b)");
//    System.exit(0);
//    pureMGU("(?x ?y)", "(:x ?B)");
//    pureMGU("(:x ?B)", "(?x ?y)");
//
//    pureMGU("(?x ?x ?y)", "(:x ?A ?A)");
//    pureMGU("(:x ?A ?A)", "(?x ?x ?y)");
//
//    pureMGU("(?x ?x ?y)", "(:x ?A ?A)");
//
//    pureMGU("(?x '1' '2')", "(:x ?A ?B)");


    // ---- DependencyGraph
    public static void mainDependencyGraph() {
        RuleSet ruleSet = RulesParser.rules
                ("r(?x) <- s(?x,?y) t(?y, ?X)"
                ,"r(?x) <- q(?x)"
                ,"q(?z) <- t1(?z) t2(?z)"
                ,"t1(?z) <- t(?z)"
                ,"t2(?z) <- t3(?z)"
                );
        ruleSet.print();
        System.out.println();

        DependencyGraph dGraph = new DependencyGraph(ruleSet);
        System.out.println();

        dGraph.print(System.out);
        System.out.println();
        System.out.println("DONE");
        System.exit(0);

        ruleSet.forEach(r->{
            System.out.println("Walk:"+r);
            dGraph.walk(r, r2->System.out.println("X:"+r2));
        });
        System.exit(0);
    }

    static void calcMGU(String s1, String s2) {
        Rel rel1 = RulesParser.parseAtom(s1);
        Rel rel2 = RulesParser.parseAtom(s2);
        Binding mgu1 = MGU.mgu(rel1, rel2);
        System.out.println(rel1+" "+rel2+" ==> "+mgu1);
        Binding mgu2 = MGU.mgu(rel2, rel1);
        System.out.println(rel2+" "+rel1+" ==> "+mgu2);
        System.out.println();
    }
}
