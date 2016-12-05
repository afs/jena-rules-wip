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

import java.util.Iterator;
import java.util.stream.Stream;

public interface RelStore {
    // Maybe RelStore -> Map of "Rels", all Rels of the same name

    public default void setReadOnly() { setWritable(false); }
    public void setWritable(boolean allowUpdate);
    
    public default boolean isReadonly() { return ! isUpdateable();} 
    public boolean isUpdateable();
    
    public void add(Rel rel);
    public void add(RelStore data);

    public void delete(Rel rel);
    public void removeAll(Rel rel);

    public Iterator<Rel> find(Rel rel);
    
    /** Get all the Rels matching a name*/
    public Stream<Rel> get(String relName);
    
    /** Does this RelStore have any rels of a given name? */ 
    public boolean containRel(String relName);

    public boolean matches(Rel rel);

    /* Contains exactly, no pattern matching */
    public boolean contains(Rel rel);

    public boolean isEmpty();

    public long size();
    
    public Stream<Rel> all();

}

