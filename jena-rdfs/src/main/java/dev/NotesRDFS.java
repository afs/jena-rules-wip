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
    // Check and deal with:
    // [RDFS]

    // Remove _0's

    // Replace.
    // org.apache.jena.riot.process.inf

    // Two engines:
    //   Matcher (for find) : Find3_X
    //   Streamer : StreamInEngine_X
    //     SuperClass for holding setup + constants.
    //     Or one engine_X

    // Dataset, Find4.

    // Review
    //   Unnecessary Triple.create, other work.
    //   Ca we use/share more between  StreamInf and Find3?

    // StreamInf_X :: The expander.
    //   Tests needed
    // better than Find3 code? Unlikely but try pure StreamInfEngineRDFS/GraphRDFS

    // OpExecutor version?


    // First - MVP - GraphRDFS working in Node space.

    // SetupRDFS - not <T>, have subclass to get that needed for Find3_X

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

