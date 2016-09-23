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

import java.util.Collection ;
import java.util.HashSet ;
import java.util.Iterator ;
import java.util.Set ;
import java.util.stream.Stream ;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;

/** An inference processor for RDFS.
 * Adds derived triples to an accumulator.
 */
public class InferenceProcessorRDFS {
    private InferenceSetupRDFS setup ;
    private Collection<Triple> acc = null ;
    private final InferenceEngineRDFS engine ;
    private StreamTriple sink ;
    
    // Add iterator cases
    
    public InferenceProcessorRDFS(InferenceSetupRDFS setup) {
        this.setup = setup ;
        this.sink = acc::add ;
        this.engine = new InferenceEngineRDFS(setup, sink) ;
    }

    /** Calculate the set of triples from processing an iterator of triples */
    public Set<Triple> process(Stream<Triple> stream) {
        Set<Triple> acc = new HashSet<>() ;
        process(acc, stream) ;
        return acc ;
    }

    /** Calculate the set of triples from processing an iterator of triples */
    public void process(Collection<Triple> acc, Stream<Triple> stream) {
        stream.forEach(triple ->
            process(acc, triple.getSubject(), triple.getPredicate(), triple.getObject()) ) ;
    }

    /** Calculate the set of triples from processing an iterator of triples */
    public Set<Triple> process(Iterator<Triple> iter) {
        Set<Triple> acc = new HashSet<>() ;
        process(acc, iter) ;
        return acc ;
    }

    /** Calculate the set of triples from processing an iterator of triples */
    public void process(Collection<Triple> acc, Iterator<Triple> iter) {
        iter.forEachRemaining(triple ->
            process(acc, triple.getSubject(), triple.getPredicate(), triple.getObject()) ) ;
    }
    
    /** Calculate the set of triples from processing a triple */
    public Set<Triple> process(Triple t) {
        return process(t.getSubject(), t.getPredicate(), t.getObject()) ;
    }
    
    /** Calculate the set of triples from processing a triple */
    public Set<Triple> process(Node s, Node p, Node o) {
        Set<Triple> acc = new HashSet<>() ;
        process(acc, s, p, o) ;
        return acc ;
    }

    /** Accumulate the triples from processing triple t */
    public void process(Collection<Triple> acc, Triple t) {
        process(acc, t.getSubject(), t.getPredicate(), t.getObject()) ;
    }
    
    /** Accumulate the triples from processing triple t */
    public void process(Collection<Triple> acc, Node s, Node p, Node o) {
        // Threading!
        acc.add(Triple.create(s,p,o)) ;
        this.acc = acc ;
        engine.process(s, p, o) ;
        this.acc = null ;
    }

}
