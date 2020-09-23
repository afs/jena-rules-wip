/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev;

public class NotesRules {
    // "Just implement" backwards


    // Rels and Tuples:
    //    tuple.map(Function<X,X>)
    //    TupleFactory

    //-----

    // Engines
    //   RDFS inf_rdfs
    //   Transitive inf_transitive
    //   (gone) inf2
    //   Rules
    //     Backwards
    //     Naive
    //     Seminaive


    // RDFS in SPARQL - any advantages?

    // inf vs inf2?

    // * inf_rdfs : hardcoded RDFS - work on Nodes - finished
    // * inf_transitive - placeholder for an extract transitive rule reasoner component.
    // * inf2 - rules engine. No specials - even transitive is by rule.
    // * rules - parser, datastructure for rules.

    // (Re) naming.
    // RuleTerm : named term.         (== Rel)
    // Rule: head and body.
    //   Currently, one term head.
    // TermStore -- store sends of bindings?
    // Need
    // BufferingGraph - no - need Find3

    // TermStore : BindingStore

    // (t1) (t2) <- (x1) (x2) (x3)
    // ==>
    //     (t1) (t2) <- project(?a,?b,?c) <- (x1) (x2) (x3)

    // General: Match, filter, calculate -> bindings -> pattern

    //     (t1) (t2) <- project(?a,?b,?c) <- (x1) (x2) (x3)

    //   project(?a,?b,?c) == collection of Bindings

    //   Cache and mark rule as "solved".
    // And relstore is "conclusions store"
    //   List of (var,value) or maplet. or specials fixed lengths.

    // DatasetGraph vs Graph

    // Backwards

    // ($x :p 123 ) <- ($x :q $z) ($z :r 123)
    // Ask for:
    //   (?a :p 123) -- replace if no recursion
    //   (?a :p ?o)  -- Match 123 to ?o and ... solve.
    //   (<a> :p ?o) -- Ground rule,


}
