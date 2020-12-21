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

package org.seaborne.jena.rules.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.sse.SSE;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.Rule;

/**
 * Syntax:<br/>
 * <pre>
 *   fact(...).
 *   head(...) <- body .
 * </pre>
 * where <tt>body</tt> is a number of terms (class {@link Rel}).
 * Rule clause terms can be named or unnamed.
 * <pre>
 *     (:s :p ?o) <- (:s :q1 ?z ) (?z :q2 ?o) .
 * <pre>
 *
 */
public class RuleParser {

    // Replace with javacc sometime.

    private static PrefixMap pmap = org.apache.jena.riot.system.Prefixes.adapt(SSE.getPrefixMapRead());

    public static Rel parseRel(String x) {
        Tokenizer tok = TokenizerText.fromString(x);
        if ( ! tok.hasNext() )
            throw new RuleParseException("No token") ;
        Rel rel = parseRel(tok);
        if ( tok.hasNext() )
            throw new RuleParseException("Parse error: Content after ')': "+tok.next()+" ...");
        return rel;
    }

    public static Rel parseRel(Tokenizer tok) {
        if ( ! tok.hasNext() )
            throw new RuleParseException("No token") ;
        String relName = "";

        if ( tok.peek().hasType(TokenType.LPAREN)) {
            tok.next();
        } else {
            Token token = tok.next();
            if ( ! token.isString() && ! token.isWord() && ! token.hasType(TokenType.UNDERSCORE) )
                throw new RuleParseException("Parse error: Rel name: "+token) ;
            if ( token.hasType(TokenType.UNDERSCORE) )
                relName = "_";
            else
                relName = token.getImage();
            if ( ! tok.hasNext() )
                throw new RuleParseException("No token when looking for '('") ;
            token = tok.next();
            if ( token.getType() != TokenType.LPAREN)
                throw new RuleParseException("Parse error: Expected LPAREN: got: "+token) ;
        }

        boolean first = true;
        List<Node> terms = new ArrayList<>();
        while(tok.hasNext()) {
            if ( ! first )
                skipComma(tok);
            else
                first = false ;
            Token token = tok.next();
            if ( token.getType() == TokenType.RPAREN )
                break;
            Node n = tokenToNode(token);
            if ( n == null )
                throw new RuleParseException("Parse error: Expected node token, got: "+token);
            terms.add(n);
        }
        Tuple<Node> tuple = TupleFactory.create(terms);
        return new Rel(relName, tuple);
    }

    private static Node tokenToNode(Token token) {
        if ( token.getType() == TokenType.UNDERSCORE )
            return Node.ANY;
        return token.asNode(pmap);
//        Node n = token.asNode(pmap);
//        if ( n == null )
//            throw new RuleParseException("Parse error: Expected node token, got: "+token);
//        return n;
    }

    private static void skipComma(Tokenizer tok) {
        if ( ! tok.hasNext() )
            throw new RuleParseException("Parse error: Early end of token input");
        Token token = tok.peek();
        if ( token.getType() == TokenType.COMMA ) {
            if ( ! tok.hasNext() )
                throw new RuleParseException("Parse error: Expected token while skipping COMMA");
            token = tok.next();
        }
        return;
    }

    public static Rule parseRule(String x) {
        // fact(,,,).
        // head(...) <- body .

        // rel{0,1} <- rel{0,} ...
        if ( ! x.contains("<-") ) {
            Tokenizer tok = TokenizerText.fromString(x);
            Rel rel = parseRel(tok);
            if ( !tok.hasNext() )
                throw new RuleParseException("Parse error: Expected DOT after fact.");
            Token peekToken = tok.peek();
            if ( peekToken == null || peekToken.getType() == TokenType.EOF )
                throw new RuleParseException("Parse error: Unexpected end of fact.");
            if ( peekToken == null || peekToken.getType() == TokenType.DOT )
                tok.next();
            if ( tok.hasNext() ) {
                Token token2 = tok.next();
                throw new RuleParseException("Parse error: Unexpected token after fact. "+token2);
            }
            return new Rule(rel);
        }
        // Not great but "<-" isn't a token.
        String[] z = x.split("<-",2);
        z[0] = z[0].trim();
        if ( z[0].isEmpty() )
            throw new RuleParseException("No head to rule");

        Rel head = parseRel(z[0]);
        Tokenizer tok = TokenizerText.fromString(z[1]);
        List<Rel> body = new ArrayList<>();
        boolean first = true;

        while(tok.hasNext()) {
            Token peekToken = tok.peek();
            if ( peekToken == null || peekToken.getType() == TokenType.EOF )
                break;
            if ( peekToken == null || peekToken.getType() == TokenType.DOT ) {
                tok.next();
                break;
            }

            if ( ! first )
                skipComma(tok);
            else
                first = false ;
            Rel r = parseRel(tok);
            body.add(r);
        }

        return new Rule(head, body);
    }
}
