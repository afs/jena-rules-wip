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

package dev_shacl;

public class NotesShaclRules {
    // Architecture
    //  Own datastructure for "template-head", body : triples block, filters[, bind]

    // Execution : No rule checking.

    // Rule parsing - use PrefixMap not Prologue

    // [x] Writer
    // [ ] Improve writer.

    // Triple generation and parse triples.
    // Split project? Same repo
    //   Common POM parent?
    // Intermediate - GraphRelStore = graph and relstore

    // "Rule" class - not parser.

    // Grammar
    // [x] TriplesTemplateBlock -- "{" TriplesTemplate(acc) "}"
    // [ ] Reverse the name of ParserShaclRules and ShaclRulesParser
    //     or ShaclRules.parser


    // [ ] IMPORT
    // [x] DATA
    // [ ] TRANSITIVE, SYMMETRIC, INVERSE_OF

}
