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

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import java.util.function.BiFunction;

import migrate.binding.Binding;
import migrate.binding.BindingBuilder;
import migrate.binding.BindingFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.sse.SSE;
import org.junit.Test;
import org.seaborne.jena.rules.lang.RuleParseException;
import org.seaborne.jena.rules.lang.RulesParser;

public class TestMGU {

    // Algorithm to test.
    private static BiFunction<Rel, Rel, Binding> mgtTest = (r1, r2)->MGU.mgu_C(r1, r2);

    @Test public void mgu_00() { test("r()", "r()", "[]"); }
    @Test public void mgu_01() { test("r()", "s()", null); }
    @Test public void mgu_02() { test("r(?x)", "r()", null); }
    @Test public void mgu_03() { test("(:x, 'a', 1)", "(:x, 'a', 1)", "()"); }
    @Test public void mgu_04() { test("(?x, 'a', 1)", "(:x, 'a', 1)", "[(?x :x)]"); }
    @Test public void mgu_05() { test("(?x, 'a', ?y)", "(:x, 'a', 1)", "[(?x :x) (?y 1)]"); }
    @Test public void mgu_06() { test("(?x, 'a', ?y)", "(:x, 'a', 1)", "[(?y 1) (?x :x)]"); }

    @Test public void mgu_10() { test("(?x, ?x)", "(:x 'a')", null); }
    @Test public void mgu_11() { test("yes(?x, ?z)", "yes(?a, ?a)", "[ (?a ?x) (?x ?z) ]"); }
    @Test public void mgu_12() { test("yes(?x, ?x)", "yes(?a, ?b)", "[ (?a ?x) (?b ?x) ]"); }
    @Test public void mgu_13() { test("(?x, ?x, ?x)", "(:p, :p, 1)", null); }

    @Test public void mgu_14() { test("(?x, :p, ?x)", "(:a, :p, :a)", "[(?x :a)]"); }

    // Order should not matter.
    @Test public void mgu_20() { test("(?x, ?x)", "(:a, ?z)", "[(?x :a) (?z :a) ]"); }

    // Without correction.
    //@Test public void mgu_21() { test("(?x, ?x)", "(?z, :a)", "[(?z ?x) (?x :a)]"); }
    @Test public void mgu_21() { test("(?x, ?x)", "(?z, :a)", "[(?z :a) (?x :a)]"); }

    // Reverse the RHS and LHS
    @Test public void mgu_22() { test("(:a, ?z)", "(?x, ?x)", "[(?x :a) (?z :a)]"); }
    // Without correction.
    //@Test public void mgu_23() { test("(?z, :a)", "(?x, ?x)", "[(?z :a) (?x ?z)]"); }
    @Test public void mgu_23() { test("(?z, :a)", "(?x, ?x)", "[(?z :a) (?x :a)]"); }

    //XXX Check any flipped?
//        mgu("(?x ?y)", "(:x ?B)"));
//        mgu("(:x ?B)", "(?x ?y)"));

    // Can't consistent unify second ?v1 and ?v2
    @Test public void mgu_30() { test("(?v1, ?v2, ?v1, ?v2)", "(:a, :b, ?z, ?z)", null); }
    @Test public void mgu_31() { test("(?v1, ?v2, ?v1, ?v2)", "(?z, ?z, :a, :b)", null); }
    // Reversed.
    @Test public void mgu_32() { test("(:a, :b, ?z, ?z)", "(?v1, ?v2, ?v1, ?v2)", null); }
    @Test public void mgu_33() { test("(?z, ?z, :a, :b)", "(?v1, ?v2, ?v1, ?v2)", null); }

    // ????
    @Test public void mgu_40() { test("(?v1, ?v2)", "(?A, ?A)", "[(?A ?v1) (?v1 ?v2)]"); }
    @Test public void mgu_41() { test("(?v1, ?v2, ?v2, ?v1)", "(?w1, ?w2, ?A, ?A)", null); }
    @Test public void mgu_42() { test("(?A, ?A)", "(?v1, ?v2)", "[(?A ?v1) (?v1 ?v2)]"); }
    @Test public void mgu_43() { test("(?w1, ?w2, ?A, ?A)", "(?v1, ?v2, ?v2, ?v1)", null); }

    @Test public void mgu_50() { test("(?x, 'a', ?z)", "(:x, ?a ?b)", "[(?x :x) (?a 'a') (?b ?z)]"); }
    @Test public void mgu_51() { test("no(?x, 'x', ?x, 'b')", "no(?y, 'x', 'a', ?y)",   null); }
    @Test public void mgu_52() { test("yes(?x, 'x', ?x, 'a')", "yes(?y, 'x', 'a', ?y)", "[ (?x 'a') (?y 'a') ]"); }
    @Test public void mgu_53() { test("yes(?x, 'x', ?x, 'a')", "yes('a', 'x', ?y, ?y)", "[ (?x 'a') (?y 'a') ]"); }

    @Test public void mgu_54() { test("(?x ?x ?y)", "('X' ?A ?A)", "[ ( ?x 'X' ) ( ?A 'X' ) ( ?y 'X' ) ]"); }

    // "Logic Programming and Databases" p86 example.
    @Test public void mgu_60() { test("yes(?x, ?z, 'a', ?u)", "yes(?y, ?y, ?v ?w)",
                                      "[ ( ?y ?x ) ( ?x ?z ) ( ?v 'a' ) ( ?w ?u ) ]"); }

    private void test(String xRel, String yRel, String outcome) {
        Rel rel1 = RulesParser.parseAtom(xRel);
        Rel rel2 = RulesParser.parseAtom(yRel);
        Binding b1 = mgtTest.apply(rel1, rel2);
        Binding b2 = MGU.mguAlg2(rel1, rel2);

        if ( b1 == null && b2 == null ) {
            System.out.println("P: "+rel1+" "+rel2);
            System.out.println("Null");
            System.out.println();
            return;
        }

        Binding b = b2;
        Rel rel1a = MGU.applyMGU(b, rel1);
        Rel rel2a = MGU.applyMGU(b, rel2);

        System.out.println("In: "+rel1+" "+rel2);
        System.out.println("MGU: "+b);
        System.out.println("Query: -- "+rel1a);
        System.out.println("Head:  -- "+rel2a);
        if ( ! rel1a.equals(rel2a) )
            System.out.println("  Differ");

        Binding expected = parseBinding(outcome);
        //assertEquals(expected, b);
//        System.out.printf("%s %s --> %s\n", rel1, rel2, b);
//        System.out.println();

//        Binding b2 = MGU.mguAlg2(rel1, rel2);
//        assertEquals(expected, b2);

        if ( outcome != null ) {
            //Rel rel1a = MGU.applyMGU(b, rel1);
            //Rel rel2a = MGU.applyMGU(b, rel2);
            if ( ! rel1a.equals(rel2a) ) {
                System.out.println("Fail");
//                System.out.println("P: "+rel1+" "+rel2);
//                System.out.println("Q: "+rel1a);
//                System.out.println("H: "+rel2a);
                System.out.println();
            }
            System.out.println();
            assertTrue("MGU applied", rel1a.equals(rel2a) );
        } else {
            System.out.println();
        }

    }

    // BindingParser

    private static PrefixMap pmap = org.apache.jena.riot.system.Prefixes.adapt(SSE.getPrefixMapRead());

    private Binding parseBinding(String outcome) {
        if ( outcome == null )
            return null;
        Tokenizer tok = TokenizerText.fromString(outcome);
        BindingBuilder builder = BindingFactory.create();
        if ( ! tok.hasNext() )
            throw exception("No token") ;
        parseBinding(tok, builder);
        if ( tok.hasNext() )
            throw exception("Parse error: Content after ')': "+tok.next()+" ...");
        return builder.build();
    }

    private void parseBinding(Tokenizer tok, BindingBuilder builder) {
        Token t = tok.next();
        if ( ! t.hasType(TokenType.LPAREN) && !t.hasType(TokenType.LBRACKET))
            throw exception("Parse error: Expected '(' or '[' at start of binding");
        //"( (?x 'a') ...."
        while(tok.hasNext()) {
            t = tok.next();
            if ( t.hasType(TokenType.RPAREN) || t.hasType(TokenType.RBRACKET))
                break;
            if ( ! t.hasType(TokenType.LPAREN) && !t.hasType(TokenType.LBRACKET))
                throw exception("Parse error: Expected '(' or '[': got: "+t, t);
            t = tok.next();
            if ( ! t.hasType(TokenType.VAR) )
                throw exception("Parse error: Expected variable: got: "+t, t);
            Var var = Var.alloc(t.asNode());

            skipComma(tok);

            t = tok.next();
            Node node = tokenToNode(t);
            if ( node == null )
                throw exception("Parse error: Expected node: got: "+t, t);
            t = tok.next();
            if ( ! t.hasType(TokenType.RPAREN) && !t.hasType(TokenType.RBRACKET))
                throw exception("Parse error: Expected ')' or ']': got: "+t, t);
            builder.add(var, node);
        }
    }

    private static Node tokenToNode(Token token) {
        if ( token.getType() == TokenType.UNDERSCORE )
            return Node.ANY;
        return token.asNode(pmap);
    }

    private static void skipComma(Tokenizer tok) {
        if ( ! tok.hasNext() )
            throw new RuleParseException("Parse error: Early end of token input", (int)tok.getLine(), (int)tok.getColumn());
        Token token = tok.peek();
        if ( token.getType() == TokenType.COMMA ) {
            if ( ! tok.hasNext() )
                throw new RuleParseException("Parse error: Expected token while skipping COMMA", (int)tok.getLine(), (int)tok.getColumn());
            token = tok.next();
        }
        return;
    }


    private static RuntimeException exception(String message) {
        return new RuntimeException(message);
    }

    private static RuntimeException exception(String message, Token token) {
        return exception(message, token.getLine(), token.getColumn());
    }


    private static RuntimeException exception(String message, long line, long column) {
        return new RuntimeException(format("[%d, %d] %s", line, column, message));
    }
}

