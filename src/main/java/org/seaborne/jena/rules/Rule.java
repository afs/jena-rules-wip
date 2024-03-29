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

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

public class Rule {
    private  Rel head ;
    private  List<Rel> body ;

    /** Create a Fact */
    public Rule(Rel head) {
       this.head = head;
       this.body = null;
       check();
    }

    //@SafeVarargs
    public Rule(Rel head, Rel...body) {
        this.head = head;
        this.body = List.of(body) ;
        check();
    }

    public Rule(Rel head, List<Rel>body) {
        this.head = head;
        this.body = body==null ? null : new ArrayList<>(body);
        check();
    }

    /** Check:
     * <ul>
     * <li>All variables in the head are mentioned in the body.
     * </ul>
     */
    private void check() {
        if ( head == null )
            throw new RuleException("Null head");
        if ( head == null )
            return ;
        Set<Var> vars = new HashSet<>();
        if ( body != null ) {
            for(Rel br : body ) {
                accVars(vars, br);
            }
        }
        for ( Node n : head.getTuple() ) {
            if ( Var.isVar(n) ) {
                if ( ! vars.contains(n) )
                    throw new RuleException("Variable '"+n+"' in head but not in body");
            }
        }
    }

    private void accVars(Collection<Var> acc, Rel rel) {
        rel.getTuple().forEach(n->{
            if ( Var.isVar(n) )
                acc.add(Var.alloc(n)) ;
        });
    }

    public Rel getHead() {
        return head ;
    }

    public List<Rel> getBody() {
        return body ;
    }

    public boolean isFact() {
        return body==null;
    }

    public Rel getFact() {
        if ( ! isFact() )
            throw new RuleException("Not a fact: "+this);
        return head;
    }

    //Cache strings.?

    @Override
    public String toString() {
        return str(this);
    }

    private static String p(Rel rel) {
        return rel.toString();
    }


    public static String str(Rule rule) {
        StringBuilder sb = new StringBuilder();
        if ( rule.getHead() != null ) {
            sb.append(p(rule.getHead()));
            sb.append(" ");
        }
        sb.append("<-");
        if ( ! rule.getBody().isEmpty() ) {
            StringJoiner sj = new StringJoiner(", ") ;
            rule.getBody().stream().map(Rule::p).forEach(sj::add);
            sb.append(" ");
            sb.append(sj.toString());
        }
        sb.append(" .");
        return sb.toString();
    }

    public static String str(List<Rule> rules) {
        StringJoiner sj = new StringJoiner(",\n  ", "[ ", "\n]") ;
        rules.forEach((r)->sj.add(str(r))) ;
        return sj.toString() ;
    }
}
