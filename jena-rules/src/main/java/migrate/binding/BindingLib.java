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

package migrate.binding;

import java.util.Iterator;
import java.util.Objects;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

/** Operations on Bindings */
public class BindingLib {

    /** test for equality - order independent (Bindings are map-like) */
    public static boolean equals(Binding bind1, Binding bind2) {
        if ( bind1 == bind2 )
            return true;
        if ( bind1.size() != bind2.size() )
            return false;
        for ( Iterator<Var> iter1 = bind1.vars() ; iter1.hasNext() ; ) {
            Var var = iter1.next();
            Node node1 = bind1.get(var);
            Node node2 = bind2.get(var);
            if ( !Objects.equals(node1, node2) )
                return false;
        }

        // No need to check the other way round as the sizes matched.
        return true;
    }

    /** Merge two bindings, assuming they are compatible. */
    public static Binding merge(Binding bind1, Binding bind2) {
        // Create binding from LHS
        BindingBuilder builder = new BindingBuilder(bind1, bind2.size());
        Iterator<Var> vIter = bind2.vars();
        // Add any variables from the RHS
        for ( ; vIter.hasNext() ; ) {
            Var v = vIter.next();
            if ( !builder.contains(v) )
                builder.add(v, bind2.get(v));
            else {
                // Checking!
                Node n1 = bind1.get(v);
                Node n2 = bind2.get(v);
                if ( !n1.equals(n2) )
                    Log.warn(BindingLib.class, "merge: Mismatch : " + n1 + " != " + n2);
            }
        }
        return builder.build();
    }
}
