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

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.RelStore;

/**
 * A RelStore of 2 RelStores - assumed to be disjoint.
 * Additions go to the first/left store.
 */
public class RelStore2 implements RelStore {

    private RelStore store1;
    // Anti-store for store1 for deletes? Makes store2 read-only.
    private RelStore store2;

    public RelStore2(RelStore store1, RelStore store2) {
        this.store1 = store1 ;
        this.store2 = store2 ;
    }

//    @Override
//    public void setWritable(boolean allowUpdate) {
//        store1.setWritable(allowUpdate);
//    }
//
//    @Override
//    public boolean isUpdateable() {
//        return store1.isUpdateable();
//    }
//
//    @Override
//    public void add(Rel rel) {
//        if ( store1.contains(rel) )
//            return ;
//        if ( store2.contains(rel) )
//            return ;
//        store1.add(rel);
//    }
//
//    @Override
//    public void add(RelStore data) {
//        data.all().forEach(this::add);
//    }
//
//    @Override
//    public void delete(Rel rel) {
//        store1.delete(rel);
//        store2.delete(rel);
//    }
//
//    @Override
//    public void removeAll(Rel rel) {
//        List<Rel> x = Iter.toList(find(rel));
//        x.forEach((r)->delete(r));
//    }

    @Override
    public Iterator<Rel> find(Rel rel) {
        Iterator<Rel> x1 = store1.find(rel);
        Iterator<Rel> x2 = store2.find(rel);
        return Iter.concat(x1,  x2);
    }

    @Override
    public boolean matches(Rel rel) {
        return store1.matches(rel) || store2.matches(rel);
    }

    /* Contains exactly, no pattern matching */
    @Override
    public boolean contains(Rel rel) {
        return store1.contains(rel) || store2.contains(rel);
    }

    @Override
    public boolean isEmpty() {
        return store1.isEmpty() && store2.isEmpty();
    }

    @Override
    public long size() {
        return store1.size() + store2.size();
    }

    @Override
    public Stream<Rel> all() {
        return Stream.concat(store1.all(), store2.all());
    }

    @Override
    public Stream<Rel> get(String relName) {
        return Stream.concat(store1.get(relName), store2.get(relName));
    }

    @Override
    public boolean containRel(String relName) {
        return store1.containRel(relName) || store2.containRel(relName);
    }
}

