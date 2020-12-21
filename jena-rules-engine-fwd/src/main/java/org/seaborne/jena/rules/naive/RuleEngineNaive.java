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

package org.seaborne.jena.rules.naive;

import org.seaborne.jena.rules.*;

/**
 * The <a href="">naïve</a> algorithm, done very naively. Hopefully, so simple it is easy
 * to verify as correct and then use in tests to compare results with other engines.
 */
public class RuleEngineNaive implements RuleEngine {
    private final RelStore data;
    private final RuleSet rules;

    public RuleEngineNaive(RelStore data, RuleSet rules) {
        this.data = data;
        this.rules = rules;
    }

    @Override
    public RelStore exec(RuleExecCxt rCxt) {
        RelStore generation = RelStoreFactory.createMem();
        generation.add(data);
        // What about variable arity?
        //--
        // Algorithm: Jacobi
        //   Do each pass with respect to the previous round.
        // Algorithm: Gauss-Seidel
        //   Combine generated facts during the loop.
        
        int i = 0;
        while(true) {
            i++;
            if ( rCxt.debug() )
                System.out.printf("N: Round %d\n", i);
            // Accumulator for this round.
            RelStore acc = RelStoreFactory.createMem();
            rules.asList().forEach(rule->{
                if ( rCxt.debug() )
                    System.out.println("==== "+rule);
                RelStore data = generation;
                if ( false )
                    // Algorithm: Gauss-Seidel
                    data = RelStoreFactory.combine(generation, acc);
                RuleExecLib.evalOne(rCxt, generation, acc, rule);
            });
            // Changes?
            if ( acc.isEmpty() )
                // No change - finished.
                return generation;
            generation.add(acc);
        }
    }
}

