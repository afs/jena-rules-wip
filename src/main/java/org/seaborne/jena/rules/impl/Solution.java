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

package org.seaborne.jena.rules.impl;

import java.util.HashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;


public class Solution extends HashMap<Var, Node>{
    // XXX Needs work for efficiency.
    public Solution() { super();}
    
    public Solution(Solution solution) { super(solution);}
    
//    Map<Var, Entry> map = new HashMap<>();
//    public class Entry {
//        public Var var;
//        public Node value;
//    }
//
//    /** Test whether a variable is bound to some object */
//    public boolean contains(Var var) ;
//
//    /** Return the object bound to a variable, or null */
//    public Node get(Var var) ;
//    
//    /** Number of (var, value) pairs. */
//    public int size() ;
//
//    /** Is this an empty binding?  No variables. */
//    public boolean isEmpty() ;
}

