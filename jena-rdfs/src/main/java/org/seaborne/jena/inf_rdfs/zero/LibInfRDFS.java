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

package org.seaborne.jena.inf_rdfs.zero;

//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import org.apache.jena.graph.Node;
//import org.apache.jena.graph.Triple;
/** 
 * Apply an {@link ApplyRDFS_0} to various ways triples are grouped together. 
 */
public class LibInfRDFS {
//    // XXX Needed??
//    
//    /** Calculate the set of triples from processing an iterator of triples */
//    public static Set<Triple> process(InferenceSetupRDFS setup, Stream<Triple> stream) {
//        Set<Triple> acc = new HashSet<>();
//        process(setup, acc, stream);
//        return acc;
//    }
//
//    /** Calculate the set of triples from processing an iterator of triples */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Stream<Triple> stream) {
//        stream.forEach(triple ->
//            process(setup, acc, triple.getSubject(), triple.getPredicate(), triple.getObject()) );
//    }
//
//    /** Calculate the set of triples from processing an iterator of triples */
//    public static Set<Triple> process(InferenceSetupRDFS setup, Iterator<Triple> iter) {
//        Set<Triple> acc = new HashSet<>();
//        process(setup, acc, iter);
//        return acc;
//    }
//
//    /** Calculate the set of triples from processing an iterator of triples */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Iterator<Triple> iter) {
//        iter.forEachRemaining(triple ->
//            process(setup, acc, triple.getSubject(), triple.getPredicate(), triple.getObject()) );
//    }
//    
//    /** Calculate the set of triples from processing a triple */
//    public static Set<Triple> process(InferenceSetupRDFS setup, Triple t) {
//        return process(setup, t.getSubject(), t.getPredicate(), t.getObject());
//    }
//    
//    /** Calculate the set of triples from processing a triple */
//    public static Set<Triple> process(InferenceSetupRDFS setup, Node s, Node p, Node o) {
//        Set<Triple> acc = new HashSet<>();
//        process(setup, acc, s, p, o);
//        return acc;
//    }
//
//    /** Accumulate the triples from processing triple t */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Triple t) {
//        process(setup, acc, t.getSubject(), t.getPredicate(), t.getObject());
//    }
//    
//    /** Accumulate the triples from processing triple t */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Node s, Node p, Node o) {
//        LibInf.process(setup, acc, s, p, o);
//    }
}
