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

package org.seaborne.jena.shacl_rules;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sys.JenaSystem;

public class DevShaclRules {

    static { JenaSystem.init(); LogCtl.setLogging(); }

    public static void main(String[] args) {

        // Can't BGP-FILTER-BGP-...
        String x = """
                PREFIX : <http://example/>
                RULE { ?S :p :o }
                WHERE {
                    ?S :p ?o
                    FILTER (?o < 2 )
                    FILTER (?o < 2 )
                }
                """;
        ParserShaclRules.parse(x);
        System.out.println("DONE");
    }

}
