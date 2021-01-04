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

package org.seaborne.jena.rules.lang.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.lang.extra.LangParserBase;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.Rule;
import org.seaborne.jena.rules.RuleSet;

public class RulesParserBase extends LangParserBase {

    protected void checkTripleTerm(Node s, Node p, Node o, int line, int column) {
        // Check valid term types.
    }

    // ---- RuleSet

    private List<Rule> rules = new ArrayList<>();

    protected void startRuleSet() { }

    protected void accumulateRule(Rule rule) {
        rules.add(rule);
    }

    protected RuleSet finishRuleSet() {
        return RuleSet.newBuilder().add(rules).build();
    }

    // ---- Atom

    private String name;
    private List<Node> atomTerms = new ArrayList<>();

    protected void startAtom(String name) {
        this.name = name;
        atomTerms.clear();
    }

    protected void atomTerm(Node node) { atomTerms.add(node); }

    protected Rel finishAtom() {
        Tuple<Node> tuple = TupleFactory.create(atomTerms);
        String n = name;
        name = null;
        return new Rel(n, tuple);
    }

    // ---- Rule

    private Rel ruleHead;
    private List<Rel> ruleBody = new ArrayList<>();

    protected void startRule() {
        ruleHead = null;
        ruleBody.clear();
    }

    protected void ruleHead(Rel head) { ruleHead = head; }

    protected void ruleBodyAtom(Rel atom) { ruleBody.add(atom); }

    protected Rule finishRule() {
        if ( ruleBody.isEmpty() )
            return new Rule(ruleHead);
        return new Rule(ruleHead, ruleBody);
    }
}

