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

import java.util.Objects;

import migrate.binding.Binding;
import migrate.binding.BindingBuilder;
import migrate.binding.BindingFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.seaborne.jena.rules.Rel;

public class RelMap {
    // Return the mapping from rel1 to rel2
    public static Binding mapFromTo(Rel rel1, Rel rel2) {
        Objects.requireNonNull(rel1);
        Objects.requireNonNull(rel2);

        // Algorithm.
        // Produce a Binding to apply to a rule (head+body)
        // Assumes disjoint variables between [Relax by adding name-apart to the binding?]
        //    Consider rel1[i] and rel2[i]

        //    If rel1[i] is a constant c and rel2[i] is a variable ?v.
        //      then add ?v -> c
        //      i.e. replace ?v by c.

        //    If rel1[i] is a constant c and rel2[i] is a constant, check same.
        //      else return null (no binding).

        //      If rel1[i] is a variable ?var1 and rel2[i] is a constant,
        //         then rename variable (on return map ?var1 to constant.
        //         check this is not a contradiction.

        //      If rel1[i] is a variable and rel2[i] is a variable.
        //         then its equivalence group so check

        // r1(?a, :b) r2(?x, ?x) =>
        //    ?a maps ?x. ?x maps in c.
        // r1(:b, :c) r2(?x, ?x) => null

        // r1(?a, ?a, :a, :b) r2(?x, ?y, ?x ?y) => null

        // Finally, check equivalence groups

        if ( rel1.len() != rel2.len() )
            return null;
        if ( ! rel1.getName().equals(rel2.getName()) )
            return null;
        int N = rel1.len();
        BindingBuilder builder = BindingFactory.create();

        for ( int i = 0 ; i < N ; i++ ) {
            Node n1 = rel1.get(i);
            Node n2 = rel2.get(i);
            if ( n1.isConcrete() ) {
                if ( n2.isVariable() ) {
                    Var v2 = Var.alloc(n2);
                    if ( builder.contains(v2) ) {
                        Node n3 = builder.get(v2);
                        if ( ! n1.equals(n3) )
                            return null;
                    }
                    else
                        builder.add(v2, n1);
                } else {
                    //n1 constant, n2 constant
                    if ( ! n1.equals(n2) )
                        return null;
                }
            } else {
//
//                // This is the output direction.
//                // n1 variable.
//                Var v1 = Var.alloc(n1);
//                if ( n2.isConcrete() ) {
//                    // n1 variable, n2 constant
//                    if ( builder.contains(v1) ) {
//                        Node n3 = builder.get(v1);
//                        if ( ! n3.equals(n2) )
//                            return null;
//                    }
//                    else
//                        builder.add(v1, n2);
//                } else {
//                    //n1 var, n2 varm-- not needed in the forward direction.
//                }
            }
        }

        return builder.build();
    }
}

