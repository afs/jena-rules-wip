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
    // INVEST
    //   APIs
    //    RuleEngine; query -> Binding stream.
    // Indexing triple rules by predicate.

    // [X] API
    // [-] Tests
    //     Prefix and base
    // [X] Stream<Rel>+query -> binding.

    // rdf:database and RDFS

    // api.Rules : evaluate to RelStore.
    // static RelStore eval(RelStore data, RuleSet ruleSet, Rel query) {
    //   + EngineType
    // Chase DRY TestRuleSolver, ...

    // Tests
    // TestRuleSolve
    //    TestRuleQuery of a h < body1 body2 body 3 .

    // Rewrite Bkd solver.

    // Data only patterns
    //  "direct" RDFS transitive rules.

    // BkwSolver = Resolver
    //  Work through on paper.
    //  Solver0 - MGU and BFI! For non-recursive datalog.
    //  Rewrite for clarity - test components like data solver.

    // v1: Non-recursive datalog
    //   Infrastructure, Parser, Graphs etc.

    // Collapse to one project for datalog
    // Class EDB (Graph)

    // Renames:
    //   Rel -> Atom

    // MGU:
    // RHS -> LHS var , LHS var -> constant ==> RHS -> constant

    // ---- tests
    // Itr2,3,4
    // mgu
    // ----

    // Work through SemiNaive example

    // "Logic Programming and Databases" (1989)
    // Foundation of Databases" (1995)
    // http://pages.cs.wisc.edu/~paris/cs838-s16/lecture-notes/lecture8.pdf (2016)
    // http://blogs.evergreen.edu/sosw/files/2014/04/Green-Vol5-DBS-017.pdf (2012)
    // Lecture 14. Semi-naive: May 20920.
    // Lecture 15: QSQ June 12, 2020

    // QSQ paper: Laurent Vieille. Recursive Axioms in Deductive Database: The Query-Subquery Approach.
    // In 1st International Conference on ExpertDatabase Systems, 1986

    /*
Algorithm 13.1.1 (Basic Seminaive Algorithm)
Input: Datalog program P and input instance I
Output: P (I)
    1. Set P′ to be the rules in P with no idb predicate in the body;
    2. S[0] := ∅, for each idb predicate S;

      S is the accumulator per rule.
      Delta is the generationally diff per rule.

    3. delta[1,S] := P′(I)(S), for each idb predicate S;
    4. i := 1;
    5. do begin
    for each idb predicate S, where T1 , . . . , Tl
                              are the idb predicates involved in rules defining S
      begin
      S[i] := S[i−1] ∪ delta[i,S] ;
      delta[i+1,S] := ??P[i,S]
         (I,
          T[i-1,1],... T[i-1,l],
          T[i,1],  ... T[i,l],
          delta[i,T1],  delta...[i,tl])
        -S[i]
      """Write P[i,S](...)
      to denote the set of tuples that result from applying the rules in P S i to given values for input
      instance I and for the T j i−1 , T j i , and . iT j .
      """
      end;
    i := i + 1
    end
    until . S[i] = ∅ for each idb predicate S.
    6. s := s[i] , for each idb predicate S.
 */

    //   Stratification

    /* IDB = rules
     * EDB = data
     * Intensional relation = appers in the head of a rule
     * Extensional relation = only in body
     */
    // abcdatalog
    // starta = distance away from EDB

    // https://github.com/juxt/crux

    // https://graphik-team.github.io/graal/
    // http://des.sourceforge.net/

    // https://www.cs.ox.ac.uk/files/1246/bry-rdflog-full.pdf
    // https://arxiv.org/abs/1906.10261 / https://arxiv.org/pdf/1906.10261.pdf

    // --- Infrastructure
    // GraphRDFS
    // GraphRules

    // Analysing rulesets
    // * Dependency graphs
    // * RuleSet.isRecursive ("containsRecursive")
    // * Execution without recursion - flattening and common subterms
    // *   mgu(rel1, rel2)
    // *   isRecursive, isLinear
    //       ??Need "mutually recursive". Not R -> S ->T -> S, : S/T is mutually recursive
    // *   DependencyGraph - better "matches"?
    // *   equivalence classes under recursion

    // * Rename variables apart Rel in two forms

    // If no recursion, can order the rules and evaluated in a building fashion.

    // "Solution" -> "Substitution" / "Substit"

    // * Treatment of RDF

    // Special treatment for (x y z) for RDF
    // "Same" if same predicate?
    // "Same" => consider the artiy and constants.
    // Recursion and dependency graph

    // * data(x,y,z) -- database only

    // Engines
    //   RDFS inf_rdfs
    //   Transitive inf_transitive
    //   (gone) inf2
    //   Rules
    //     Backwards
    //     Naive
    //     Semi-naive

    // * inf_rdfs : hardcoded RDFS - work on Nodes - finished
    // * inf_transitive - placeholder for an extract transitive rule reasoner component.
    // * inf2 - rules engine. No specials - even transitive is by rule.
    // * rules - parser, datastructure for rules.

    // BufferingGraph - no - need Find3

    // TermStore : BindingStore

    // (t1) (t2) <- (x1) (x2) (x3)
    // ==>
    //     (t1) (t2) <- project(?a,?b,?c) <- (x1) (x2) (x3)

    // General: Match, filter, calculate -> bindings -> pattern

    //     (t1) (t2) <- project(?a,?b,?c) <- (x1) (x2) (x3)

    //   project(?a,?b,?c) == collection of Bindings
}
