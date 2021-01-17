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

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.atlas.lib.CollectionUtils;
import org.apache.jena.atlas.lib.StreamOps;
import org.seaborne.jena.rules.exec.Renamer;
import org.seaborne.jena.rules.exec.RuleOps;

public class RuleSet implements Iterable<Rule>{
    private final List<Rule> rules ;
    private final List<Rule> execRules;

    public static class Builder {
        private final List<Rule> accRules =  new ArrayList<>() ;

        public Builder() {}

        public Builder add(Rule rule) {
            accRules.add(rule);
            return this;
        }

        public Builder add(Rule... rules) {
            for ( Rule r : rules)
                accRules.add(r);
            return this;
        }

        public Builder add(Collection<Rule> rules) {
            rules.forEach(this::add);
            return this;
        }

        public RuleSet build() {
            List<Rule> rules = new ArrayList<>(this.accRules);
            return new RuleSet(rules, Renamer.rename("v", rules));
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static RuleSet create(Rule ... rules) {
        return new Builder().add(rules).build();
    }
//
//    private RuleSet(Collection<Rule> rules) {
//        this(rules, Renamer.rename("v", rules));
//    }

    private RuleSet(List<Rule> rules, List<Rule> execRules) {
        this.rules = rules;
        this.execRules = execRules;
    }

    /**
     * Return a list of rules where the variables are unique across rules.
     * This is required for some algorithms.
     * This list aligns with the return from {@link #rules()} method.
     */
    public List<Rule> execRules() {
        return execRules;
    }

    public List<Rule> rules() {
        return rules;
    }

    public int size() {
        return rules.size();
    }

    @Override
    public Iterator<Rule> iterator() {
        return rules.iterator();
    }

    public Stream<Rule> stream() {
        return rules.stream();
    }

    /** Get a rule that exactly matches the {@code Rel} (same variable names). */
    public Rule get(Rel rel) {
        return stream().filter(r->rel.equals(r.getHead())).findAny().orElse(null);
    }

    /** Get all the rules that exactly match the {@code Rel} (same variable names). */
    public Collection<Rule> getAll(Rel rel) {
        return stream().filter(r->rel.equals(r.getHead())).collect(Collectors.toList());
    }

    /** Get the rule that exactly matches the {@code Rel} (same variable names) or throw a {@link RulesException}. */
    public Rule getOne(Rel rel) {
        Collection<Rule> c = getAll(rel);
        if ( c.size() != 1 )
            throw new RulesException("Not exactly one match: "+rel+" : got "+c);
        return CollectionUtils.oneElt(c);
    }

    public Collection<Rule> provides(Rel rel) {
        Stream<Rule> providers = stream().filter(rule -> RuleOps.provides(rel, rule.getHead()));
        return StreamOps.toList(providers);
    }

    public List<Rel> getHeads() {
        return stream().map(r -> r.getHead()).collect(toList()) ;
    }

    public List<String> getHeadNames() {
        return stream().map(r -> r.getHead().getName()).collect(toList()) ;
    }

//    /** Intensional rules = occurs in the head of a rule.
//     * Returns a set of Triples with "canonical relations": ANY for variables.
//     */
//    public Set<Rel> getIntensional() {
//        return RuleOps.intensionalRelations(rules) ;
//    }
//
//    /** Extensional relationship = relation occurring only in the body of rules.*/
//    public Collection<Rel> getExtensional() {
//        return RuleOps.extensionalRelations(rules) ;
//    }

//    /** Rules that has concrete predicate that is also in some rule body.
//     * These rules may trigger other rules.
//     */
//    public Collection<Node> loops() {
//        Set<Node> headReln = stream().map(r->r.getHead().getPredicate()).filter(n->n.isConcrete()).collect(toSet()) ;
//        Stream<Node> bodyReln = stream().flatMap(r->r.getBody().stream()).map(t->t.getPredicate());
//        return bodyReln.filter(headReln::contains).collect(toSet()) ;
//    }

    //public void forEach(Consumer<? super Rule> action) { rules.forEach(action); }

    public void print() {
        print(System.out);
    }

    private void print(PrintStream out) {
        System.out.println("[RuleSet]");
        rules.stream().map(Rule::toString).forEach(r->System.out.println("  "+r));
    }

    public String toMultilineString() {
        StringJoiner sj = new StringJoiner(" .\n", "", " .\n") ;
        rules.stream().map(Rule::toString).forEach(sj::add);
        return sj.toString();
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" . ", "", " .") ;
        rules.stream().map(Rule::toString).forEach(sj::add);
        return sj.toString();
    }

//    // Convert a rule to use "any", not named variables.
//    private Rule unvar(Rule r) {
//        Rel h2 = RuleOps.unvar(r.getHead());
//        List<Rel> b2 = r.getBody().stream().map(RuleOps::unvar).collect(Collectors.toList());
//        return new Rule(h2, b2);
//    }
}
