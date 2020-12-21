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

package org.seaborne.jena.rules;

import static java.util.stream.Collectors.toList;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RuleSet implements Iterable<Rule>{
    private final List<Rule> rules ;
    private final List<Rule> unvarRules;

    public RuleSet(List<Rule> rules) {
        this.rules = Collections.unmodifiableList(new ArrayList<>(rules));
        this.unvarRules = Collections.unmodifiableList
            (rules.stream().sequential()
             .map(r -> unvar(r))
             .collect(Collectors.toList()) );
    }

    public List<Rule> asList() {
        return rules;
    }

    @Override
    public Iterator<Rule> iterator() {
        return rules.iterator();
    }

    public Stream<Rule> stream() {
        return rules.stream();
    }

    public List<Rel> getHeads() {
        return stream().map(r -> r.getHead()).collect(toList()) ;
    }

    public List<String> getHeadNames() {
        return stream().map(r -> r.getHead().getName()).collect(toList()) ;
    }

    /** Intensional rules = occurs in the head of a rule.
     * Returns a set of Triples with "canonical relations": ANY for variables.
     */
    public Set<Rel> getIntensional() {
        //Re-unvars
        return Rules.intensionalRelations(rules) ;
    }

    /** Extensional relationship = relation occurring only in the body of rules.*/
    public Collection<Rel> getExtensional() {
        //Re-unvars
        return Rules.extensionalRelations(rules) ;
    }

//    /** Rules that has concrete predicate that is also in some rule body.
//     * These rules may trigger other rules.
//     */
//    public Collection<Node> loops() {
//        Set<Node> headReln = stream().map(r->r.getHead().getPredicate()).filter(n->n.isConcrete()).collect(toSet()) ;
//        Stream<Node> bodyReln = stream().flatMap(r->r.getBody().stream()).map(t->t.getPredicate());
//        return bodyReln.filter(headReln::contains).collect(toSet()) ;
//    }
//
//    //NEEDS REWRITE
//
//    /** for each head predicate, the rule path of the loop (this is not equivalence classes) */
//    public Map<Node, List<List<Rule>>> loopsChains() {
//        // Inefficient.
//        Collection<Node> loops = loops();
//        Map<Node, List<List<Rule>>> results = new HashMap<>();
//        loops.forEach(n->{
//            Set<Node> visited = new HashSet<>();
//            List<List<Rule>> chain = followChain(visited, n);
//            results.put(n, chain);
//        }) ;
//        return results;
//    }
//
//    private List<List<Rule>> followChain(Set<Node> visited, Node predicate) {
//        //System.err.println("followChain p="+SSE.str(predicate)) ;
//        List<Rule> heads = rules.stream().filter(r-> predicate.equals(r.getHead().getPredicate()) ).collect(toList());
//        System.err.println("heads="+heads) ;
//        List<List<Rule>> chains = new ArrayList<>();
//        chains.add(heads);
//        heads.forEach(r->{
//            List<List<Rule>> chainsForRule = followChain(visited, r);
//            chains.addAll(chainsForRule);
//        }) ;
//        return chains;
//    }
//
//    private List<List<Rule>> followChain(Set<Node> visited, Rule rule) {
//        //System.err.println("followChain r="+rule);
//        // May branch!
//        List<List<Rule>> chains = new ArrayList<>();
//        for(Rel t: rule.getBody()) {
//            Node p = t.getPredicate();
//            if ( visited.contains(p) )
//                continue;
//            visited.add(p);
//            List<List<Rule>> x = followChain(visited, p) ;
//            if ( x.size() > 1 )
//                // Clone and branch.s
//                System.err.println("Warning branching chain: "+t);
//            chains.addAll(x);
//        }
//        return chains ;
//    }

    public Collection<Rule> getAsUnvar() {
        return unvarRules;
    }

    //public void forEach(Consumer<? super Rule> action) { rules.forEach(action); }

    public String toMultilineString() {
        StringJoiner sj = new StringJoiner("\n") ;
        rules.stream().map(Rule::toString).forEach(sj::add);
        return sj.toString();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n") ;
        rules.stream().map(Rule::toString).forEach(sj::add);
        return sj.toString();
    }

    // Convert a rule to use "any", not named variables.
    private Rule unvar(Rule r) {
        Rel h2 = Rules.unvar(r.getHead());
        List<Rel> b2 = r.getBody().stream().map(Rules::unvar).collect(Collectors.toList());
        return new Rule(h2, b2);
    }
}
