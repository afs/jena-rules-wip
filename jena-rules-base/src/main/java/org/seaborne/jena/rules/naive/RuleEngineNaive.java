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
import org.seaborne.jena.rules.store.RelStore2;

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
    protected RelStore generateInferred() {
        RelStoreAcc inferred = RelStoreFactory.createAcc();
        // Keep as two separate RelStores so we can track what is added.
        RelStore both = new RelStore2(data, inferred);

        int i = 0;
        while(true) {
            i++;
            if ( rCxt.debug() )
                rCxt.out().printf("N: Round %d\n", i);
            // Accumulator for this round.
            RelStoreAcc acc = RelStoreFactory.createAcc();
            boolean changeHappened = false;
            for ( Rule rule: rules ) {
                if ( rCxt.debug() )
                    rCxt.out().println("==== "+rule);
                // Jacobi - inferred is updated at end of round
                // GaussSeidel - inferred is updated after each rule.
//                RelStore data =
//                        GaussSeidel
//                        // Algorithm: Gauss-Seidel (propagate early)
//                        ? RelStoreFactory.combine(generation, acc)
//                        // Algorithm: Jacobi (propagate last round)
//                        : generation;
                RulesLib.evalOne(rCxt, both, acc, rule);
                changeHappened = changeHappened | ! acc.isEmpty();
                if ( rCxt.debug() )
                    rCxt.out().println("Step => "+acc);
                if ( GaussSeidel ) {
                    inferred.add(acc);
                    acc.clear();
                }
            } // End of rule loop.
            if ( rCxt.debug() )
                rCxt.out().println("Acc  => "+acc);

            // Changes?
            if ( ! changeHappened )
                // No change - finished.
                break;
            if ( ! GaussSeidel )
                inferred.add(acc);
            if ( rCxt.debug() )
                rCxt.out().println("New  => "+inferred);
        }
        return inferred.freeze();
    }
}

