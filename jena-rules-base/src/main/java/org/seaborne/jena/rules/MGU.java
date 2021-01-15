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

import static org.apache.jena.sparql.core.Var.isVar;

import java.util.Objects;

import migrate.binding.Binding;
import migrate.binding.BindingBuilder;
import migrate.binding.BindingFactory;
import migrate.binding.Sub;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class MGU {

    /*
     * "Logic Programming and Databases" p85 "FUNCTION MGU" algorithm is actually buggy.
     *
     * Order matters if there are two uses of a variable.
     *   mgu("(?x, :p, ?x)", "(:z, :p, ?z)") => "[(?x :z) (?z :z) ]"
     *   mgu("(?x, :p, ?x)", "(?z, :p, :z)") => "[(?z ?x) (?x :z)]"
     *
     * Fixup A: Post loop fixup. if >z -> >?x, check ?x.
     * Fixup B: When adding (var, var)
     * Fixup C: Two loop; constant loop first, then variables.
     *   Fixup D in fixup C.
     *
     * "Foundations of databases" p294
     *  defines that theta(z) = constant has precedence.
     *  Our fixed ordering of variables is "in the first"
     * -----------
     * "Logic Programming and Databases"
     *
     *    L = p(t1,t2, ..., tn) M = p(s1, s2, ..., sm)
     *    MGU(L, M)
     *
     * if name or arity different -> null
     * theta = {} -- function
     * i = 1
     * unifies = true
     * REPEAT
     *   if theta(ti) != theta(si)
     *   then
     *     if theta(si) is a variable
     *     then
     *       theta = theta and { theta(si) -> theta(ti) }
     *     else if theta(ti) is a variable
     *       theta = theta and { theta(ti) -> theta(si) }
     *     else
     *       unifies == false
     *     i++
     *     until i>n OR not unifies
     *  return unifier ? theta : null
     */

    /**
     * Most General Unifier.
     * Assumes variables on left and right are disjoint.
     * A variable on the left will only map to a constant.
     */
    public static Binding mgu(Rel rel1, Rel rel2) {
        return mgu_C(rel1, rel2);
    }

    public static Binding mgu_C(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);
        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();
        BindingBuilder map = BindingFactory.create();
        // Constants
        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            if ( Var.isVar(n1) && Var.isVar(n2) )
                continue;
            // One or both are constants.
            Node x1 = mguMap(map, n1);
            Node x2 = mguMap(map, n2);
            if ( x1.equals(x2) )
                continue;
            if ( isVar(x2) ) {
                // x1 is not a variable
                // mguAdd not needed?
                boolean isOK = mguAdd(map, Var.alloc(x2), x1);
                if ( !isOK )
                    return null;
                continue;
            }
            if ( isVar(x1) ) {
                // x2 is not a variable
                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
                if ( !isOK )
                    return null;
                continue;
            }
            // Incompatible.
            return null;
        }
        // Variable pairs.
        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            if ( ! Var.isVar(n1) || ! Var.isVar(n2) )
                continue;
            // Both n1 and n2 are variables.
            Node x1 = mguMap(map, n1);
            Node x2 = mguMap(map, n2);
            // x1 and/or x2 may be constants
            if ( x1.equals(x2) )
                continue;

//             //Fixup D.
//            // Still both variables?
//            if ( Var.isVar(x1) && Var.isVar(x2) ) {
//                // mguAdd not needed?
//                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
//                if ( !isOK )
//                    return null;
//                continue;
//            }

            // Order matters here? Or both mappings?
            if ( isVar(x2) ) {
                // mguAdd not needed?
                boolean isOK = mguAdd(map, Var.alloc(x2), x1);
                if ( !isOK )
                    return null;
                continue;
            }
            if ( isVar(x1) ) {
                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
                if ( !isOK )
                    return null;
                continue;
            }
            // Incompatible.
            return null;

        }
        return map.build();
    }

    public static Binding mgu_B(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);
        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();
        BindingBuilder map = BindingFactory.create();
        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            Node x1 = mguMap(map, n1);
            Node x2 = mguMap(map, n2);
            if ( x1.equals(x2) )
                continue;

            if ( isVar(x2) ) {
                // Fixup B
                if ( Var.isVar(x1) ) {
                    Var v1 = Var.alloc(x1);
                    if ( map.contains(v1) ) {
                        // Follow x1/v1 mapping.
                        Node x3 = map.get(v1);
                        //if ( ! Var.isVar(x3) )
                            x1 = x3;
                    }
                }

                boolean isOK = mguAdd(map, Var.alloc(x2), x1);
                if ( !isOK )
                    return null;

                continue;
            }
            if ( isVar(x1) ) {
                // Fixup B
                // Impossible because x2 is a var was handled above.
                if ( Var.isVar(x2) ) {
                    Var v2 = Var.alloc(x2);
                    if ( map.contains(v2) ) {
                        Node x3 = map.get(v2);
                        //if ( ! Var.isVar(x3) )
                        x2 = x3;
                    }
                }
                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
                if ( !isOK )
                    return null;
                continue;
            }
            return null;
        }

        // Fixup.
        map.vars().forEachRemaining(v->{
            Node n = map.get(v);
            if ( Var.isVar(n) ) {
                Node n2 = map.get(Var.alloc(n));
                if ( n2 != null && n2.isConcrete() )
                    map.set(v, n2);
            }
        });

        return map.build();
    }

    public static Binding mgu_A(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);
        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();
        BindingBuilder map = BindingFactory.create();
        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            Node x1 = mguMap(map, n1);
            Node x2 = mguMap(map, n2);
            if ( x1.equals(x2) )
                continue;

            if ( isVar(x2) ) {
                boolean isOK = mguAdd(map, Var.alloc(x2), x1);
                if ( !isOK )
                    return null;

                continue;
            }
            if ( isVar(x1) ) {
                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
                if ( !isOK )
                    return null;
                continue;
            }
            return null;
        }

        // Fixup A.
        map.vars().forEachRemaining(v->{
            Node n = map.get(v);
            if ( Var.isVar(n) ) {
                Node n2 = map.get(Var.alloc(n));
                if ( n2 != null && n2.isConcrete() )
                    map.set(v, n2);
            }
        });

        return map.build();
    }

    // No fixups.
    public static Binding mgu_0(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);
        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();
        BindingBuilder map = BindingFactory.create();
        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            Node x1 = mguMap(map, n1);
            Node x2 = mguMap(map, n2);
            if ( x1.equals(x2) )
                continue;

            if ( isVar(x2) ) {
                boolean isOK = mguAdd(map, Var.alloc(x2), x1);
                if ( !isOK )
                    return null;

                continue;
            }
            if ( isVar(x1) ) {
                boolean isOK = mguAdd(map, Var.alloc(x1), x2);
                if ( !isOK )
                    return null;
                continue;
            }
            return null;
        }
        return map.build();
    }

    /** Apply */
    public static Rel applyMGU(Binding binding, Rel rel) {
        return Sub.substitute(binding, rel);
    }

    /** Map, or return the original. */
    private static Node mguMap(BindingBuilder map, Node node) {
        if ( ! Var.isVar(node) )
            return node;
        Var var = Var.alloc(node);
        Node x = map.get(var);
        return x == null ? var : x ;
    }

    /**
     * Set, returning true if successful.
     * Return false for already and incompatibly set.
     */
    private static boolean mguAdd(BindingBuilder map, Var var, Node node) {
        Node n = map.get(var);
        if ( n == null ) {
            map.add(var, node);
            return true;
        }
        return n.equals(node);
    }

    // ---
    /**
     * @implSpec
     * Taken from: https://www.cs.ubc.ca/~poole/aibook/html/ArtInt_287.html
     * <pre>
        select and remove x=y from E
            if (y is not identical to x) then
                if (x is a variable) then
                    replace x with y everywhere in E and S
                    S←{x/y}∪S
                else if (y is a variable) then
                    replace y with x everywhere in E and S
                    S←{y/x}∪S
                else if (x is f(x1 ,...,xn) and y is f(y1 ,...,yn) then
                    E←E∪{x1 = y1 ,..., xn =yn }
                else
                    return ⊥
     * </pre>
     *
     */

    // Returns the left-to-right mapping biased MGU.
    // i.e. var-var mappings are from right var to left var
    // i.e. output is in LHS namespace.
    public static Binding mguAlg2(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);
        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();

        BindingBuilder map = BindingFactory.create();

        // The first step. E is these two Node[] and this does the first loop iteration.
        Node[] t1 = new Node[N];
        rel1.getTuple().copyInto(t1);
        Node[] t2 = new Node[N];
        rel2.getTuple().copyInto(t2);

        // For E, we use t1 and t2 already expanded.
        for ( int i = 0 ; i < N ; i++ ) {
            Node x = t1[i];
            Node y = t2[i];
            // Already replaced? We did a forward sweep.
            // Else could(?):
//            if ( Var.isVar(x) )
//                x = map.getOrSame(Var.alloc(x));
//            if ( Var.isVar(y) )
//                y = map.getOrSame(Var.alloc(y));
            // which does not need the tuple copy.
            // Experiment.
            if ( x.equals(y) )
                continue;
            if ( Var.isVar(x) ) {
                //replace x with y everywhere in E and S
                // ???? y first?
                replace(t1,i,x,y);
                replace(t2,i,x,y);
                //S←{x/y}∪S
                // ** Do we need to check the range of the map for x?
                // What about 2 pass?
                map.set(Var.alloc(x), y);
                continue;
            }
            if ( Var.isVar(y) ) {
                //replace y with x everywhere in E and S
                // In E
                replace(t1,i,y,x);
                replace(t2,i,y,x);
                //S←{y/x}∪S
                // ** Do we need to check the range of the map for y?
                map.set(Var.alloc(y), x);
                //map.replace
                continue;
            }
            return null;
        }
        return map.build();
    }

    private static void replace(Node[] t, int from, Node x, Node y) {
        for ( int i = from ; i < t.length ; i++ ) {
            Node n = t[i];
            if ( n.equals(x) )
                t[i] = y;
        }
    }

}
