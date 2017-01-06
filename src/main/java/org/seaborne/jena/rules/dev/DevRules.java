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

package org.seaborne.jena.rules.dev;

import org.seaborne.jena.rules.*;
import org.seaborne.jena.rules.naive.RuleEngineNaive;
import org.seaborne.jena.rules.parser.RuleParser;
import org.seaborne.jena.rules.seminaive.RuleEngineSemiNaive ;

public class DevRules {
    // https://github.com/threatgrid/naga -- forward only.
    // Paper exercise on RDFS
    
    // Special case (?s ?p ?o) - single scan + (?p rdfs:domain ?T) triggered for new material.
    //   Invalidate on delete.
    //   Invalidate on schema change.
    
    public static void main(String... args) {
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
        RuleEngine engine1 = new RuleEngineNaive(data, ruleSet);
        RelStore rs2 = engine1.exec();
        System.out.print(rs2);
        System.out.println("[] [] [] [] [] [] [] [] [] [] []");
        RuleEngine engine2 = new RuleEngineSemiNaive(data, ruleSet);
        RelStore rs3 = engine2.exec();
        System.out.print(rs2);
        
    }
}
