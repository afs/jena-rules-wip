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

package dev;

public class NotesRDFS {

    // ---- RDFS
    // -- Release
    // [ ] org.apache.jena.rdfs

    // -- Refinement
    // Check and deal with:
    // [RDFS]
    // Setup.
    // [?] accABC -> streams?
    // [-] Set up function to calculate and behave like a Graph<X>
    // [x] Test empty setup!

    // [x] Strip MatchRDFS of unnecessary extras.
    //     find_ANY_type_T - ensure tests cover.
    //     Test, No subclasses, only D&R
    //     test find_X_type_T. Subtype in domain and as a superlcass.
    //     find_ANY_ANY_Y and Y is or has sub/super classes.

    // [x] Integration tests - dataset esp. findNG

    // [ ] Test SPARQL.
    // [ ] Fuseki Main --rdfs=

    // == Phase 2

    // [ ] TDB2
    // [ ] TDB1
    // [ ] assertSparql
    // [ ] SysRDFS  - register an OpExecutor?


    // [ ] Test for subproperties of rdf:type and other vocabulary.
    //     Factory: test for no RDFS and get out of the way (Dataset)

    // Tests for Transitive in ARQ?

    // ==== Other RDFS+
    //   rdfs:member
    //   list:member

     /* RDFS 3.0 / RDFS Plus /
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

        ---
        OWL, not in "RDFS 3.0"
        owl:disjointWith
        owl:differentFrom
        owl:complementOf
        owl:someValueOf
        owl:allValuesFrom
        owl:hasValue

Head is a single triple
   Transitive reasoner
   sameAs
   rules, single head
     :C owl:hasValue v, :C owl:onProperty :p, ?x :p v => ?x a C
     :C owl:hasValue v, :C owl:onProperty :p, ?x a C => ?x :p v
       someValuesFrom
       allValues
     :p inverseOf :q, ?x :p ?y => ?y :q ?x .
     :p symmetric , x :p ?y => ?y :p ?x .
     rdf:_N processes => rdf:member
     list:member => ????
     OWL RL

     OWL

*/

}
