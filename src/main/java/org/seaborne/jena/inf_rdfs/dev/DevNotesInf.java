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

package org.seaborne.jena.inf_rdfs.dev;

public class DevNotesInf {
    // First - MVP - GraphRDFS working in Node space.
    
    // TODO: Other RDFS+ 
    //   rdfs:member
    //   list:member

    // Second - translate into BGPs for X space execution?  Only is vocab and data in same TDB. Refinement laster.
    
//    IF { body = BGP+paths+FILTER+BIND }
//    THEN { BGP }
//
//    Plain triples:
//
//    IF {} THEN { triple }
//
//    Stardog: multiple triples -> several one triple rule.
//      To make backward chaining?
//
//    Special predicates
//        sp:directType
//        sp:directSubClassOf
//        sp:strictSubClassOf
//
//        sp:directSubPropertyOf
//        sp:strictSubPropertyOf
//

    // Redesign built-into Node/engine
    //   TDB cache is enough?
    //   Need to split InfSetupRDFS<T> > InferenceSetupRDFS
    //    so can have a converted from InfSetupRDFS<NodeId> to InferenceSetupRDFS
    
    // StreamTriple, StreamQuad - remove?
    //    StreamTriple - used in InferenceEngineRDFS, InferenceProcessorRDFS
    
    // Stream<T> version of InferenceEngineRDFS (flatMap)
    // Or Iterator version.
    //    Iter.flatMap(Function<X, Iterator<X>>)
    
    // Special transitive processor
    
    /* See Description */
    
    // Use and test InfFactory.
    
    // TDB : need to store the InferenceSetupRDFS_TDB
    // That is a lot of tables (8) so for now compile to in-memory.
    // First hack: <Node> 
    
    /*
     * http://jena.apache.org/documentation/inference/#OWLcoverage
     *   Micro
     *   
     * rdfs:member, list:member
     * Remove rule rdfs5b ( P subPropertyOf P )
     * 
     * OWL 2 RL / OWL 2 QL.
     * http://www.w3.org/TR/owl2-profiles/#OWL_2_QL
     * http://www.w3.org/TR/owl2-profiles/#OWL_2_RL

     * RDFS 3.0 / RDFS Plus / 
     * http://www.w3.org/2009/12/rdf-ws/papers/ws31
        rdfs:domain
        rdfs:range
        rdfs:subClassOf
        rdfs:subPropertyOf
        
        owl:equivalentClass 
        owl:equivalentProperty
        
        owl:sameAs
        owl:inverseOf
        (no reflexive)
        owl:TransitiveProperty
        owl:SymmetricProperty

        owl:FunctionalProperty
        owl:InverseFunctionalProperty
     *
Head is a single triple
   Transitive reasoner
   sameas
   rules, single head
     :C owl:hasValue v, :C owl:onProperty :p, ?x :p v => ?x a C
     :C owl:hasValue v, :C owl:onProperty :p, ?x a C => ?x :p v
       someValuesFrom
       allValues
     :p inverseOf :q, ?x :p ?y => ?y :q ?x .
     :p symmetric , x :p ?y => ?y :p ?x .
     rdf:_N processess => rdf:member
     list:member => ????
     OWL RL
*/
    
}

