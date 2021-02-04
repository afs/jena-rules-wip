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

package org.seaborne.jena.inf_rdfs.setup;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.tdb.TDBException;
import org.apache.jena.tdb.store.DatasetGraphTDB;
import org.apache.jena.tdb.store.NodeId;
import org.apache.jena.tdb.store.nodetable.NodeTable;
import org.apache.jena.tdb.sys.TDBInternal;

/** RDFS setup in NodeId space (TDB1) */
public class SetupRDFS_TDB1 extends BaseSetupRDFS<NodeId> {
    private final DatasetGraphTDB dsgtdb;
    private final NodeTable nodetable;
    public SetupRDFS_TDB1(Graph vocab, DatasetGraph dsg) {
        this(vocab, dsg, false);
    }

    public SetupRDFS_TDB1(Graph vocab, DatasetGraph dsg, boolean incDerivedDataRDFS) {
        super(vocab, incDerivedDataRDFS);
        this.dsgtdb =  TDBInternal.getDatasetGraphTDB(dsg);
        if ( dsgtdb == null )
            throw new IllegalArgumentException("Not a TDB1 DatasetGraph");
        this.nodetable = dsgtdb.getTripleTable().getNodeTupleTable().getNodeTable();
    }

    @Override
    protected NodeId fromNode(Node node) {
        NodeId n = nodetable.getNodeIdForNode(node);
        if ( NodeId.isDoesNotExist(n) )
            throw new TDBException("Called to provide a NodeId for a Node not in the dataset: "+n);
        return n;
    }
}
