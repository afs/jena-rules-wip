package org.seaborne.jena.rules.api;

import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.util.Context;
import org.seaborne.jena.rules.RulesEngine;
import org.seaborne.jena.rules.RulesException;
import org.seaborne.jena.rules.RuleSet;
import org.seaborne.jena.rules.lang.RulesParser;

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
    private static Context globalRuleContext = dftContext();

    public static Context getContext() {
        return globalRuleContext;
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
    public static Builder create() { return new Builder(); }

    public static RulesEngine createEngine(EngineType type) {
        switch (type) {
            case FWD_NAIVE :
            case FWD_NAIVE_JACOBI :
                break;
            case FWD_NAIVE_GUEASS_SEIDEL :
                break;
            case FWD_SEMINAIVE :
                break;

            case BKD_NON_RECURSIVE_SLD :
                break;
            case BKD_QSQI :
                break;
            case BKD_QSQR :
                break;
            default :
                throw new RulesException("No such engine type: "+type);
        }
        throw new RulesException("Not implemented: "+type);
    }

    /** Read file (or stdin) of rules */
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

