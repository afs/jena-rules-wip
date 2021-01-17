package org.seaborne.jena.rules;

import java.util.stream.Stream;

import migrate.binding.Binding;
import migrate.binding.Sub;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.util.Context;
import org.seaborne.jena.rules.lang.RulesParser;
import org.seaborne.jena.rules.store.RelStoreBuilder;
import org.seaborne.jena.rules.store.RelStoreSimple;

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

public class Rules {

    // XXX Javadoc

    private static RuleExecCxt globalRuleExecContext = RuleExecCxt.global;

    private static Context globalRuleContext = dftContext();

    public static Context getContext() {
        return globalRuleContext;
    }

    /** Evaluate and return results as a {@link RelStore}. */
    public static RelStore eval(RelStore data, RuleSet ruleSet, EngineType engineType, Rel query) {
        RelStoreBuilder storeBuilder = RelStoreSimple.create();
        RulesEngine engine = RulesGraphBuilder.create(engineType, ruleSet, data);
        Stream<Binding> iter1 = engine.solve(query);
        iter1.map(b->Sub.substitute(b, query)).forEach(r->storeBuilder.add(r));
        return storeBuilder.build();
    }

//    public static Context setContext(Context context) {
//        if ( context == null )
//            context = dftContext();
//        globalRuleContext = context;
//        return globalRuleContext;
//    }

    private static Context dftContext() {
        // RIOT.getContext();
        return new Context();
    }

    // ----
    public static RulesGraphBuilder create() { return new RulesGraphBuilder(); }

    /** Create a new {@link RulesEngine}. */
    public static RulesEngine create(EngineType type, RuleSet ruleSet, RelStore baseData) {
        return RulesGraphBuilder.create(type, ruleSet, baseData);
    }

    /** Read a file of files. Use filename "-" for stdin. */
    public static RuleSet rules(String filename) {
        String baseIRI = null;
        if ( ! filename.equals("-") )
            baseIRI = IRILib.filenameToIRI(filename);
        RuleSet ruleSet = RulesParser.parseRuleSet(IO.openFileBuffered(filename), baseIRI);
        return ruleSet;
    }

    /** Return a graph that applies the given {@link RuleSet} to its output. */
    public static Graph withRules(Graph graph, RuleSet ruleSet) {
        return null;
    }

//    /** Return a graph that applies the given {@link RuleSet}, read from a file, to its output. */
//    public static Graph withRules(Graph graph, String ruleSetFilename) {
//        return null;
//    }

    private static Graph withRules(Graph graph, RulesEngine engine) {
        return null;
    }
}

