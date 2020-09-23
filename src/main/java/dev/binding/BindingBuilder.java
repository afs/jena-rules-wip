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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.FmtUtils;

// Parent vs copy.

/** Build Bindings
 * @see Binding
 */
public class BindingBuilder {
    // XXX Make proper builder!
    public static final Binding noParent = null;
    // factory like:

    /** Create a binding of no pairs */
    public static Binding binding() { return binding(noParent); }

    /** Create a binding of no pairs */
    public static Binding binding(Binding parent) { return new Binding0(parent) ; }

    public static Binding binding(Var var, Node node) { return binding(noParent, var, node); }

    /** Create a binding of one (var, value) pair */
    public static Binding binding(Binding parent, Var var, Node node)
    {
        if ( Var.isAnonVar(var) )
            return new Binding0(parent);
        return new Binding1(parent, var, node);
    }


    private static boolean CHECKING = true;
    private static final boolean UNIQUE_NAMES_CHECK = true;
    private Binding parent = noParent;
    private boolean haveBuilt = false;


    // Optimise up to 3 slots - these delay setting the Map so maps don't churn
    private Var  var1  = null;
    private Node node1 = null;

    private Var  var2  = null;
    private Node node2 = null;

    private Var  var3  = null;
    private Node node3 = null;

    private Var  var4  = null;
    private Node node4 = null;

    // More than 4
    private Map<Var,Node> map = null;

    private int countSlots() {
        if ( map != null ) return map.size();
        if ( var1 == null ) return 0;
        if ( var2 == null ) return 1;
        if ( var3 == null ) return 2;
        if ( var4 == null ) return 3;
        if ( map == null ) return 4;
        throw new InternalErrorException("Inconsistent internal state");
    }

    public BindingBuilder() { this(null, -1); }

    public BindingBuilder(Binding parent) {
        this(parent, -1);
    }

    public BindingBuilder(Binding parent, int size) {
        setParent(parent);
    }

    private void setParent(Binding parent) {
        if ( parent != null )
            throw new IllegalStateException("Parent already set");
        this.parent = parent;
    }

    public int size() {
        return countSlots();
    }

    // Accumulate (var,value) pairs.
    public void add(Var var, Node node) {
        checkAdd(var, node);
//        if ( Var.isAnonVar(var) )
//            return;
        if ( map != null ) {
            map.put(var, node);
            return;
        }

        if ( var1 == null ) {
            var1 = var;
            node1 = node;
            return;
        }
        if ( var2 == null ) {
            var2 = var;
            node2 = node;
            return;
        }
        if ( var3 == null ) {
            var3 = var;
            node3 = node;
            return;
        }
        if ( var4 == null ) {
            var4 = var;
            node4 = node;
            return;
        }
        // Spill from 4 to N.
        map = new HashMap<>();
        map.put(var1, node1);
        map.put(var2, node2);
        map.put(var3, node3);
        map.put(var4, node4);
    }

    public Node get(Var var) {
        if ( var == null )
            return null;
        if ( map != null )
            return map.get(var);
        if ( var.equals(var1) )
            return node1;
        if ( var.equals(var2) )
            return node2;
        if ( var.equals(var3) )
            return node3;
        if ( var.equals(var4) )
            return node4;
        return null;
    }

    public boolean contains(Var var) {
        Objects.requireNonNull(var);
        if ( map != null )
            return map.containsKey(var);
        if ( var.equals(var1) ) return true;
        if ( var.equals(var2) ) return true;
        if ( var.equals(var3) ) return true;
        if ( var.equals(var4) ) return true;
        return false;
    }

    private void checkAdd(Var var, Node node) {
        if ( ! CHECKING )
            return;
        if ( var == null )
            throw new InternalErrorException("check("+var+", "+node+"): null var" );
        if ( node == null )
            throw new InternalErrorException("check("+var+", "+node+"): null node value" );

//        if ( parent != null && UNIQUE_NAMES_CHECK && parent.contains(var) )
//            throw new ARQInternalErrorException("Attempt to reassign parent variable '"+var+
//                                                "' from '"+FmtUtils.stringForNode(get(var))+
//                                                "' to '"+FmtUtils.stringForNode(node)+"'");
        if ( UNIQUE_NAMES_CHECK && contains(var) )
            throw new ARQInternalErrorException("Attempt to reassign '"+var+
                                                "' from '"+FmtUtils.stringForNode(get(var))+
                                                "' to '"+FmtUtils.stringForNode(node)+"'");
    }

    /** Add all the (var, value) pairs from another binding */
    public void addAll(Binding other) {
        Iterator<Var> vIter = other.vars();
        for (; vIter.hasNext(); ) {
            Var v = vIter.next();
            Node n = other.get(v);
            add(v,n);
        }
    }

    // XXX ??? BindingBuilder.reset
    public void reset() {
        var1 = null;
        node1 = null;
        var2 = null;
        node2 = null;
        var3 = null;
        node3 = null;
        var4 = null;
        node4 = null;
    }

    // Use once.
    public Binding build() {
        if ( haveBuilt )
            throw new IllegalStateException();
        haveBuilt = true;
        if ( map != null )
            return new BindingOverMap(parent, map);
        if ( var1 != null )
            return new Binding1(parent, var1, node1);
        if ( var2 != null )
            return new Binding2(parent, var1, node1, var2, node2);
        if ( var3 != null )
            return new Binding3(parent, var1, node1, var2, node2, var3, node3);
        if ( var4 != null )
            return new Binding4(parent, var1, node1, var2, node2, var3, node3, var4, node4);
        throw new InternalErrorException("build: Inconsistent internal state");
//        int N = countSlots();
//        switch(N) {
//            case 0 : return new Binding0();
//            case 1 : return new Binding1(parent, var1, node1);
//            case 2 : return new Binding2(parent, var1, node1, var2, node2);
//            case 3 : return new Binding3(parent, var1, node1, var2, node2, var3, node3);
//            case 4 : return new Binding4(parent, var1, node1, var2, node2, var3, node3, var4, node4);
//            default:
//                map.put(var1, node1);
//                map.put(var2, node2);
//                map.put(var3, node3);
//                return new BindingOverMap(parent, map);
//        }
//        throw new InternalErrorException("build: Inconsistent internal state");
    }
}

