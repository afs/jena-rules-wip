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
import org.seaborne.jena.inf_rdfs.engine.SetupRDFS;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_Node;

/** Factory for interference-related classes. */
public class InfFactory {

    /**
     * Create an RDFS inference graph with split A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data, Graph vocab) {
        return graphRDFS(data, new SetupRDFS_Node(vocab, data==vocab));
    }

    /**
     * Create an RDFS inference graph over a graph with both  with split A-box (data) and T-box (RDFS schema).
     */
    public static Graph graphRDFS(Graph data) {
        return graphRDFS(data, new SetupRDFS_Node(data, true));
    }

    /** Create an {@link SetupRDFS} */
    public static SetupRDFS_Node setupRDF(Graph vocab, boolean incDerivedDataRDFS) {
        return new SetupRDFS_Node(vocab, incDerivedDataRDFS);
    }

    // Modes of
    //   combined A-box, T-box but hiding derived data
    //   split A-box, T-box but with derived RDFS from data
    // are not supported.

    public static Graph graphRDFS(Graph data, SetupRDFS_Node setup) {
        return new GraphRDFS(data, setup);
    }

    /** Stream expand data based on a separate vocabulary */
    public static StreamRDF infRDFS(StreamRDF data, Model vocab) {
        return infRDFS(data, vocab.getGraph());
    }

    /** Stream expand data based on a separate vocabulary */
    public static StreamRDF infRDFS(StreamRDF data, Graph vocab) {
        SetupRDFS_Node setup = new SetupRDFS_Node(vocab, false);
        return infRDFS(data, setup);
    }

    /** Expand a stream of RDF using RDFS */
    public static StreamRDF infRDFS(StreamRDF data, SetupRDFS_Node setup) {
        return new InferenceStreamRDFS(data, setup);
    }
}
