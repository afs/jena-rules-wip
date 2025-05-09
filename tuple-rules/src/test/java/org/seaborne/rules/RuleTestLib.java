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

import java.util.ArrayList;
import java.util.List;

import org.seaborne.rules.lang.RulesParser;

public class RuleTestLib {

    static Iterable<Object[]> allEngines() {
        return engines(EngineType.FWD_NAIVE,
                       EngineType.BKD_NON_RECURSIVE_SLD);
    }

    static Iterable<Object[]> engines(EngineType ... types) {
        List<Object[]> x = new ArrayList<>();
        for( EngineType t : types ) {
            x.add(new Object[]{t.displayName(), t});
        }
        return x;
    }

    static RelStore data(String dataStr) {
        if ( dataStr == null )
            return RelStoreFactory.empty();
        return RulesParser.parseData(dataStr);
    }

    // Multiple rules, one string per rule.
    static RuleSet rules(String...strings) {
        return RulesParser.parseRules(strings);
    }

    static RuleSet ruleSet(String ruleStr) {
        return RulesParser.parseRuleSet(ruleStr);
    }

    // Different words, same thing.

    static Rel query(String queryStr) {
        return RulesParser.parseAtom(queryStr);
    }

    static Rel atom(String queryStr) {
        return RulesParser.parseAtom(queryStr);
    }

    static Rel rel(String queryStr) {
        return RulesParser.parseAtom(queryStr);
    }


}

