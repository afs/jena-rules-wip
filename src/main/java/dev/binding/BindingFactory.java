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

package dev.binding;

import static dev.binding.Binding.noParent;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class BindingFactory
{

    public static BindingBuilder create() { return new BindingBuilder(); }

    /** Create a binding of no pairs */
    public static Binding binding() { return binding(noParent); }

    /** Create a binding of no pairs */
    public static Binding binding(Binding parent) {
        return new Binding0(parent);
    }

    /** Create a binding of one pair */
    public static Binding binding(Var var, Node node) {
        return binding(noParent, var, node);
    }

    /** Create a binding of two pairs */
    public static Binding binding(Var var1, Node node1, Var var2, Node node2) {
        return binding(noParent, var1, node1, var2, node2);
    }

    /** Create a binding of three pairs */
    public static Binding binding(Var var1, Node node1, Var var2, Node node2, Var var3, Node node3) {
        return binding(noParent, var1, node1, var2, node2, var3, node3);
    }

    /** Create a binding of fours pairs */
    public static Binding binding(Var var1, Node node1, Var var2, Node node2, Var var3, Node node3, Var var4, Node node4) {
        return binding(noParent, var1, node1, var2, node2, var3, node3, var4, node4);
    }

    /** Create a binding of one (var, value) pair */
    public static Binding binding(Binding parent, Var var, Node node) {
        return new Binding1(parent, var, node);
    }

    /** Create a binding of two (var, value) pairs */
    public static Binding binding(Binding parent, Var var1, Node node1, Var var2, Node node2) {
        return new Binding2(parent, var1, node1, var2, node2);
    }

    /** Create a binding of three (var, value) pairs */
    public static Binding binding(Binding parent, Var var1, Node node1, Var var2, Node node2, Var var3, Node node3) {
        return new Binding3(parent, var1, node1, var2, node2, var3, node3);
    }

    /** Create a binding of four (var, value) pairs */
    public static Binding binding(Binding parent, Var var1, Node node1, Var var2, Node node2, Var var3, Node node3, Var var4, Node node4) {
        return new Binding4(parent, var1, node1, var2, node2, var3, node3, var4, node4);
    }


    /** Create a root binding */
    public static Binding root() {
        return BindingRoot.create();
    }

    /**
     * Create a new {@link Binding} as a copy of an existing one.
     * Additionally, it guarantees to touch each element of the binding.
     */
    public static Binding materialize(Binding b) {
        Iterator<Var> vIter = b.vars();
        BindingBuilder builder = new BindingBuilder();
        builder.addAll(b);
        return builder.build();
    }
}
