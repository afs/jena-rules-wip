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

package org.seaborne.jena.rules.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap ;
import org.apache.jena.ext.com.google.common.collect.Multimap ;
import org.seaborne.jena.rules.Rel ;
import org.seaborne.jena.rules.RelStore ;
import org.seaborne.jena.rules.RelStoreAcc;

/**
 * Simple {@link RelStore}, uses a Multimap of rule name to
 * Useful as an independent implementation for testing.
 */
public class RelStoreAccSimple extends RelStoreBase implements RelStoreAcc {

    private Multimap<String,Rel> store;

    public RelStoreAccSimple() {
        store = ArrayListMultimap.create();
    }

    private RelStoreAccSimple(Multimap<String,Rel> store) {
        store = ArrayListMultimap.create(store);
    }

    private boolean readOnly = false;
    @Override
    public void setWritable(boolean allowUpdate) {
        this.readOnly = !allowUpdate;
    }

    @Override
    public boolean isUpdateable() {
        return !readOnly;
    }

    protected void checkUpdatable() {
        checkUpdatable(null);
    }

    protected void checkUpdatable(Supplier<String> message) {
        if ( readOnly ) {
            String msg = "Attempt to update a read-only RelStore";
            if ( message != null )
                msg = msg+": "+message.get();
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public Iterator<Rel> find(Rel rel) {
        // Materializing for simplicity
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
    public boolean isEmpty() {
        return store.isEmpty();
    }

    @Override
    public long size() {
        return store.size();
    }


    @Override
    public void add(Rel rel) {
        //checkUpdatable(()->"add("+rel+")") ;
        store.put(rel.getName(), rel);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public void delete(Rel rel) {
        //checkUpdatable(()->"delete("+rel+")") ;
        store.remove(rel.getName(), rel);
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
        // Does not work! CCME!
        //return x.stream().anyMatch(r-> match(r,rel));
        for(Rel r : x) {
            if ( match(r,rel) )
                return true;
        }
        return false;
    }

    @Override
    public Stream<Rel> stream() {
        return store.values().stream();
    }
}
