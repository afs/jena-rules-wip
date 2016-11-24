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

public class Rule {
    private  Rel head ;
    private  List<Rel> body ;

    @SafeVarargs
    public Rule(Rel head, Rel...body) {
        this.head = head;
        //List.of in java9
        this.body = Collections.unmodifiableList(Arrays.asList(body)) ;
    }

    public Rule(Rel head, List<Rel>body) {
        this.head = head;
        //List.of in java9
        this.body = new ArrayList<>(body);
    }

    public Rel getHead() {
        return head ;
    }

    public List<Rel> getBody() {
        return body ;
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
        if ( rule.getBody().isEmpty() )
            sb.append(" .");
        else {
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
