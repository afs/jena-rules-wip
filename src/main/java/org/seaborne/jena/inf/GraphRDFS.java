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

import java.util.* ;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.graph.GraphWrapper ;
import org.apache.jena.util.iterator.ExtendedIterator ;
import org.apache.jena.util.iterator.WrappedIterator ;
import org.seaborne.jena.inf.engine.Find3_Graph;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

/** RDFS graph over a plain base graph.
 *  Precalculated RDFS.
 */
public class GraphRDFS extends GraphWrapper {
    private static Logger log = LoggerFactory.getLogger(GraphRDFS.class) ;
    
    private Find3_Graph fGraph ;
    private InferenceSetupRDFS setup ;

    public GraphRDFS(InferenceSetupRDFS setup, Graph graph) {
        this(setup, graph, new Find3_Graph(setup, graph));
    }
    
    public GraphRDFS(InferenceSetupRDFS setup, Graph graph, Find3_Graph fGraph) {
        super(graph) ;
        this.setup = setup ;
        this.fGraph = fGraph;
    }
    
    @Override
    public ExtendedIterator<Triple> find(Triple m) {
        return find(m.getSubject(), m.getPredicate(), m.getObject()) ;
    }

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        Iterator<Triple> iter = fGraph.find(s, p, o).iterator() ;
        return WrappedIterator.create(iter) ;
    }

    static public Iterator<Triple> print(Iterator<Triple> iter) {
        List<Triple> triples = new ArrayList<>() ; 
        for ( ; iter.hasNext() ;)
            triples.add(iter.next()) ;
        triples.stream().forEach(t -> System.out.println("# "+t)) ;
        return triples.iterator() ;
    }
}
