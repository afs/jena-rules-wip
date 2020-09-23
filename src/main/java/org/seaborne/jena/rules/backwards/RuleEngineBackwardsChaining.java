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

package org.seaborne.jena.rules.backwards;

import java.util.Iterator;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.seaborne.jena.rules.*;

public class RuleEngineBackwardsChaining  implements RuleEngine {

    private final RelStore data; // RelStore.setReadOnly.
    private final RuleSet rules;

    public RuleEngineBackwardsChaining(RelStore data, RuleSet rules) {
        this.data = data;
        this.rules = rules;
    }

    @Override
    public RelStore exec() {
        return null;
    }

    public Iterator<?> solve(Rel pattern) {
        return null;
    }

    public RelStore one(Rel target) {
        System.out.println("Target: "+target);
        //if ( target.isConcrete();

        Rel rel = target;

        // Find rule heads
        // Get by head name.
        rules.stream()
            .filter(rule->compatible(rule, rel))
            .forEach(System.out::println);

        data.all()
            .filter(dRel->compatible(new Rule(dRel), rel))
            .forEach(System.out::println);


        return null;

    }

    // Is rule (argument 1) compatible with data rel (argument 2)?
    // Compatible => ground rule slots match.
    private boolean compatible(Rule rule, Rel data) {

        // To match:
        //   Do const-const match?
        //   Can we bind a data pattern to a rule const?
        //   Can we ground the rule use a const of the data?


        Rel head = rule.getHead();
        //return compatible(head, rel);
        Tuple<Node> tuple1 = head.getTuple();
        Tuple<Node> tuple2 = data.getTuple();

        if( tuple1.len() != tuple2.len() )
            return false;
        // Const-const match.
        for ( int i = 0 ; i < tuple1.len(); i++ ) {
            Node n1 = tuple1.get(i);
            Node n2 = tuple2.get(i);
            if ( ! constCompatible(n1,n2) )
                return false;
        }


        return true;
    }

    private boolean compatible(Node n1 /*rule*/, Node n2 /*pattern*/) {
//        if ( Var.isVar(n1) && Var.isVar(n2) ) {
//            return false;
//        }
//        if ( Var.isVar(n1) ) {
//            //
//            // Rule defines a variable for a ground term.
//            return true;
//        }
//        if ( Var.isVar(n2) ) {
//            // Target defines a variable for a rule ground term.
//            return true;
//        }
        return false;
    }
    private boolean constCompatible(Node n1 /*rule*/, Node n2 /*pattern*/) {
        if ( Var.isVar(n1) || Var.isVar(n2) )
            return true;

        // Both ground - must match.
        return n1.equals(n2);
    }

}

