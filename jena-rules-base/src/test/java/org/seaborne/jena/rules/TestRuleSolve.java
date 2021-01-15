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

import static org.junit.Assert.fail;
import static org.seaborne.jena.rules.RuleTestLib.data;
import static org.seaborne.jena.rules.RuleTestLib.query;
import static org.seaborne.jena.rules.RuleTestLib.ruleSet;

import java.util.Iterator;

import migrate.binding.Binding;
import migrate.binding.Sub;
import org.apache.jena.atlas.iterator.Iter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.seaborne.jena.rules.api.EngineType;
import org.seaborne.jena.rules.api.Rules;
import org.seaborne.jena.rules.exec.BkdSolver;

/**
 * Tests of rule matching features, comparing to expected answers.
 */
@RunWith(Parameterized.class)
public class TestRuleSolve {

    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> tests() { return RuleTestLib.allEngines(); }

    final private EngineType underTest;

    public TestRuleSolve(String testName, EngineType engineType) {
        underTest = engineType;
    }

    // Basics
    @Test public void solve_01() { test("r(1)", "", "r(1)", "r(1)"); }
    @Test public void solve_02() { test("r(1)", "", "r(2)", null); }
    @Test public void solve_03() { test("r(1)", "", "r(?x)", "r(1)"); }

    @Test public void solve_10() { test("r(1)", "s(?x) <- r(?x) .", "s(?x)", "s(1)"); }
    @Test public void solve_11() { test("r(1)", "s(?x) <- r(?x) .", "s(?v)",  "s(1)"); }
    @Test public void solve_12() { test("r(1)", "s(?x) <- r(?x) .", "s(1)", "s(1)"); }
    @Test public void solve_13() { test("r(1)", "s(?x) <- r(?x) .", "s(2)", null); }

    @Test public void solve_20() { test("r(1,1).r(1,2).", "s(?x) <- r(1,?x) .", "s(?x)", "s(1) . s(2)"); }

    @Test public void solve_50() { test("r(1)", "s(1) <- r(1). s(2) <- r(2). ", "s(2)", null); }
    @Test public void solve_51() { test("r(1)", "s(1) <- r(?x). s(2) <- r(?x). ", "s(1)", "s(1)"); }
    @Test public void solve_52() { test("r(1)", "s(?x) <- r(?x). t(?z) <- s(?z).", "t(?a)", "t(1)"); }

    private void test(String dataStr, String rulesStr, String queryStr, String result) {
        RelStore data = data(dataStr);
        RuleSet ruleSet = ruleSet(rulesStr);
//        RuleSet ruleSet0 = ruleSet(rulesStr);
//        RuleSet ruleSet = Renamer.rename("vvv", ruleSet0);

        Rel query = query(queryStr);

        RelStore expected = data(result);
        RelStore actual = Rules.eval(data, ruleSet, underTest, query);
        boolean testResult = RelStore.equals(expected, actual);
        if ( testResult )
            return;

        // ---- Test fail.
        // -- Debug
        System.out.printf("D:%s R:%s Q:%s -> %s\n",dataStr, rulesStr, queryStr, result);
        try {
            Iterator<Binding> iter = BkdSolver.solver(query, ruleSet, data);
            System.out.println("Query = "+query);
    //        System.out.println("== Data ==");
    //        System.out.println(data.toString());
            System.out.println("== Rules ==");
            System.out.println(ruleSet.toString());
            System.out.println("== Result ==");

            if ( iter.hasNext() ) {
                Iter.map(iter, b->Sub.substitute(b, query)).forEachRemaining(System.out::println);
            } else
                System.out.println("EMPTY");

            System.out.println();

            String message = "Expected: < "+expected+" > : Got: < "+actual+" >";
            fail(message);
        } finally {
            RuleExecCxt.global.DEBUG = false;
        }
    }
}

