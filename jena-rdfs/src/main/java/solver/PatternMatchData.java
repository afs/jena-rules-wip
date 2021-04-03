/*
QueryIterAbortable * Licensed to the Apache Software Foundation (ASF) under one
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

package solver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryException;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.Abortable;
import org.apache.jena.sparql.engine.iterator.QueryIterAbortable;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

/**
 * Match a graph node + basic graph pattern.
 * This "Solver.execute" in Node space.
 */
public class PatternMatchData {
    /** Non-reordering execution of a quad pattern, given an iterator of bindings as input.
     *  GraphNode is Node.ANY for execution over the union of named graphs.
     *  GraphNode is null for execution over the real default graph.
     */
    public static QueryIterator execute(DatasetGraph dsg, Node graphNode, BasicPattern pattern,
                                        QueryIterator input, Predicate<Quad> filter,
                                        ExecutionContext execCxt)
    {
        // Translate:
        //   graphNode may be Node.ANY, meaning we should make triples unique.
        //   graphNode may be null, meaning default graph
        if ( Quad.isUnionGraph(graphNode) )
            graphNode = Node.ANY;
        if ( Quad.isDefaultGraph(graphNode) )
            graphNode = null;

        List<Triple> triples = pattern.getList();
        boolean isDefaultGraph = (graphNode == null);
        boolean anyGraph = isDefaultGraph ? false : (Node.ANY.equals(graphNode));

        int tupleLen = isDefaultGraph ? 3 : 4 ;
        if ( graphNode == null ) {
            if ( 3 != tupleLen )
                throw new QueryException("PatternMatchData: Null graph node but tuples are of length " + tupleLen);
        } else {
            if ( 4 != tupleLen )
                throw new QueryException("PatternMatchData: Graph node specified but tuples are of length " + tupleLen);
        }

        if ( false ) {
            List<Binding> x = Iter.toList(input);
            System.out.println(x);
            input = new QueryIterPlainWrapper(x.iterator());
        }

        Iterator<Binding> chain = input;
        if ( false ) {
            List<Binding> x = Iter.toList(chain);
            System.out.println(x);
            chain = x.iterator();
        }

        List<Abortable> killList = new ArrayList<>();

        for ( Triple triple : triples ) {
            // RDF-star SA
            //chain = RX.rdfStarTriple(chain, pattern, execCxt);

            // [Match] Plain - StageMatchData.access will go into RX as matchData
            chain = StageMatchData.access(chain, graphNode, triple, filter, anyGraph, execCxt);
            chain = SolverLib.makeAbortable(chain, killList);
        }

        // DEBUG POINT
        if ( false ) {
            if ( chain.hasNext() )
                chain = Iter.debug(chain);
            else
                System.out.println("No results");
        }

        // "input" will be closed by QueryIterAbortable but is otherwise unused.
        // "killList" will be aborted on timeout.
        return new QueryIterAbortable(chain, killList, input, execCxt);
    }

//    static Iterator<Binding> matchQuadPattern(Iterator<BindingNodeId> chain, Node graphNode, Triple tPattern,
//                                              NodeTupleTable nodeTupleTable, Tuple<Node> patternTuple,
//                                              boolean anyGraph, Predicate<Tuple<NodeId>> filter, ExecutionContext execCxt) {
//        if ( DATAPATH ) {
//            if ( ! tripleHasNodeTriple(tPattern) || tPattern.isConcrete() ) {
//                // No RDF-star <<>> with variables.
//                return StageMatchTuple.access(nodeTupleTable, chain, patternTuple, filter, anyGraph, execCxt);
//            }
//        }
//
//        // RDF-star <<>> with variables.
//        // This path should work regardless.
//
//        boolean isTriple = (patternTuple.len() == 3);
//        NodeTable nodeTable = nodeTupleTable.getNodeTable();
//
//        Function<BindingNodeId, Iterator<BindingNodeId>> step =
//                bnid -> find(bnid, nodeTupleTable, graphNode, tPattern, anyGraph, filter, execCxt);
//        return Iter.flatMap(chain, step);
//    }
}
