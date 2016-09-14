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

package org.seaborne.jena.inf;

import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.ListUtils ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.sse.SSE ;

public class TestInf {

    Graph base ;
    Graph inf ;
    Graph reference ;
    
    // Check test already present.
    
    // API
    // AbstractTestRDFS > AbstractTestGraphRDFS (separate) > 
    //   Check and make AbstractTestGraphRDFS independent.
    
    // SPARQL
    
    static void compare(String msg, Graph g1, Graph g2, Node s, Node p , Node o) {
        //System.out.printf("find(%s, %s, %s)\n", str(s), str(p), str(o)) ;
        List<Triple> x1 = g1.find(s, p, o).toList() ;
        List<Triple> x2 = g2.find(s, p, o).toList() ;
        boolean b = ListUtils.equalsUnordered(x1, x2) ;
        if ( !b ) {
            System.out.println(msg) ;
            System.out.println("  Different:") ;
            x1.stream().map(SSE::str).forEach(t -> System.out.println("1:  "+t)) ;
            x2.stream().map(SSE::str).forEach(t -> System.out.println("2:  "+t)) ;
        }
        BaseTest.assertEqualsUnordered(x1, x2) ;
    }
}
