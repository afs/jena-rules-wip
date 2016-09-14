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

package org.seaborne.jena.inf ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;
import org.apache.jena.tdb.store.NodeId ;
import org.apache.jena.tdb.store.nodetable.NodeTable ;

/** RDFS setup in NodeId space */
public class InferenceSetupRDFS_TDB extends BaseInfSetupRDFS<NodeId>{
    private final DatasetGraphTDB dsgtdb ;
    private final NodeTable nodetable ;

    public InferenceSetupRDFS_TDB(Graph vocab, DatasetGraphTDB dsgtdb) {
        this(vocab, dsgtdb, false) ;
    }

    public InferenceSetupRDFS_TDB(Graph vocab, DatasetGraphTDB dsgtdb, boolean incDerivedDataRDFS) {
        super(vocab, incDerivedDataRDFS) ;
        this.dsgtdb = dsgtdb ;
        this.nodetable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable() ;
    }
    public InferenceSetupRDFS_TDB(Model vocab, DatasetGraphTDB dsgtdb) {
        this(vocab, dsgtdb, false) ;
    }
    
    public InferenceSetupRDFS_TDB(Model vocab, DatasetGraphTDB dsgtdb, boolean incDerivedDataRDFS) {
       super(vocab, incDerivedDataRDFS) ;
       this.dsgtdb = dsgtdb ;
       this.nodetable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable() ;
    }

    @Override
    protected NodeId fromNode(Node node) {
        NodeId n = nodetable.getAllocateNodeId(node) ;
        return n ;
    }
}
