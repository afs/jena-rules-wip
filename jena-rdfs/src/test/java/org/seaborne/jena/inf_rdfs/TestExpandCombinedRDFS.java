/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.inf_rdfs;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.BeforeClass;

/** Test of RDFS.
 * Expanded graph, combined vocab and data.
 */
public class TestExpandCombinedRDFS extends AbstractTestGraphRDFS {

    private static Graph testGraphExpanded;
    @BeforeClass public static void setupHere() {
        Graph dataTest = GraphFactory.createDefaultGraph();
        testGraphExpanded = GraphFactory.createDefaultGraph();
        GraphUtil.addInto(dataTest, data);
        GraphUtil.addInto(dataTest, vocab);
        SetupRDFS<Node> setup = InfFactory.setupRDF(vocab, true);
        StreamRDF stream = StreamRDFLib.graph(testGraphExpanded);
        stream = new InfStreamRDFS(stream, setup);
        StreamRDFOps.graphToStream(dataTest, stream);
    }

    public TestExpandCombinedRDFS() {}

    @Override
    protected boolean removeVocabFromReferenceResults() {
        return false;
    }

    @Override
    protected Graph getTestGraph() {
        return testGraphExpanded;
    }

    @Override
    protected String getTestLabel() {
        return "Expaned, combined";
    }
}

