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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.DatasetGraph;

/** Factory for interference-related classes. */
public class InfFactory {

    /**
     * Create an RDFS inference graph with split A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data, Graph vocab) {
        return graphRDFS(data, new SetupRDFS(vocab, data==vocab));
    }

    /**
     * Create an RDFS inference graph over a graph with both A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data) {
        return graphRDFS(data, new SetupRDFS(data, true));
    }

    /**
     * Create an RDFS inference graph over a graph according to an {@link SetupRDFS}.
     */
    public static Graph graphRDFS(Graph data, SetupRDFS setup) {
        return new GraphRDFS(data, setup);
    }

    /**
     * Create an RDFS inference dataset.
     */
    public static DatasetGraph datasetRDFS(DatasetGraph data, SetupRDFS setup) {
        return new DatasetGraphRDFS(data, setup);
    }

    /**
     * Create an RDFS inference dataset.
     */
    public static DatasetGraph datasetRDFS(DatasetGraph data, Graph vocab ) {
        return new DatasetGraphRDFS(data, new SetupRDFS(vocab, true));
    }

    /** Create an {@link SetupRDFS} */
    public static SetupRDFS setupRDFS(Graph vocab, boolean incDerivedDataRDFS) {
        return new SetupRDFS(vocab, incDerivedDataRDFS);
    }

    /** Stream expand data based on a separate vocabulary */
    public static StreamRDF infRDFS(StreamRDF data, Model vocab) {
        return infRDFS(data, vocab.getGraph());
    }

    /** Stream expand data based on a separate vocabulary */
    public static StreamRDF infRDFS(StreamRDF data, Graph vocab) {
        SetupRDFS setup = new SetupRDFS(vocab, false);
        return infRDFS(data, setup);
    }

    /** Expand a stream of RDF using RDFS */
    public static StreamRDF infRDFS(StreamRDF data, SetupRDFS setup) {
        return new InfStreamRDFS(data, setup);
    }
}
