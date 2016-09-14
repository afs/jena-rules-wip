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

/** Test of RDFS.
 * <li> combined data and vocabulary,
 * <li> test graph performs inference on find() 
 */
public class TestGraphCombinedRDFS extends AbstractTestGraphRDFS {
    
    @Override
    protected Graph createGraphRDFS() {
        return new GraphRDFS(setupInc, dataVocab.getGraph()) ;
    }
    
    @Override
    protected String getTestLabel() {
        return "Combined GraphRDFS" ;
    }

    @Override
    protected boolean removeVocabFromReferenceResults() {
        return false ;
    }

}


