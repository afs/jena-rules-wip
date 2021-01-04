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
import org.seaborne.jena.rules.api.EngineType;

/**
 * The <a href="">naïve</a> algorithm, done very naively. Hopefully, so simple it is easy
 * to verify as correct and then use in tests to compare results with other engines.
 */
public class RuleEngineNaive extends RulesEngineFwd {

    public static RulesEngine.Factory factory = RuleEngineNaive::new;

    private static RuleExecCxt rCxt = RuleExecCxt.global;

    private static boolean GaussSeidel = false;

    public RuleEngineNaive(RelStore data, RuleSet ruleSet) {
        super(data, ruleSet);
    }

    @Override
    public EngineType engineType() {
        return GaussSeidel ? EngineType.FWD_NAIVE_JACOBI : EngineType.FWD_NAIVE_JACOBI;
    }

    // What about variable arity?
    //--
    // Algorithm: Jacobi
    //   Do each pass with respect to the previous round.
    // Algorithm: Gauss-Seidel
    //   Combine generated facts during the loop.

    @Override
    protected RelStore doForReal() {
        RelStoreAcc generation = RelStoreFactory.createAcc();
        generation.add(data);

        int i = 0;
        while(true) {
            i++;
            if ( rCxt.debug() )
                rCxt.out().printf("N: Round %d\n", i);
            // Accumulator for this round.
            RelStoreAcc acc = RelStoreFactory.createAcc();
            rules.asList().forEach(rule->{
                if ( rCxt.debug() )
                    rCxt.out().println("==== "+rule);
                RelStore data =
                        GaussSeidel
                        // Algorithm: Gauss-Seidel (propagate early)
                        ? RelStoreFactory.combine(generation, acc)
                        // Algorithm: Jacobi (propagate last round)
                        : generation;
                RulesLib.evalOne(rCxt, generation, acc, rule);
                if ( rCxt.debug() )
                    rCxt.out().println("Step => "+acc);

            });
            if ( rCxt.debug() )
                rCxt.out().println("Acc  => "+acc);

            // Changes?
            if ( acc.isEmpty() )
                // No change - finished.
                return generation.freeze();
            generation.add(acc);
            if ( rCxt.debug() )
                rCxt.out().println("New  => "+generation);
        }
    }
}

