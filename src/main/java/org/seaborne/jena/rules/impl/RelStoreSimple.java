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

import java.util.*;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple ;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap ;
import org.apache.jena.ext.com.google.common.collect.Multimap ;
import org.apache.jena.graph.Node ;
import org.seaborne.jena.rules.Rel ;
import org.seaborne.jena.rules.RelStore ;

/** Very simple {@link RelStore}.
 * Useful as an independent implementation for testing. 
 */
public class RelStoreSimple implements RelStore {
    private Multimap<String,Rel> store;
    
    public RelStoreSimple() {
        store = ArrayListMultimap.create();
    }
    
    @Override
    public void add(Rel rel) {
        store.put(rel.getName(), rel);
    }
    
    @Override
    public void add(RelStore data) {
        data.all().forEach(this::add);
    }

    @Override
    public void delete(Rel rel) {
        store.remove(rel.getName(), rel);
    }
    
    @Override
    public void removeAll(Rel rel) {
        List<Rel> x = Iter.toList(find(rel)); 
        x.forEach((r)->delete(r));
    }

    @Override
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public long size() {
        return store.size();
    }

    @Override
    public boolean contains(Rel rel) {
        if ( !store.containsKey(rel.getName()))
            return false;
        return store.get(rel.getName()).contains(rel);
    }
    
    @Override
    public boolean matches(Rel rel) {
        if ( !store.containsKey(rel.getName()))
            return false;
        Collection<Rel> x = store.get(rel.getName());
        //return x.stream().anyMatch(r-> match(r,rel));
        for(Rel r : x) {
            if ( match(r,rel) )
                return true;
        }
        return false;
    }

    @Override
    public Stream<Rel> all() {
        return store.values().stream();
    }

    @Override
    public Iterator<Rel> find(Rel rel) {
        if ( !store.containsKey(rel.getName()))
            return Iter.nullIterator();
        List<Rel> result = new ArrayList<>();
        Collection<Rel> x = store.get(rel.getName());
        for(Rel r : x) {
            if ( match(r,rel) )
                result.add(r);
        }
        return result.iterator();
    }
    
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n + ", "++ ", "\n") ;
        all().forEach(r->sj.add(r.toString()));
        return sj.toString();
    }
    
    private boolean match(Rel item, Rel pattern) {
        Tuple<Node> data = item.getTuple();
        Tuple<Node> pat = pattern.getTuple();
        if ( data.len() != pat.len() )
            return false;
        for ( int i = 0 ; i < data.len() ; i++ ) {
            Node n1 = data.get(i);
            Node n2 = pat.get(i);
            if ( !match(n1, n2) )
                return false;
        }
        return true;
    }

    private boolean match(Node n1, Node n2) {
        if ( wildcard(n1) || wildcard(n2) )
            return true;
        return n1.equals(n2);
    }

    private boolean wildcard(Node n) {
        return Node.ANY.equals(n) || n.isVariable();
    }
}
