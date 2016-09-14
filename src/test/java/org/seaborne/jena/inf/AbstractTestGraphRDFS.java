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

import org.apache.jena.graph.Graph ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.riot.RDFDataMgr ;
import org.junit.BeforeClass ;

/** Test of RDFS, with separate data and vocabulary, no RDFS in the deductions.
 * See TestCombinedRDFS.
 */
public abstract class AbstractTestGraphRDFS extends AbstractTestRDFS {
    protected static Model vocab ;
    // Data - no vocab */
    protected static Model data ;
    // Data - with vocab */
    protected static Model dataVocab ;

    protected static InferenceSetupRDFS setupExc ;
    protected static InferenceSetupRDFS setupInc ;
    
    // The reference graph for this test.
    static Graph infGraph ;
    // The main test target
    Graph testGraphRDFS ;
    
    static final String DIR = "testing/Inf" ;
    static final String DATA_FILE = DIR+"/rdfs-data.ttl" ;
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl" ;
    //static final String RULES_FILE = DIR+"/rdfs-min-backwards.rules" ;
    static final String RULES_FILE = DIR+"/rdfs-min.rules" ;

    // Out of control!
    
    @BeforeClass public static void setupClass() {
        vocab = RDFDataMgr.loadModel(VOCAB_FILE) ;
        data = RDFDataMgr.loadModel(DATA_FILE) ;
        dataVocab = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(dataVocab, DATA_FILE) ;
        RDFDataMgr.read(dataVocab, VOCAB_FILE) ;
        infGraph = createRulesGraph(data, vocab, RULES_FILE) ;
        
        // inc vocab / infer vocab -> 
        // true -> Combined (inc vocab / infer vocab)
        // false -> separate, no infer
        
        setupInc = new InferenceSetupRDFS(vocab, true) ;
        setupExc = new InferenceSetupRDFS(vocab, false) ;
    }
    
    protected AbstractTestGraphRDFS() {
        testGraphRDFS = createGraphRDFS() ;
    }
    
    protected abstract Graph createGraphRDFS() ; 

    @Override
    final protected Graph getTestGraph() {
        return testGraphRDFS ;
    }
    
    /** Return the graph that gives the right answers */ 
    @Override
    final protected Graph getReferenceGraph() {
        return infGraph ;
    }

    @Override
    final protected String getReferenceLabel() {
        return "Inference" ;
    }
}

