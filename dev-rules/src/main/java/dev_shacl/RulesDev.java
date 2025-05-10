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

package dev_shacl;

import org.apache.jena.atlas.io.AWriter;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL;
import org.apache.jena.riot.system.Prefixes;
import org.apache.jena.sparql.sse.SSE;

public class RulesDev {

    public static Node node(String x) {
        return switch(x) {
            case null, "", "_" -> Node.ANY;
            default -> SSE.parseNode(x);
        };
    }

    /**
     * Write a graph in flat Turtle.
     */
    public static void write(Graph graph) {
        RDFWriter.source(graph).format(RDFFormat.TURTLE_FLAT).output(System.out);
    }

    /**
     * Print a graph in flat, abbreviated triples, but don't print the prefix map
     * Development use.
     */
    public static void print(Graph graph) {
        NodeFormatter nt = new NodeFormatterTTL(null, Prefixes.adapt(graph));

        AWriter out = IO.wrapUTF8(System.out);
        graph.find().forEach(t->{
            nt.format(out, t.getSubject());
            out.print(" ");
            nt.format(out, t.getPredicate());
            out.print(" ");
            nt.format(out, t.getObject());
            out.println();
        });
        out.flush();
    }

}