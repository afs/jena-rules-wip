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

    // Quad solver.
    // [Match]
    // Check effect of case insensitive language change.

    // [ ] Testing
    //     General, TIM, TDB1, TDB2.
    // [ ] Library in ARQ:; Abortable, IteratorAbortable.
    // [ ] SolverRX: abortable? (TDB - abortable?)
    // [ ] Close for GraphMatcher stream.

    // Maybe use equalTerms in TDB1,TDB2 (means getting Node)

    // Clearup/rename: TDB1, TDB2
    //   SolverLib, Solver, SolverRx

    // [ ] TDB2
    // [ ] TDB1

    // ---- Abortable
    //
    // Only need at the data touch points + sorting, not every iterator?
    // [ ] Library in ARQ:; Abortable, IteratorAbortable. Share TDB
    // [ ] SolverRX: abortable? (TDB - abortable?)

    // ---- RDFS

    // [ ] More tests: TestInfStreamRDFS
    // [ ] AssemblerDatasetRDFS
    // [ ] AssemblerGraphRDFS

    // Check and deal with:
    // [RDFS]

    // Test for subproperties of rdf:type and other vocabulary.
    // Factory: test for no RDFS and get out of the way (Dataset)

    // Tests for Transitive in ARQ?


    // TODO: Other RDFS+
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
*/

}

