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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jena.graph.Node;
import org.junit.Test;
import org.seaborne.jena.rules.lang.RuleParseException;
import org.seaborne.jena.rules.lang.RulesParser;

public class TestParser {
    private Rule parseRule(String string) {
        Rule rule = RulesParser.parseRule(string);
        return rule;
    }

    private Rel parseAtom(String string) {
        Rel rel = RulesParser.parseAtom(string);
        return rel;
    }

    @Test public void rule_rel_1() {
        Rel r = parseAtom("fact()");
    }

    @Test public void rule_rel_2() {
        Rel r = parseAtom("rel(1,2,3)");
        assertEquals(3, r.getTuple().len());
    }

    @Test public void rule_rel_3() {
        Rel r = parseAtom("rel(1 2 3)");
        assertEquals(3, r.getTuple().len());
    }

    @Test public void rule_rel_4() {
        Rel r = parseAtom("rel(:a <b> '3')");
        assertEquals(3, r.getTuple().len());
        assertEquals("rel", r.getName());
    }

    @Test public void rule_rel_5() {
        Rel r = parseAtom("(?x)");
        assertEquals(1, r.getTuple().len());
        assertTrue(r.getTuple().get(0).isVariable());
        assertEquals("", r.getName());
    }

    @Test public void rule_rel_6() {
        Rel r = parseAtom("any(_)");
        assertEquals(1, r.getTuple().len());
        assertTrue(r.getTuple().get(0).equals(Node.ANY));
    }

    @Test public void rule_fact_1() {
        Rule r = parseRule("fact() .");
        assertTrue(r.isFact());
    }

    @Test public void rule_fact_2() {
        Rule r = parseRule("fact(1,2,3) .");
        assertTrue(r.isFact());
    }

    @Test public void rule_fact_3() {
        Rule r = parseRule("fact(1 2 3) .");
        assertTrue(r.isFact());
    }

    @Test public void rule_fact_4() {
        Rule r = parseRule("(<s> <p> <o>) .");
        assertTrue(r.isFact());
    }

    @Test public void rule_fact_5() {
        Rule r = parseRule("(<s> <p> <o>)");
        assertTrue(r.isFact());
    }

    // Error.
    @Test(expected=RulesException.class)
    public void rule_fact_err_1() {
        Rule r = parseRule("fact(1,?x,3) .");
        assertTrue(r.isFact());
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_1() {
        Rule r = parseRule("<-.");
        assertNotNull(r);
        assertNull(r.getHead());
        assertTrue(r.getBody().isEmpty());
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_2() {
        Rule r = parseRule("<-");
        assertNull(r.getHead());
        assertTrue(r.getBody().isEmpty());
    }

    @Test
    public void rule_parse_3() {
        Rule r = parseRule("head()<-.");
        assertNotNull(r);
        assertNotNull(r.getHead());
        assertTrue(r.getBody().isEmpty());
        assertTrue(r.isFact());
        assertNotNull(r.getFact());
        assertEquals("head", r.getFact().getName());
    }

    @Test
    public void rule_parse_4() {
        Rule r = parseRule("head()<-body().");
        assertNotNull(r);
        assertNotNull(r.getHead());
        assertFalse(r.getBody().isEmpty());
        assertFalse(r.isFact());
    }

    @Test
    public void rule_parse_5() {
        Rule r = parseRule("head()<-lit1() , lit2() .");
        assertNotNull(r);
        assertNotNull(r.getHead());
        assertEquals(2, r.getBody().size());
    }

    @Test
    public void rule_parse_6() {
        Rule r = parseRule("head():-lit1() , lit2() .");
        assertNotNull(r);
        assertNotNull(r.getHead());
        assertEquals(2, r.getBody().size());
    }

    @Test
    public void rule_parse_7() {
        Rule r = parseRule("head():-lit1() lit2() .");
        assertNotNull(r);
        assertNotNull(r.getHead());
        assertEquals(2, r.getBody().size());
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_90() {
        Rule r = parseRule("");
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_91() {
        Rule r = parseRule(".");
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_92() {
        Rule r = parseRule(". head():-lit1() lit2() .");
    }

    @Test(expected=RuleParseException.class)
    public void rule_parse_93() {
        Rule r = parseRule("head():-lit1() lit2() . .");
    }

    // XXX parse RuleSets

}
