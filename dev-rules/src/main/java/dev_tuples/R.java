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

package dev_tuples;

import static org.seaborne.rules.lang.RulesParser.data;

import org.seaborne.rules.RelStore;
import org.seaborne.rules.RuleSet;
import org.seaborne.rules.RuleSets;
import org.seaborne.rules.lang.RulesParser;

public class R {

    // ---- Empty
    static RelStore dataEmpty = data();
    static RuleSet rulesEmpty = RulesParser.rules();
    static RuleSet ruleSetRSG = RulesParser.rules
    ("rsg(?x, ?y) <- flat(?x, ?y)"
    ,"rsg(?x, ?y) <- up(?x ?x1) flat(?y1, ?x1) down(?y1, ?y)"
    );
    static RelStore dataRSG = data
    ("up('a','e')", "up('a', 'f')", "up('f','m')", "up('g','n')", "up('h', 'n')", "up('i', 'o')" , "up('j','o')"
    ,"flat('g', 'f')", "flat('m','n')", "flat('m', 'o')", "flat('p', 'm')"
    ,"down('l', 'f')", "down('m','f')", " down('g','b')", " down('h', 'c')", " down('i', 'd')", " down('p', 'k')"
    );
    // ---- Plain1
    static String dataStrPlain1[]   =  { "name1(:x :p :z)" };
    static RelStore dataPlain1      = data(dataStrPlain1);
    static String rulesStrPlain1[]  = { "name2(:x, ?p, ?Z) <- name1(:x, ?p, ?Z)" };
    static RuleSet ruleSetPlain1    = RulesParser.rules(rulesStrPlain1);
    // ---- Plain
    static String dataStrPlain[] =
        {"name1(:x :p :Z)"
        ,"name1(:x :p :z)"
    };
    static RelStore dataPlain = data(dataStrPlain);
    static String rulesStrPlain[] = {
         "name(:x, :r, ?Z) <- name1(:x, :p, ?Z)"
        ,"name(:x, :q, ?Z) <- name1(:x, :p, ?Z)"
        ,"name2(:x, ?p, ?Z) <- name1(:x, ?p, ?Z)"
      };
    static RuleSet ruleSetPlain = RulesParser.rules(rulesStrPlain);
    static String dataStrRDFS[] = {
        "(:x :p 123)",
        "(:p rdfs:domain :T)",
        "(:T rdfs:subClassOf :T2)",
        "(:T2 rdfs:subClassOf :T3)"
    };
    static RelStore dataRDFS = data(dataStrRDFS);
    static RuleSet ruleSetRDFS = RuleSets.rulesRDFSbasic();
}

