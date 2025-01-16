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

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.Graph;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RIOT;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.G;
import org.seaborne.jena.rules.EngineType;
import org.seaborne.jena.rules.RuleSet;
import org.seaborne.jena.rules.Rules;

public class RunRules {

    static {
        LogCtl.setLog4j2();
        JenaSystem.init();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

    public static void main(String...a) {
        //mainGraph(EngineType.BKD_NON_RECURSIVE_SLD);
        mainGraph(EngineType.FWD_NAIVE);
    }

    public static void mainGraph(EngineType type) {
        System.out.println("mainGraph(EngineType="+type);
        Graph baseGraph = RDFDataMgr.loadGraph("D.ttl");
        RuleSet ruleSet = R.ruleSetRDFS;
        Graph graph = Rules.create().baseGraph(baseGraph).rules(ruleSet).system(type).build();
        //graph.find().forEachRemaining(System.out::println);
        //System.out.println();
        System.out.println("==== Base Graph");
        RDFDataMgr.write(System.out, baseGraph, Lang.TTL);
        System.out.println("==== Inferred Graph");
        RDFDataMgr.write(System.out, graph, RDFFormat.TURTLE_PRETTY);
        System.out.println("====");
        System.out.println( G.typesOfNodeAsList(graph, SSE.parseNode(":s")) );
        System.out.println("====");
        System.out.println("DONE");
    }
}
