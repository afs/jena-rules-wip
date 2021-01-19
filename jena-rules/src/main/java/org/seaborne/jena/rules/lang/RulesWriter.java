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

package org.seaborne.jena.rules.lang;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.RiotLib;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.Rule;
import org.seaborne.jena.rules.RuleSet;

public class RulesWriter {

    public static void write(IndentedWriter out, RuleSet ruleSet) {
        //RiotLib.writeBase(out, base, true);
        PrefixMap prefixMap = ruleSet.getPrefixMap();
        RiotLib.writePrefixes(out, prefixMap, true);
        if ( ! prefixMap.isEmpty() )
            out.println();
        NodeFormatter fmt = new NodeFormatterTTL(null, prefixMap);
        write(out, ruleSet, fmt);
    }

    public static void write(IndentedWriter out, RuleSet ruleSet, NodeFormatter fmt) {
        //RiotLib.writeBase(out, base, true);

        boolean first = true;
        for(Rule rule : ruleSet) {
            if ( ! first )
                // Gap.
                out.println();
            first = false;
            write(out, rule, fmt);
            out.println(" .");
        }
    }


    public static void write(IndentedWriter out, Rule rule, NodeFormatter fmt) {
        if ( rule.getHead() != null )
            write(out, rule.getHead(), fmt);

        if ( ! rule.getBody().isEmpty() ) {
            out.print(" <-");
            rule.getBody().forEach(r->{
                out.print(" ");
                write(out, r, fmt);
            });
        }
    }

    public static void write(IndentedWriter out, Rel rel, NodeFormatter fmt) {
        out.print(rel.getName());
        boolean first = true;
        out.print("(");
        for ( Node n : rel.getTuple() ) {
            if ( ! first )
                out.print(", ");
            first = false;
            fmt.format(out, n);
        }
        out.print(")");
    }


}

