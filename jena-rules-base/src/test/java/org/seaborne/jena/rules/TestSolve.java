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

import java.util.Iterator;

import migrate.binding.Binding;
import migrate.binding.Sub;
import org.apache.jena.atlas.iterator.Iter;
import org.junit.Test;
import org.seaborne.jena.rules.exec.BkdSolver;
import org.seaborne.jena.rules.lang.RulesParser;
import org.seaborne.jena.rules.store.RelStoreBuilder;
import org.seaborne.jena.rules.store.RelStoreSimple;

public class TestSolve {

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
        RuleSet ruleSet = rules(rulesStr);
        Rel query = query(queryStr);

        RelStore expected = data(result);
        RelStore actual = eval(data, ruleSet, query);
        if ( RelStore.equals(expected, actual) )
                return;

        //BkdSolver.DEBUG = true;
        Iterator<Binding> iter = BkdSolver.solver(query, ruleSet, data);
        System.out.println("Query = "+query);
//        System.out.println("== Data ==");
//        System.out.println(data.toString());
        System.out.println("== Rules ==");
        System.out.println(ruleSet.toString());
        System.out.println("== Result ==");

        if ( iter.hasNext() ) {
            //Binding b = iter.next();
            Iter.map(iter, b->Sub.substitute(b, query)).forEachRemaining(System.out::println);
        } else
            System.out.println("EMPTY");

        System.out.println();

        String message = "Expected: < "+expected+" > : Got: < "+actual+" >";
        fail(message);
    }

    static RelStore eval(RelStore data, RuleSet ruleSet, Rel query) {
        RelStoreBuilder builder = RelStoreSimple.create();
        Iterator<Binding> iter1 = BkdSolver.solver(query, ruleSet, data);
        Iter.iter(iter1).map(b->Sub.substitute(b, query)).forEach(r->builder.add(r));
        return builder.build();
    }

    private static RelStore data(String dataStr) {
        if ( dataStr == null )
            return RelStoreFactory.empty();
        return RulesParser.parseData(dataStr);
    }

    private static RuleSet rules(String ruleStr) {
        return RulesParser.parseRuleSet(ruleStr);
    }

    private Rel query(String queryStr) {
        return RulesParser.parseAtom(queryStr);
    }
}

