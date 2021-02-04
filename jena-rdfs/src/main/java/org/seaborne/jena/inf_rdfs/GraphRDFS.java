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

import java.util.Iterator;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.seaborne.jena.inf_rdfs.engine.Find3_Graph;
import org.seaborne.jena.inf_rdfs.engine.SetupRDFS;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * RDFS graph over a plain base graph.
 */
public class GraphRDFS extends GraphWrapper {
    private static Logger log = LoggerFactory.getLogger(GraphRDFS.class);
    private Find3_Graph fGraph;
    private SetupRDFS<Node> setup;
    public GraphRDFS(Graph graph, SetupRDFS_Node setup) {
        super(graph);
        this.setup = setup;
        this.fGraph = new Find3_Graph(setup, graph);
    }

    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject());
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Iterator<Triple> iter = fGraph.find(s, p, o).iterator();
        return WrappedIterator.create(iter);
    }

    // This is a read-only RDFS "view".

//
//
//    // Transactions.
//
//    // Contains
//
//    // isEmpty
//
//    // size (!!)
//
//    private boolean isSetup = false;
//    private void change(Triple t) {}
//    private void checkSetup() {
//        //this.setup = new setup;
//    }
//
// implements GraphWithPerform
//    @Override
//    public void performAdd(Triple t) {
//        change(t);
//        super.add(t);
//    }
//
//    @Override
//    public void performDelete(Triple t) {
//        change(t);
//        super.delete(t);
//    }
//
//    @Override
//    public void remove(Node s, Node p, Node o) {
//        // Decompose.
//        // Execute on base.
//        super.remove(s, p, o);
//    }
//
//    @Override
//    public int size() {
//        // Report the size of the underlying graph.
//        return super.size();
//    }

    @Override
    public boolean dependsOn(Graph other) {
        if ( other == super.get() )
            return true;
        return super.dependsOn(other);
    }
}
