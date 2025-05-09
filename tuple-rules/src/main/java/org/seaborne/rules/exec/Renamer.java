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

package org.seaborne.rules.exec;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.seaborne.rules.Rel;
import org.seaborne.rules.Rule;
import org.seaborne.rules.RuleSet;

/**
 * Rename collections of rules so that they contain only unique names for variables
 * across the whole collection.
 */
public class Renamer {

//    private final AtomicInteger idx;
//    public Renamer() {
//        this(0);
//    }
//
//    public Renamer(int i) {
//        idx = new AtomicInteger(i);
//    }

    public static List<Rule> rename(String base, List<Rule> rules) {
        return rename(base, new HashMap<>(), 0, rules);
    }

    public static List<Rule> rename(String base, Map<Rule, Map<Var, Var>> ruleSetMap, List<Rule> rules) {
        return rename(base, ruleSetMap, 0, rules);
    }

    public static List<Rule> rename(String base, Map<Rule, Map<Var, Var>> ruleSetMap, int start, List<Rule> rules) {
        AtomicInteger idx = new AtomicInteger(start);
        int RN = rules.size();
        List<Rule> x = new ArrayList<>(RN);

        RuleSet.Builder b = RuleSet.newBuilder();
        rules.forEach(rule->{
            Rule rule1 = rename(base, ruleSetMap, idx, rule);
            x.add(rule1);
        });
        return x;
    }

    private static Rule rename(String base, Map<Rule, Map<Var, Var>> ruleSetMap, AtomicInteger idx, Rule rule) {
        Map<Var, Var> map = ruleSetMap.computeIfAbsent(rule, k->new HashMap<>());
        Rel h = rename(map, base, idx, rule.getHead());
        int N = rule.getBody().size();
        List<Rel> b = new ArrayList<>(N);
        for ( int i = 0 ; i < N ; i++ ) {
            Rel r = rename(map, base, idx, rule.getBody().get(i));
            b.add(r);
        }
        return new Rule(h,b);
    }

    private static Rel rename(Map<Var, Var> map, String base, AtomicInteger idx, Rel rel) {
        int N = rel.len();
        Node[] nodes = new Node[N];
        for(int i = 0 ; i < N ; i++ ) {
            Node n = rel.get(i);
            if ( Var.isVar(n) )
                n = map.computeIfAbsent(Var.alloc(n), k->Var.alloc(base+(idx.getAndIncrement())));
            nodes[i] = n;
        }
        return new Rel(rel.getName(), nodes);
    }

}

