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

public class DevRule {
    // Analyse for intensional and extensional.
    // idb relation = occurs in the head of a rule
    // edb relation = relation occuring only in the body of rules
    
    /*
     * Separate rule engine?
     * 
     * Rule - head, body
     * Relation - named tuple of termsterms
     * Term is a Var or a Const
     *   Term = Item<Var, Const> 
     *  
     */
    
    //Datalog : Single head clause
    //  All variables in a negation in the body must also be positively mentioned in the body.
    //  All variables in an artimetic relationship must be in the body as solveable sterms.
    
    // PropertyFunctionGenerator.buildPropertyFunctions
    //    magicProperty needs to be careful about rdf:rest! 
    
    // Version without recursion
    //   Recursion via SPARQL only over the base data.
    
    // T0 <- T1, T2, ...  SPARQL, restricted.
    // T  <- SPARQL 
}
