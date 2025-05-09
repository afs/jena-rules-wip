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

import static org.junit.Assert.*;
import static org.seaborne.rules.RuleTestLib.*;

import java.util.Collection;

import org.junit.Test;

public class TestDependencyGraph {
    @Test public void dependency_1() {
        DependencyGraph dGraph = dependencyGraph("r(?x,?y) <- s(?x ?y)"
                                                ,"r(?x,?y) <- r(?x,?z) r(?x,?y)"
                                                );
        Rel rel1 = query("r(?x,?y)");
        assertNotNull(dGraph.getRuleSet().get(rel1));
        assertEquals(2, dGraph.getRuleSet().getAll(rel1).size());
    }

    @Test public void dependency_2() {
        DependencyGraph dGraph = dependencyGraph("r(?x,?y) <- s(?x ?y)");

        Rel rel1 = query("r(?x,?y)");
        Collection<Rule> c1 = dGraph.getRuleSet().provides(rel1);
        assertEquals(1, c1.size());

        Rel rel2 = query("s(?x,?y)");
        Collection<Rule> c2 = dGraph.getRuleSet().provides(rel2);
        assertEquals(0, c2.size());

        assertNotNull(dGraph.getRuleSet().get(rel1));
    }

    @Test public void dependency_recursive_1() {
        DependencyGraph dGraph = dependencyGraph("r(?x,?y) <- s(?x ?y)"
                                                ,"s(?x,?x) <- r(?x ?x)"
                                                );
        dGraph.getRuleSet().forEach(r->{
            assertTrue(dGraph.isRecursive(r));
        });
    }

    @Test public void dependency_recursive_2() {
        DependencyGraph dGraph = dependencyGraph("t(?x,?y) <- r(?x ?y)"
                                                ,"r(?x,?y) <- s(?x ?y)"
                                                ,"s(?x,?x) <- r(?x ?x)"
                                                );
        RuleSet ruleSet = dGraph.getRuleSet();

        Rel rel1 = atom("t(?x,?y)");
        Rule rule = ruleSet.getOne(rel1);
        dGraph.isRecursive(rule);
        assertFalse(dGraph.isRecursive(rule));
    }

    private static DependencyGraph dependencyGraph(String ...xs) {
        RuleSet rs = rules(xs);
        return new DependencyGraph(rs);
    }
}

