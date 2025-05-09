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

package org.seaborne.rules;

import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.IsoMatcher;
import org.seaborne.rules.lang.RulesParser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test suite that runs a rules engine type against the "naive" engine
 * and checks that that a query (atom with variables)
 * returns the same results from each engine.
 *
 */
@RunWith(Parameterized.class)
public class TestRuleQuery {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> tests() { return RuleTestLib.allEngines(); }

    private EngineType underTest;
    private static EngineType ref = EngineType.FWD_NAIVE;

    public TestRuleQuery(String testName, EngineType engineType) {
        underTest = engineType;
    }

    @Test
    public void query99() {
        Graph baseGraph = SSE.parseGraph("(graph (:s :p :o) )");
        RuleSet ruleSet = RulesParser.rules("(:s :q ?o) <- (:s :p ?o)");
        Rel query = RulesParser.parseAtom("(?a ?b ?c)");
        test(query, baseGraph, ruleSet);
    }

    private void test(Rel query, Graph data, RuleSet ruleSet) {
        Graph graph1 = Rules.create().baseGraph(data).rules(ruleSet).system(underTest).build();
        Graph graph2 = Rules.create().baseGraph(data).rules(ruleSet).system(ref).build();
        //boolean b = graph1.isIsomorphicWith(graph2);//IsoMatcher.isomorphic(graph1, graph2);
        boolean b = IsoMatcher.isomorphic(graph1, graph2);
        if ( ! b ) {
            System.out.println("==== Expected");
            RDFDataMgr.write(System.out, graph2, Lang.TTL);
            System.out.println("==== Actual");
            RDFDataMgr.write(System.out, graph1, Lang.TTL);
            System.out.println("====");
        }
        assertTrue("Not isomorphic", b);
    }
}

