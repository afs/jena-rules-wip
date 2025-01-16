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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.sse.SSE;
import org.seaborne.jena.rules.lang.RulesParser;

/**
 * Rules engine backed graph, various rules engines
 */
@RunWith(Parameterized.class)
public class TestRuleGraph {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> tests() { return RuleTestLib.allEngines(); }

    private EngineType underTest;

    public TestRuleGraph(String testName, EngineType engineType) {
        underTest = engineType;
    }

    @Test public void query1() {
        Graph baseGraph = SSE.parseGraph("(graph (:s :p :o) )");
        RuleSet ruleSet = RulesParser.rules("(:s :q ?o) <- (:s :p ?o)");
        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(underTest).build();
        Triple t1 = SSE.parseTriple("(:s :p :o)");
        Triple t2 = SSE.parseTriple("(:s :q :o)");
        Triple t3 = SSE.parseTriple("(?s ?q :o)");
        assertTrue("Triple1", graph.contains(t1));
        assertTrue("Triple2", graph.contains(t2));
        assertTrue("Triple3", graph.contains(t3));
        assertEquals("Size", 2, graph.size());
    }

    @Test public void query2() {
        Graph baseGraph = SSE.parseGraph("(graph (:s :p :x) (:x :q :o))");
        RuleSet ruleSet = RulesParser.rules("(?s :p ?o) <- (?s :q ?o)",
                                            "(?s :r ?o) <- (?s :p ?o)");
        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(underTest).build();

        Triple t1 = SSE.parseTriple("(:s :r :x)");
        Triple t2 = SSE.parseTriple("(:x :r :o)");
        assertTrue("Triple1", graph.contains(t1));
        assertTrue("Triple2", graph.contains(t2));
    }

    @Test public void query3() {
        Graph baseGraph = SSE.parseGraph("(graph (:s :p :x) (:x :q :o))");
        RuleSet ruleSet = RulesParser.rules("(:x :p :o) <- (?s :q ?o)");
        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(underTest).build();
        Triple t1 = SSE.parseTriple("(:s :r :x)");
        assertFalse("Triple1", graph.contains(t1));
        Triple t2 = SSE.parseTriple("(:x :p :o)");
        assertFalse("Triple1", graph.contains(t1));
    }

    // Tests needed
    // (:X :P :Z) <- (?s :p :o)");
    // (:X :P :Z) <- (:s :p :o)");

    @Test public void query_two_rules_1() {
        testTwoRules("(?s :p ?x) (?x :q ?o)");
    }

    @Test public void query_two_rules_2() {
        testTwoRules("(?s :p ?x) (?x :r ?o)");
    }

    @Test public void query_two_rules_3() {
        testTwoRules("(?s :r ?x) (?x :q ?o)");
    }

    @Test public void query_two_rules_4() {
        testTwoRules("(?s :r ?x) (?x :r ?o)");
    }

    // Same, not graph
    @Test public void query_two_rules_1d() {
        testTwoRulesDirect("(?s :p ?x) (?x :q ?o)");
    }

    @Test public void query_two_rules_2d() {
        testTwoRulesDirect("(?s :p ?x) (?x :r ?o)");
    }

    @Test public void query_two_rules_3d() {
        testTwoRulesDirect("(?s :r ?x) (?x :q ?o)");
    }

    @Test public void query_two_rules_4d() {
        testTwoRulesDirect("(?s :r ?x) (?x :r ?o)");
    }


    // NOT graph related

    private void testTwoRules(String pathRuleBody) {
        Graph baseGraph = SSE.parseGraph("(graph (:s :p :x) (:x :q :o))");

        // SLD :p then :q 3 and 4 fail
        // SLD :q then :p 2 (:p then :r)and 4 fail
        RuleSet ruleSet = RulesParser.rules(
//                                            "(?s :r ?o) <- (?s :p ?o)",
//                                            "(?s :r ?o) <- (?s :q ?o)",
                                            "(?s :r ?o) <- (?s :q ?o)",
                                            "(?s :r ?o) <- (?s :p ?o)",
                                            "(:X :P :Z) <- "+pathRuleBody);

        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(underTest).build();
        Triple t1 = SSE.parseTriple("(:X :P :Z)");
        if ( ! graph.contains(t1) ) {
            graph.find().forEach(t->System.out.println("|"+t+"|"));
        }

        assertTrue("Triple", graph.contains(t1));
    }

    private void testTwoRulesDirect(String pathRuleBody) {
        RelStore baseData = RulesParser.parseData("(:s :p :x) (:x :q :o)");
        RuleSet ruleSet = RulesParser.rules("(?s :r ?o) <- (?s :q ?o)",
                                            "(?s :r ?o) <- (?s :p ?o)",
                                            "(:X :P :Z) <- "+pathRuleBody);
        Rel queryRel = RulesParser.parseAtom("(:X :P :Z)");
        //-- Add to Rules builder?
        RulesEngine engine = RulesGraphBuilder.create(underTest, ruleSet, baseData);
        Stream<Binding> results = engine.solve(queryRel);
        //engine.stream(queryRel);
        List<Rel> x = results.map(b->Sub.substitute(b, queryRel)).toList();
        Rel answer = RulesParser.parseRel("(:X :P :Z)");
        assertTrue("Rel", x.contains(answer));
    }

}
