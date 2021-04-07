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

    // X subclass X not inferred. Inc/exc issue?

    // ---- Quad solver.
    // [Match]
    //   PatternMatchData - notes RX integration.
    // Can/should we merge OpExecutor, OpExecutorQuads?

    // [ ] Testing
    //     General, TIM, TDB1, TDB2.
    // [x] Library in ARQ:; Abortable, IteratorAbortable.
    // * SolverRX: abortable? (TDB - abortable?)

    // Clearup/rename: TDB1, TDB2
    //   SolverLib, Solver, SolverRx

    // Ready. Use with TIM (and, in tests, general).

    // ---- RDFS

    // [x] More tests: TestInfStreamRDFS
    // [ ] test:assemblers.
    // [-] Expose the stream sets - class, property, domain, range. Each has a "if work to do start so keep as-is.
    // [?] Closing iterators IIF stream ends
    // [ ] Check hiding modes.
    // [ ] SysRDFS  - register OpExecutor.
    // [ ] Integration tests - dataset esp. findNG
    // [ ] Try AbstractTestGraphRDFS : min-backwards. -> 4 failures because of
    //    org.seaborne.jena.inf_rdfs.TestGraphCombinedRDFS
    //      test_rdfs_11(org.seaborne.jena.inf_rdfs.TestGraphCombinedRDFS)
    //      test_rdfs_16a(org.seaborne.jena.inf_rdfs.TestGraphCombinedRDFS)
    //    org.seaborne.jena.inf_rdfs.TestMaterializedCombinedRDFS
    //      test_rdfs_11(org.seaborne.jena.inf_rdfs.TestMaterializedCombinedRDFS)
    //      test_rdfs_16a(org.seaborne.jena.inf_rdfs.TestMaterializedCombinedRDFS)
    // rdfs_16a is short.
//    Expected: find(null, null, http://example/X)
//        http://example/b @rdf:type http://example/X
//      Got (Combined data,vocab):
//        http://example/b @rdf:type http://example/X
//        http://example/X @rdfs:subClassOf http://example/X  **

//    Expected: find(null, null, http://example/X)
//        http://example/b @rdf:type http://example/X
//      Got (Expanded, combined):
//        http://example/X @rdfs:subClassOf http://example/X  **
//        http://example/b @rdf:type http://example/X





    // [ ] TDB2
    // [ ] TDB1

    // Can we see the vocab?  And get subclass of subclass
    //   ?SC rdfs:subClassOf :Type .
    //   :R  rdfs:subClassOf :S1 .
    //   :R  rdfs:subClassOf :S2 .
    // Data has :subClassOf, :subProperty Of:
    // Domain and range need expanding?
    //   :p rdfs:domain :T1 . :T1 subClassOf :T2 => :p rdfs:domain :T2 .
    //       includeDerivedDataRDFS

    // [ ] assertSparql

    // """
    // Modes of
    //   combined A-box, T-box but hiding derived data
    //   split A-box, T-box but with derived RDFS from data
    // are not supported.
    // """

    // [x] AssemblerDatasetRDFS
    // [x] AssemblerGraphRDFS
    // [x] org.apache.jena.riot.process.inf.InfFactory

    // Check and deal with:
    // [RDFS]

    // Test for subproperties of rdf:type and other vocabulary.
    // Factory: test for no RDFS and get out of the way (Dataset)

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

