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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.seaborne.jena.rules.api.RulesGraphBuilder;
import org.seaborne.jena.rules.api.EngineType;
import org.seaborne.jena.rules.lang.RulesParser;

/** Rule engine testing - does not cover {@link RulesEngine#solve}.
 *
 * See
 *  <ul>
 *  <li> {@link TestRuleQuery} for {@link RulesEngine#solve} tests</li>
 *  <li> {@link TestRuleGraph} for tests using the engines via RDF graphs</li>
 *  </ul>
 */
@RunWith(Parameterized.class)
public class TestRuleEngine {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> tests() { return RuleTestLib.allEngines(); }

    private final EngineType underTest;
    private final RulesEngine engine;

    private static final RuleSet ruleSet;
    private static final RelStore baseData;
    private static final RelStore inferredData;
    private static final RelStore resultsData;

    static {
        String relStoreStr = "(:s :p :x) (:x :q :o)";
        baseData = RulesParser.parseData(relStoreStr);

        ruleSet = RulesParser.rules(
            "(?s :r ?o) <- (?s :q ?o)",
            "(?s :r ?o) <- (?s :p ?o)",
            "(:X :P :Z) <- (?s :r ?x) (?x :r ?o)");
        inferredData =  RulesParser.data("(:x, :r, :o)",
                                         "(:s, :r, :x)",
                                         "(:X, :P, :Z)");
        resultsData = RelStoreFactory.create().add(baseData).add(inferredData).build();
    }

    public TestRuleEngine(String testName, EngineType engineType) {
        this.underTest = engineType;
        this.engine = RulesGraphBuilder.create(underTest, ruleSet, baseData);
    }

    @Test public void stream1() {
        RulesEngine engine = RulesGraphBuilder.create(underTest, ruleSet, baseData);

        List<Rel> streamSet = engine.stream().collect(Collectors.toList());
        RelStore store = RelStoreFactory.create().add(streamSet).build();
        assertTrue(RulesLib.equals(inferredData, store));
   }

    @Test public void materialize1() {
        RulesEngine engine = RulesGraphBuilder.create(underTest, ruleSet, baseData);
        RelStore store = engine.materialize();
        assertTrue(RulesLib.equals(resultsData, store));
    }
}

