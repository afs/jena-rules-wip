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

package org.seaborne.jena.inf;

import org.apache.jena.graph.Factory ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

/** Test of RDFS.
 *  Expand the inferences graph and then test.
 */ 
public abstract class AbstractTestExpandRDFS extends AbstractTestRDFS {
    static Model vocab ;
    static Model data ;

    static InferenceSetupRDFS setup ;
    // Jena graph to check results against.
    static Graph infGraph ;
    // The main test target
    static Graph testGraphExpanded ;
    
    static final String DIR = "testing/Inf" ;
    static final String DATA_FILE = DIR+"/rdfs-data.ttl" ;
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
    static final String RULES_FILE = DIR+"/rdfs-min.rules" ;
    static boolean everything = true ;
    
    public static void setup(boolean combined) {
        everything = combined ;
        if ( combined ) {
            vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
            data = RDFDataMgr.loadModel(DATA_FILE) ;
            data.add(vocab) ;
        } else {
            // Seperate vocab and data.
            vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
            data = RDFDataMgr.loadModel(DATA_FILE) ;
        }
        
        infGraph = createRulesGraph(data, vocab, RULES_FILE) ;
        
        setup = new InferenceSetupRDFS(vocab, combined) ;

        // Expansion Graph
        testGraphExpanded = Factory.createDefaultGraph() ;
        StreamRDF stream = StreamRDFLib.graph(testGraphExpanded) ;
        // Apply inferences.
        stream = new InferenceProcessorStreamRDF(stream, setup) ;
        
        StreamOps.graphToStream(data.getGraph(), stream) ;
    }
    
    @Override
    final protected Graph getReferenceGraph() {
        return infGraph ;
    }

    @Override
    final protected Graph getTestGraph() {
        return testGraphExpanded ;
    }

    @Override
    protected String getReferenceLabel() {
        return "Inference" ;
    }

    @Override
    protected String getTestLabel() {
        return "Expanded" ;
    }

    @Override
    protected boolean removeVocabFromReferenceResults() {
        return ! everything ;
    }
}

