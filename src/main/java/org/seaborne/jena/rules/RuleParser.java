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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.TokenType;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;
import org.apache.jena.sparql.sse.SSE;

public class RuleParser {

    // Replace with javacc sometime.
    // How of make the end of rule? DOT?
    
    public static void main(String...arg) {
//        Rel rel = parseRel("name(:x, :p :o)") ;
//        System.out.println(rel) ;
        
        String data[] = {
            "name(:x, :p :o) <- name1(:x, :p ?Z), name2(?Z :p :o) (?Z :p :o) ."
            ,"<- name1(:x, :p ?Z), name2(?Z :p :o) (?Z :p :o) ."
            ,"name(:x, :p :o) <- ."
        };
        
        for ( String d: data) {
            System.out.println(">> "+d);
            Rule rule = parseRule(d) ;
            System.out.println("<< "+rule);
            System.out.println();
        }
    }

    private static PrefixMap pmap = PrefixMapFactory.create(SSE.getPrefixMapRead());
    
    public static Rel parseRel(String x) {
        Tokenizer tok = TokenizerFactory.makeTokenizerString(x);
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
                throw new RuleParseException("Parse error: Expected left bracket: got: "+token) ;
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
            Node n = token.asNode(pmap);
            if ( n == null )
                throw new RuleParseException("Parse error: Expected node token, got: "+token);
            terms.add(n);
        }
        Tuple<Node> tuple = TupleFactory.create(terms);
        return new Rel(relName, tuple);
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
        // rel{0,1} <- rel{0,} ...
        if ( ! x.contains("<-") )
            throw new RuleParseException("Parse error: No rule arrow");
            // Fast path the split.
        String[] z = x.split("<-",2);
        z[0] = z[0].trim();
        Rel head = null;
        if ( ! z[0].isEmpty() )
            head = parseRel(z[0]);
        
        Tokenizer tok = TokenizerFactory.makeTokenizerString(z[1]);
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
