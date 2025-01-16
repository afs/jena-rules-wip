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

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.seaborne.jena.rules.exec.RuleOps;

/**
 * Rules dependency graph. The graph has vertices of rules and links being "depends
 * on" another rule, i.e. for a node that is a head of a rule, it is linked to the
 * relations in its body. from this, we can determine whether a rule is:
 * <ul>
 * <li>Data only (body contains relations that only appear in the data)</li>
 * <li>Not-recursive: it can be solved by top-down flattening</li>
 * <li>Mutually recursive rules</li>
 * <li>linear (only one body relationship is recursive)</li>
 * </ul>
 */
public class DependencyGraph {
    // Rule -> other rules it needs
    // This is not the body - a Rel in the body may have more than one rule it depends on

    private MultiValuedMap<Rule, Rule> direct = MultiMapUtils.newListValuedHashMap();
    // Rule -> predicates without rules (so must be in the data)

    private MultiValuedMap<Rule, Rel> data = MultiMapUtils.newListValuedHashMap();
//    private MultiValuedMap<Rel, Rule> rules = MultiMapUtils.newListValuedHashMap();
    private final RuleSet ruleSet;

    public DependencyGraph(RuleSet ruleSet) {
        this.ruleSet = ruleSet;
        ruleSet.forEach(this::put);
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    private boolean DEBUG = false;

    private void put(Rule rule) {
        if ( DEBUG )
            System.out.println("put:"+rule);
        List<Rel> body = rule.getBody();
        body.forEach(bodyRel->{
            Collection<Rule> c = RuleOps.provides(bodyRel, ruleSet);
            if ( c.isEmpty() ) {
                if ( DEBUG )
                    System.out.println("    data:"+bodyRel);
                data.put(rule, bodyRel);
            } else {
                if ( DEBUG )
                    System.out.println("    "+c);
                direct.putAll(rule, c);
            }
        });
    }

    public void walk(Rule rule, Consumer<Rule> action) {
        walk$(rule, action);
    }

    // Use the direct set.

    private void walk$(Rule rule, Consumer<Rule> action) {
        Set<Rule> acc = new HashSet<>();
        Deque<Rule> stack = new ArrayDeque<>();
        walk$(rule, action, acc, stack);
    }

    private void walk$(Rule rule, Consumer<Rule> action, Set<Rule> visited, Deque<Rule> pathVisited) {
        if ( visited.contains(rule) )
            return;
        visited.add(rule);
        action.accept(rule);
        pathVisited.push(rule);
        walkStep(rule, action, visited, pathVisited);
        pathVisited.pop();
    }

    private void walkStep(Rule rule, Consumer<Rule> action, Set<Rule> visited, Deque<Rule> pathVisited) {
        Collection<Rule> others = direct.get(rule);
        for ( Rule otherRule : others ) {
            walk$(otherRule, action, visited, pathVisited);
        }
    }

    // Recursion test. Like walk but with early exit.
    // Can terminate early if all we want is whether it is/is not recursive.
    public boolean isRecursive(Rule rule) {
        Deque<Rule> stack = new ArrayDeque<>();
        boolean b = isRecursive(rule, rule, stack);
        if ( b ) {
            if ( DEBUG ) {
                stack.stream().map(r->r.getHead()).forEach(h->System.out.printf("--%s", h));
                System.out.println();
                System.out.println(stack);
            }
        }
        return b;
    }

    private boolean isRecursive(Rule topRule, Rule rule, Deque<Rule> visited) {
        if ( DEBUG )
            System.out.printf("isRecursive(\n  %s,\n  %s,\n  %s)\n", topRule, rule, visited);
        if ( ! visited.isEmpty() && topRule.equals(rule))
            return true;
        if ( visited.contains(rule) )
            // Other cycle.
            return false;
        visited.push(rule);
        boolean b = isRecursive2(topRule, rule, visited) ;
        if ( b )
            return b;
        visited.pop();
        return false;
    }

    private boolean isRecursive2(Rule topRule, Rule rule, Deque<Rule> visited) {
        Rel head = rule.getHead();
        List<Rel> body = rule.getBody();
        for ( Rel bodyRel : body ) {
            // Not the dependencyGraph because this considers "provides", not "matches".
            Collection<Rule> others = ruleSet.provides(bodyRel);
            for ( Rule otherRule : others ) {
                if ( isRecursive(topRule, otherRule, visited) )
                    return true;
            }
        }
        return false;
    }


//    // Write a walk with termination criterion.
//
//    // --- Equivalence class
//
//    /** Return the equivalence class for a rule (all the rules in any recursion path) */
//    public Set<Rule> recursionEquivalence(Rule rule) {
//        Set<Rule> acc = new HashSet<>();
//        Deque<Rule> stack = new ArrayDeque<>();
//        isRecursiveEquiv(rule, stack, acc);
//        return acc;
//    }
//
//    private boolean isRecursiveEquiv(Rule rule, Deque<Rule> visited, Set<Rule> acc) {
//        if ( visited.contains(rule) )
//            return true;
//        visited.push(rule);
//        boolean b = isRecursiveEquiv2(rule, visited, acc);
//        if ( b )
//            acc.addAll(visited);
//        visited.pop();
//        return b;
//    }
//
//    private boolean isRecursiveEquiv2(Rule rule, Deque<Rule> visited, Set<Rule> acc) {
//        Rel head = rule.getHead();
//        List<Rel> body = rule.getBody();
//        boolean b = false;
//        for ( Rel otherRule : body ) {
//            Collection<Rule> others = matches(otherRule.getHead(), ruleSet);
//            boolean b1 = isRecursiveEquiv(otherRule, visited, acc);
//            b = b | b1;
//        }
//        return b;
//    }
//
//    // Recursion test. Do we circle back to the input?
//    // Can terminate early if all we want is whether it is/is not recursive.
//    public boolean isRecursive(Rule rule) {
//        Deque<Rule> stack = new ArrayDeque<>();
//        boolean b = isRecursive(rule, stack);
//        if ( b ) {
//            stack.stream().map(r->r.getHead()).forEach(h->System.out.printf("--%s", h));
//            System.out.println();
//            System.out.println(stack);
//        }
//        return b;
//    }
//
//    private boolean isRecursive(Rule rule, Deque<Rule> visited) {
//        if ( visited.contains(rule) )
//            return true;
//        visited.push(rule);
//        boolean b = isRecursive2(rule, visited) ;
//        if ( b )
//            return b;
//        visited.pop();
//        return false;
//    }
//
//    private boolean isRecursive2(Rule rule, Deque<Rule> visited) {
//        Rel head = rule.getHead();
//        List<Rel> body = rule.getBody();
//        for ( Rel bodyRel : body ) {
//            Collection<Rule> others = matches(bodyRel, rules);
//            for ( Rule otherRule : others ) {
//                if ( isRecursive(otherRule, visited) )
//                    return true;
//            }
//        }
//        return false;
//    }

//    /** Return the relationships (rels) that match the given relationship. */
//    public static Collection<Rule> matches(Rel rel, Multimap<Rel, Rule> rules) {
//        List<Rule> array = new ArrayList<>();
//        rules.forEach((head,body)->{
//            if ( provides(rel, head) )
//                array.add(body);
//        });
//        return array;
//    }

//    /** Return the rules that match the given relationship. */
//    private static Collection<Rule> matches(Rule rule, RuleSet ruleSet) {
//        List<Rule> array = new ArrayList<>();
//
//        for ( Rel bodyRel : rule.getBody() ) {
//            // find all rules in the RuleSet that provide the rule
//            for ( Rule r : ruleSet ) {
//                if ( provides(bodyRel, r.getHead()) ) {
//                    array.add(r);
//                }
//            }
//        }
//        return array;
//    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        System.out.println("[DependencyGraph]");
        for ( Rule r : direct.keySet() ) {
            out.println(r);
            Collection<Rule> c = direct.get(r);
            c.forEach(rr -> {
                out.print("  ");
                out.print(rr);
                out.println();
            });
        }
    }
}

