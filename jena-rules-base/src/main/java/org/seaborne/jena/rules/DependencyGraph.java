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

import java.util.*;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap;
import org.apache.jena.ext.com.google.common.collect.Multimap;
import org.apache.jena.graph.Node;

/** Rules dependency graph */ 
public class DependencyGraph {
    private Multimap<Rel, Rel> direct = ArrayListMultimap.create();
    private Multimap<Rel, Rule> rules = ArrayListMultimap.create();
    
    public void put(Rule rule) {
        Rel head = rule.getHead();
        if ( direct.containsKey(head) )
            direct.removeAll(head);
        List<Rel> body = rule.getBody();
        body.forEach(r->direct.put(head,r));
        rules.put(head, rule);
    }
    
    public boolean isRecursive(Rule rule) {
        Deque<Rule> stack = new ArrayDeque<>();
        boolean b = isRecursive(rule, stack);
        if ( b ) {
            stack.stream().map(r->r.getHead()).forEach(h->System.out.printf("--%s", h));
            System.out.println();
            System.out.println(stack);
        }
        return b;
    }

    private boolean isRecursive(Rule rule, Deque<Rule> visited) {
        if ( visited.contains(rule) )
            return true;
        visited.push(rule);
        boolean b = isRecursive2(rule, visited) ;
        if ( b )
            return b;
        visited.pop();
        return false;
    }
    
    private boolean isRecursive2(Rule rule, Deque<Rule> visited) {
        Rel head = rule.getHead();
        List<Rel> body = rule.getBody();
        for ( Rel bodyRel : body ) {
            Collection<Rule> others = matches(bodyRel, rules);
            for ( Rule otherRule : others ) {
                if ( isRecursive(otherRule, visited) )
                    return true;
            }
        }
        return false;
    }

    public static Collection<Rule> matches(Rel rel, Multimap<Rel, Rule> rules) {
        List<Rule> array = new ArrayList<>();
        rules.forEach((head,body)->{
            if ( provides(rel, head) )
                array.add(body);
        });
        return array; 
    }
    
    // Need mapping version
    /** Does {@code target} help to solver.<br/>
     * <tt>target &lt;- src</tt>
     * @param target
     * @param src
     * @return boolean
     */
    
    public static boolean provides(Rel target, Rel src) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(src);
        if ( ! target.getName().equals(src.getName()) )
            return false;
        Tuple<Node> tRel = target.getTuple();
        Tuple<Node> tR = src.getTuple();
        if ( tRel.len() != tR.len() )
            return false;
        for ( int i = 0 ; i < tRel.len() ; i++ ) {
            Node right = tRel.get(i);
            Node left = tR.get(i);
            if ( ! provides(right, left) )
                return false;
        }
        return true;
    }

    // Does not consider variable names.
    private static boolean provides(Node right, Node left) {
        if ( right.isVariable() || left.isVariable() )
            return true;
        // Ground terms match.
        return right.equals(left) ;
    }
}

