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

import org.junit.BeforeClass;
import org.junit.Test;
import org.seaborne.jena.rules.impl.RelStoreSimple;
import org.seaborne.jena.rules.parser.RuleParser;

import static org.junit.Assert.*;

public class TestRelStore {
    static String   data[] = {"name(:x, :p :o)", "rel(1,2)", "rel(2,3)"};

    static RelStore relStore;

    public RelStore getRelStore() {
        return relStore;
    }

    public RelStore createEmptyRelStore() {
        return new RelStoreSimple();
    }

    @BeforeClass
    public static void beforeClass() {
        relStore = new RelStoreSimple();
        for ( String d : data ) {
            Rel rel = RuleParser.parseRel(d);
            relStore.add(rel);
        }
    }

    @Test
    public void relStore_01() {
        RelStore rs = createEmptyRelStore();
        Rel rel = RuleParser.parseRel("(:s :p :o)");
        rs.add(rel);
        assertFalse(rs.isEmpty());
        assertEquals(1,rs.size());
        assertTrue(rs.contains(rel));
    }

    @Test
    public void relStore_02() {
        RelStore rs = createEmptyRelStore();
        Rel rel = RuleParser.parseRel("(:s :p :o)");
        rs.add(rel);
        assertFalse(rs.isEmpty());
        rs.delete(rel);
        assertTrue(rs.isEmpty());
        assertFalse(rs.contains(rel));
    }

    // Find, match etc.
    
    @Test
    public void relStore_access_01() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("(1,2)");
        assertFalse(rs.contains(rel));
        assertFalse(rs.matches(rel));
    }

    @Test
    public void relStore_access_02() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("rel(1,9)");
        assertFalse(rs.contains(rel));
        assertFalse(rs.matches(rel));
    }

    @Test
    public void relStore_access_03() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("(1,_)");
        assertFalse(rs.contains(rel));
        assertFalse(rs.matches(rel));
    }

    @Test
    public void relStore_access_04() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("rel(1,_)");
        assertFalse(rs.contains(rel));
        assertTrue(rs.matches(rel));
    }

    @Test
    public void relStore_access_05() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("rel(1,?x)");
        assertFalse(rs.contains(rel));
        assertTrue(rs.matches(rel));
    }
    
    @Test
    public void relStore_access_06() {
        RelStore rs = getRelStore();
        Rel rel = RuleParser.parseRel("rel(?x,2)");
        assertFalse(rs.contains(rel));
        assertTrue(rs.matches(rel));
    }
}
