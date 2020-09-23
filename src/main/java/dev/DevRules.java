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

package dev;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.reasoner.rulesys.LPBackwardRuleInfGraph;
import org.apache.jena.reasoner.rulesys.impl.LPBRuleEngine;
import org.apache.jena.reasoner.rulesys.impl.LPInterpreter;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.seaborne.jena.rules.*;
import org.seaborne.jena.rules.backwards.RuleEngineBackwardsChaining;
import org.seaborne.jena.rules.naive.RuleEngineNaive;
import org.seaborne.jena.rules.parser.RuleParser;
import org.seaborne.jena.rules.seminaive.RuleEngineSemiNaive ;

public class DevRules {
    // Special case (?s ?p ?o) - single scan + (?p rdfs:domain ?T) triggered for new material.
    //   Invalidate on delete.
    //   Invalidate on schema change.

    // [ ] DOCUMENT CODE
    // [ ] TESTS
    //     RuleEngine interface.
    //  Clearup.
    // [ ] Merge Backwards
    // [ ] Recursion.
    // [ ] Shared sub-rules.
    // [ ] See jena InfEngine @ LPBackwardRuleInfGraph


    // new Bindings
    // ---
    // resolution
    // WAM
    // tabled LP backchaining interpreter
    LPBackwardRuleInfGraph x;
    LPBRuleEngine engine;
    LPInterpreter interpreter;

    // Other
    // Library: Tuple.map.
    //    Binding.forEach(BiConsumer<Var, Node> action);

    // Backward chaing.
    //
    // Crude - does not handle:
    //   Left recursion.
    //   Cache intermediate and shared rule solving (tabling)
    // Ideas:
    //   S() rule = schema - full forward.
    //     then () are backwards.


    public static void main(String... args) {
        RelStore data1 = data(
            "(:x :p1 :y)",
            "(:y :q 456)"
            );

        RuleSet ruleSet1 = rulesBuilder(
            "(?x :p0 ?z) <- (?x :p1 ?z)"
            );
        Rel query1 = RuleParser.parseRel("(?a :p0 ?b)");
        // --
        RelStore data2 = data(
            "(:x1 :p1 :c)",
            "(:x2 :p0 :d)",
            "(:c :q 456)",
            "(:d :q 789)"
            );
        RuleSet ruleSet2 = rulesBuilder(
            "(?x :r ?v) <- (?x :p0 ?y) (?y :q ?v)",
            "(?x1 :p0 ?x2) <- (?x1 :p1 ?x2)"
            );
        Rel query2 = RuleParser.parseRel("(?a :r ?b)");
        // --
        RelStore data3 = data(
            "(:b :p1 135)",
            "(:x :p0 :b)"
            );
        RuleSet ruleSet3 = rulesBuilder(
            "(?x :r ?v) <- (?x :p0 ?y) (?y :q ?v)",
            "(?x1 :p0 ?x2) <- (?x1 :p1 ?x2)"
            );
        Rel query3 = RuleParser.parseRel("(?a :r ?b)");

        //exec(data1, ruleSet1, query1);

        exec(data2, ruleSet2, query2);
        //exec(data3, ruleSet3, query3);
    }


    private static void exec(RelStore data, RuleSet ruleSet, Rel query) {
        System.out.println();
        System.out.println("-- Rules");
        System.out.println(ruleSet.toMultilineString());
        System.out.println("-- Data");
        System.out.print(data);
        System.out.println("-- Query");
        System.out.println(query);
        System.out.println("-- ------");
        Binding input = BindingFactory.root();
        Iterator<Binding> iter1 = RelSolver.solver(input, query, data, ruleSet);

        System.out.println("RESULTS");
        if ( ! iter1.hasNext() )
            System.out.println("-- empty --");
        else
            iter1.forEachRemaining(System.out::println);
    }

    private static RelStore data(String... dataStr) {
        RelStore data = RelStoreFactory.create();
        for ( String d : dataStr ) {
            //System.out.println(">> " + d);
            Rel rel = RuleParser.parseRel(d);
            //System.out.println("<< " + rel);
            //System.out.println();
            data.add(rel);
        }
        return data;
    }

    public static void main1(String... args) {

        String dataStr[] = {
            "(:x :p 123)",
            "(:p rdfs:domain :T)",
            "(:T rdfs:subClassOf :T2)",
            "(:T2 rdfs:subClassOf :T3)"
        };

        RelStore data = data(dataStr);

        System.out.println("Data:");
        System.out.print(data);
        System.out.println("Rules:");
        //RuleSet ruleSet = /*DefRules.*/rulesRDFSbasic();
        RuleSet ruleSet = devRules();
        System.out.println(ruleSet);

        RuleEngineBackwardsChaining engine = new RuleEngineBackwardsChaining(data, ruleSet);

        // To solver this (a, b. c), need that mapped as (->a, ->b, ->c)

        System.out.println("--");
        Rel rel = RuleParser.parseRel("(?x :p ?o)");
        engine.one(rel);
    }

    public static RuleSet devRules() {
        String[] xs = {
            "(?s :p ?o) <- (?s :q ?o)"
        };
        return rulesBuilder(xs);
    }

    public static RuleSet rulesRDFSbasic() {
        List<Rule> rules = new ArrayList<>();
        String[] xs = {
            ""
            // Domain and range
            ,"(?s rdf:type ?T) <- (?s ?p ?x) S(?p rdfs:domain ?T)"
            ,"(?o rdf:type ?T) <- (?s ?p ?o) S(?p rdfs:range  ?T)"
            // SubClassOf
            ,"(?s rdf:type ?T) <- (?s rdf:type ?TX )S(?TX rdfs:subClassOf ?T)"
            ,"S(?t1 rdfs:subClassOf ?t2) <- S(?t1 rdfs:subClassOf ?X) S(?X rdfs:subClassOf ?t2)"
            // SubPropertyOf
            ,"(?s ?q ?o) <- (?s ?p ?o ) S(?p rdfs:subPropertyOf ?q)"
            ,"S(?p1 rdfs:subPropertyOf ?p2) <- S(?p1 rdfs:subPropertyOf ?X) S(?X rdfs:subPropertyOf ?p2)"
        };
        return rulesBuilder(xs);
    }

    private static RuleSet rulesBuilder(String... xs) {
        List<Rule> rules = new ArrayList<>();
        for( String x : xs ) {
            if ( x == null || x.isEmpty() )
                continue;
            Rule r = RuleParser.parseRule(x);
            rules.add(r);
        }
        return new RuleSet(rules);
    }

    public static void mainFwd(String... args) {
//        String rulesStr[] = {
//            "name(:x, :r ?Z) <- name1(:x, :p ?Z)"
//            ,"name(:x, :q ?Z) <- name1(:x, :p ?Z)"
//            };
//        List<Rule> rules = new ArrayList<>();
//        for ( String d : rulesStr ) {
//            //System.out.println("> " + d);
//            Rule rule = RuleParser.parseRule(d);
//            //System.out.println("< " + rule);
//            rules.add(rule);
//        }
//        RuleSet ruleSet = new RuleSet(rules);
//
        // Tests : parsing : Rels
        RelStore data = RelStoreFactory.create();
        //String dataStr[] = {"name(:x :p 123)", "name1(:x :p 456)"};

        String dataStr[] = {
            "(:x :p 123)",
            "(:p rdfs:domain :T)",
            "(:T rdfs:subClassOf :T2)",
            "(:T2 rdfs:subClassOf :T3)"
        };

        for ( String d : dataStr ) {
            //System.out.println(">> " + d);
            Rel rel = RuleParser.parseRel(d);
            //System.out.println("<< " + rel);
            //System.out.println();
            data.add(rel);
        }

        System.out.println("Data:");
        System.out.print(data);
        System.out.println("Rules:");
        RuleSet ruleSet = DefRules.rulesRDFSbasic();
        System.out.println(ruleSet);
        System.out.println();

        System.out.println("** Naive");
        RuleEngine engine1 = new RuleEngineNaive(data, ruleSet);
        RelStore rs1 = engine1.exec();
        System.out.print(rs1);

        System.out.println("\n[] [] [] [] [] [] [] [] [] [] []\n");

        System.out.println("** SemiNaive");
        RuleEngine engine2 = new RuleEngineSemiNaive(data, ruleSet);
        RelStore rs2 = engine2.exec();
        System.out.print(rs2);
        System.out.println("\n[] [] [] [] [] [] [] [] [] [] []\n");
        if ( sameAs(rs1, rs2) )
            System.out.println("Same");
        else
            System.out.println("** Different");
    }

    private static boolean sameAs(RelStore rs1, RelStore rs2) {
        if ( rs1.size() != rs2.size() )
            return false;

        boolean diff1 = rs1.all().anyMatch(rel->rs2.contains(rel));
        boolean diff2 = rs2.all().anyMatch(rel->rs1.contains(rel));

        return diff1 && diff2 ;
    }
}
