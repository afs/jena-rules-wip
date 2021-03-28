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

package org.seaborne.jena.inf_rdfs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.CollectorStreamTriples;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;

public class TestInfStreamRDFS {

    static final String DIR = "testing/Inf";
    static final String DATA_FILE = DIR+"/rdfs-data.ttl";
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";
    //static final String RULES_FILE = DIR+"/rdfs-min-backwards.rules";
    static final String RULES_FILE = DIR+"/rdfs-min.rules";
    protected static Graph vocab;
    protected static Graph data;

    static {
        vocab = RDFDataMgr.loadGraph(VOCAB_FILE);
        data = RDFDataMgr.loadGraph(DATA_FILE);
    }

    @Test public void basic_0() {
        Graph graph = inf(vocab, stream->StreamRDFOps.sendGraphToStream(data, stream));
        //RDFDataMgr.write(System.out, graph, Lang.TTL);
        assertEquals(5, data.size());
        assertEquals(13, graph.size());
    }

    private static Node node_c  = SSE.parseNode(":c");
    private static Node node_X  = SSE.parseNode(":X");
    private static Node node_p  = SSE.parseNode(":p");
    private static Node node_Q  = SSE.parseNode(":Q");
    private static Node node_Q2 = SSE.parseNode(":Q2");
    private static Node rdfType = NodeConst.nodeRDFType;

    @Test public void infer_1() {
        Triple t = SSE.parseTriple("(:c :p :x)");
        Graph graph = inf(vocab, x->x.triple(t));

        long count1 = G.countSP(graph, node_c, rdfType);
        assertEquals(2, count1);

        assertTrue(G.isOfType(graph, node_c, node_Q));
        assertTrue(G.isOfType(graph, node_c, node_Q2));
    }

    @Test public void infer_2() {
        Triple t = SSE.parseTriple("(:X rdf:type :T)");
        // Types :T :T2 :T3 and :U.
        Graph graph = inf(vocab, x->x.triple(t));
        //RDFDataMgr.write(System.out, graph, Lang.TTL);
        long count1 = G.countSP(graph, node_X, rdfType);
        assertEquals(4, count1);
        // And only these type triples
        assertEquals(4, graph.size());
    }

    // Inference to a list
    private static List<Triple> infOutput(Triple ... triples) {
        CollectorStreamTriples dest = new CollectorStreamTriples();
        StreamRDF stream = InfFactory.infRDFS(dest, vocab);
        exec(stream, x->Arrays.stream(triples).forEach(x::triple));
        return dest.getCollected();
    }

    // Inference to graph
    private static Graph inf(Graph vocab, Consumer<StreamRDF> action) {
        Graph graph = GraphFactory.createDefaultGraph();
        StreamRDF dest = StreamRDFLib.graph(graph);
        StreamRDF stream = InfFactory.infRDFS(dest, vocab);
        exec(stream, action);
        return graph;
    }

    private static void exec(StreamRDF stream, Consumer<StreamRDF> action) {
        stream.start();
        try {
            action.accept(stream);
        } finally { stream.finish(); }
    }
}
