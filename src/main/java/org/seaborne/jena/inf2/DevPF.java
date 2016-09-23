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

import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.query.* ;
import org.apache.jena.sparql.algebra.Algebra ;
import org.apache.jena.sparql.algebra.Op ;
import org.apache.jena.sparql.pfunction.PropertyFunctionFactory ;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry ;
import org.apache.jena.sparql.sse.SSE ;
import org.apache.jena.sparql.util.QueryExecUtils ;
import org.apache.jena.system.JenaSystem ;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;
import org.seaborne.jena.inf2.PFbyTable.Table ;

public class DevPF {
    public static void main(String...argv) {
        //Datalog : Single head clause
        //  All variables in a negation in the body must also be positively mentioned in the body.
        //  All variables in an artimetic relationship must be in the body as solveable sterms.
        
        // PropertyFunctionGenerator.buildPropertyFunctions
        //    magicProperty needs to be careful about rdf:rest! 
        
        // Version without recursion
        //   Recursion via SPARQL only over the base data.
        
        // T0 <- T1, T2, ...  SPATQL, restricted.
        // T  <- SPARQL 
        
        JenaSystem.init();
        Table table = new PFbyTable.Table() ;
        
        add(table, ":s1", ":o1") ;
        add(table, ":s1", ":o2") ;
        add(table, ":s2", ":o1") ;
        add(table, ":s2", ":o2") ;
        add(table, ":s2", ":o3") ;
        
        Dataset ds = DatasetFactory.create() ;
        
        PropertyFunctionFactory pff = (uri)->new PFbyTable() ;
        // rdf:rest is special to property functions!  
        String iri = "http://example/trans" ;
        PFbyTable.addTable(NodeFactory.createURI(iri), table);
        PropertyFunctionRegistry.get().put(iri, pff) ;
        
        String x = StrUtils.strjoinNL
            ("PREFIX : <http://example/>"
            ,"PREFIX rdf: <"+RDF.getURI()+">"
            ,"PREFIX rdfs: <"+RDFS.getURI()+">"
            //,"SELECT * { ?s ?p ?o . ?s rdfs:member :o1 . ?s rdf:rest :o1 }"
            //,"SELECT * { ?s rdfs:member :o1 }"
            ,"SELECT * { { ?s :trans :o1 } UNION { :s2 :trans ?o }  UNION { ?X :trans ?Y } }"
            ) ;
        Query query = QueryFactory.create(x) ;
        System.out.println(query); 
        
        Op op = Algebra.compile(query) ;
        Op op1 = Algebra.optimize(op) ;
        System.out.println(op1);
        SSE.write(op1); 
        
        QueryExecution qExec = QueryExecutionFactory.create(query, ds) ;
        QueryExecUtils.executeQuery(qExec);
    }
    
    static void add(Table table, String s, String o) {
        table.add(SSE.parseNode(s), SSE.parseNode(o)) ;
    }
}
