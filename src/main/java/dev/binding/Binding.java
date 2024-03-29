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

import java.util.Iterator;
import java.util.function.BiConsumer;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/** Interface encapsulating a mapping from a name to a value. */

public interface Binding //extends Iterable<Var>
{
    public static final Binding noParent = null;

    /** Iterate over all variables of this binding. */
    public Iterator<Var> vars();

    /** Operate on each entry. */
    public void forEach(BiConsumer<Var, Node> action);

    /** Test whether a variable is bound to some object */
    public boolean contains(Var var);

    /** Return the object bound to a variable, or null */
    public Node get(Var var);

    /** Number of (var, value) pairs. */
    public int size();

    /** Is this an empty binding?  No variables. */
    public boolean isEmpty();

}
