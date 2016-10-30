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

package org.seaborne.jena.inf2;

import java.util.* ;

import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.sse.SSE ;

public class Rule {
    private final Triple head ;
    private final List<Triple> body ;

    public Rule(Triple head, Triple...body) {
        this.head = head ;
        this.body = Collections.unmodifiableList(Arrays.asList(body)) ;
    }

    public Rule(Triple head, List<Triple> body) {
        this.head = head ;
        this.body = Collections.unmodifiableList(new ArrayList<>(body)) ;
    }

    public Triple getHead() {
        return head ;
    }

    public List<Triple> getBody() {
        return body ;
    }
    
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ") ;
        body.stream().map(Rule::p).forEach(sj::add);
        return String.format("%s <- %s", p(head), sj.toString()) ; 
    }

    private static String p(Triple triple) {
        return SSE.str(triple).replaceAll("ANY", "_") ;
    }
    

    public static String str(Rule rule) {
        StringJoiner sj = new StringJoiner(", ") ;
        rule.getBody().stream().map(Rule::p).forEach(sj::add);
        return String.format("%-30s <- %s", p(rule.getHead()), sj.toString()) ; 
    }

    public static String str(List<Rule> rules) {
        StringJoiner sj = new StringJoiner(",\n  ", "[ ", "\n]") ;
        rules.forEach((r)->sj.add(str(r))) ;
        return sj.toString() ; 
    }

}
