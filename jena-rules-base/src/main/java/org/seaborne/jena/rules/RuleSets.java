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

import org.seaborne.jena.rules.lang.RulesParser;

public class RuleSets {

    /** RDFS, without axioms */
    public static RuleSet rulesRDFSjenaMin() {
        return rules(
            // Domain and range
              "(?s rdf:type ?T) <- (?s ?p ?x) (?p rdfs:domain ?T)"
            , "(?o rdf:type ?T) <- (?s ?p ?o) (?p rdfs:range  ?T)"
            // SubClassOf
            ,"(?s rdf:type ?T) <- (?s rdf:type ?TX )(?TX rdfs:subClassOf ?T)"
            ,"(?t1 rdfs:subClassOf ?t2) <- (?t1 rdfs:subClassOf ?X) (?X rdfs:subClassOf ?t2)"
            // SubPropertyOf
            ,"(?s ?q ?o) <- (?s ?p ?o ) (?p rdfs:subPropertyOf ?q)"
            ,"(?p1 rdfs:subPropertyOf ?p2) <- (?p1 rdfs:subPropertyOf ?X) (?X rdfs:subPropertyOf ?p2)"

            // SubClassOf self
            ,"(?X rdfs:subClassOf ?X) <- (?Y rdfs:subClassOf ?X )"
            ,"(?X rdfs:subClassOf ?X) <- (?X rdfs:subClassOf ?Y )"
            ,"(?X rdfs:subClassOf ?X) <- (?Y rdf:type ?X)"

            // SubPropertyOf self
            ,"(?X rdfs:subPropertyOf ?X) <- (?Y rdfs:subPropertyOf ?X )"
            ,"(?X rdfs:subPropertyOf ?X) <- (?X rdfs:subPropertyOf ?Y )"
        );
    }

    private static RuleSet rules(String ... xs) {
        return RulesParser.rules(xs);
    }

    /** RDFS, without axioms, or subCLass/subproperty of self. */
    public static RuleSet rulesRDFSbasic() {
        return rules(
            ""
            // Domain and range
            ,"(?s rdf:type ?T) <- (?s ?p ?x) (?p rdfs:domain ?T)"
            ,"(?o rdf:type ?T) <- (?s ?p ?o) (?p rdfs:range  ?T)"
            // SubClassOf
            ,"(?s rdf:type ?T) <- (?s rdf:type ?TX )(?TX rdfs:subClassOf ?T)"
            ,"(?t1 rdfs:subClassOf ?t2) <- (?t1 rdfs:subClassOf ?X) (?X rdfs:subClassOf ?t2)"
            // SubPropertyOf
            ,"(?s ?q ?o) <- (?s ?p ?o ) (?p rdfs:subPropertyOf ?q)"
            ,"(?p1 rdfs:subPropertyOf ?p2) <- (?p1 rdfs:subPropertyOf ?X) (?X rdfs:subPropertyOf ?p2)"
        );
    }
}
