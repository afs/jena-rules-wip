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

package org.seaborne.jena.inf_rdfs ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.system.StreamRDF ;
import org.seaborne.jena.inf_rdfs.engine.InferenceSetupRDFS;
import org.seaborne.jena.inf_rdfs.engine.InferenceSetupRDFS_Node;

/** Factory for interference-related classes. */ 
public class InfFactory {
    
    /** Split A-box and T-box */
    public static Graph graphRDFS(Graph data, Graph vocab) {
        return graphRDFS(data, new InferenceSetupRDFS_Node(vocab, data==vocab)) ;
    }

    /** Data contains A-box and T-box */ 
    public static Graph graphRDFS(Graph data) {
        return graphRDFS(data, new InferenceSetupRDFS_Node(data, true)) ;
    }

    /** Create an {@link InferenceSetupRDFS} */
    public static InferenceSetupRDFS setupRDF(Graph vocab, boolean incDerivedDataRDFS) {
        return new InferenceSetupRDFS_Node(vocab, incDerivedDataRDFS);
    }
    
    // Modes of 
    //   combined A-box, T-box but hiding derived data
    //   split A-box, T-box but with derived RDFS from data
    // are not supported.

    public static Graph graphRDFS(Graph data, InferenceSetupRDFS setup) {
        return new GraphRDFS(setup, data) ;
    }

    /** Stream expand data based on a separate vocabulary */ 
    public static StreamRDF inf(StreamRDF data, Model vocab) {
        return inf(data, vocab.getGraph()) ; 
    }

    /** Stream expand data based on a separate vocabulary */ 
    public static StreamRDF inf(StreamRDF data, Graph vocab) {
        InferenceSetupRDFS setup = new InferenceSetupRDFS_Node(vocab, false) ;
        return inf(data, setup) ;
    }

    /** Expand a stream of RDF using RDFS */
    public static StreamRDF inf(StreamRDF data, InferenceSetupRDFS setup) {
        return new InferenceStreamRDFS(data, setup) ;
    }
}