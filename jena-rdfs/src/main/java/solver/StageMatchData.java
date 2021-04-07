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

package solver;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;

/**
 * This is the data access step: RX.matchData/SolverRX.matchQuadPattern
 */
public class StageMatchData {

    // Positions in Tuple4/Quad
    private static int QG = 0 ;
    private static int QS = 1 ;
    private static int QP = 2 ;
    private static int QO = 3 ;

    private static Function<Quad, Quad> quadsToUnion =
            quad -> Quad.create(Quad.unionGraph, quad.getSubject(), quad.getPredicate(), quad.getObject());

    /* Entry point from PattenMatchData.
     *   graphNode may be Node.ANY, meaning union graph and should make triples unique.
     *   graphNode may be null, meaning default graph
     */
    static Iterator<Binding> access(Iterator<Binding> input, Node graphName, Triple pattern, Predicate<Quad> filter, boolean anyGraph, ExecutionContext execCxt) {
        return Iter.flatMap(input, binding -> {
            return access(binding, graphName, pattern, filter, anyGraph, execCxt);
        });
    }

    static Iterator<Binding> access(Binding binding, Node graphName, Triple pattern, Predicate<Quad> filter, boolean anyGraph, ExecutionContext execCxt) {
        // Assumes if anyGraph, then graphName == null.
        // graphName == Quad.defaultgraphURI for triples.
        Node g = graphName;
        Node s = pattern.getSubject();
        Node p = pattern.getPredicate();
        Node o = pattern.getObject();

        Node[] matchConst = new Node[4];
        Var[] vars = new Var[4];

        boolean b = prepareQuad(binding, graphName, pattern, matchConst, vars);
        if ( !b )
            return Iter.nullIterator();

        Node gm = matchConst[QG];
        Node sm = matchConst[QS];
        Node pm = matchConst[QP];
        Node om = matchConst[QO];

        DatasetGraph dsg = execCxt.getDataset();

        // Union -> findNG - do not include default graph.
        Iterator<Quad> iterMatches = anyGraph
                ? dsg.findNG(gm, sm, pm, om)
                : dsg.find(gm, sm, pm, om) ;

        if ( false ) {
            // Debug
            List<Quad> x = Iter.toList(iterMatches);
            System.out.println(x);
            iterMatches = x.iterator();
        }

        // ** Allow a triple or quad filter here.
        if ( filter != null )
            iterMatches = Iter.filter(iterMatches, filter);

        // Rewrite to union is anyGraph
        // If we want to reduce to RDF semantics over quads,
        // we need to reduce the quads to unique triples.
        // We do that by having the graph slot as "any", then running
        // through a distinct-ifier.
        // Assumes quads are GSPO - zaps the first slot.
        // Assumes that tuples are not shared.
        if ( anyGraph ) {
            iterMatches = Iter.map(iterMatches, quadsToUnion);
            // Guaranteed
            iterMatches = Iter.distinct(iterMatches);

            // This depends on the way indexes are chosen and
            // the indexing pattern. It assumes that the index
            // chosen ends in G so same triples are adjacent
            // in a union query.
            //
            // If any slot is defined, then the index will be X??G.
            // If no slot is defined, then the index will be ???G.
            // Must be guaranteed.
            //
            //iterMatches = Iter.distinctAdjacent(iterMatches);
        }

        BindingBuilder bindingBuilder = BindingFactory.builder(binding);

        Function<Quad, Binding> binder = quad -> quadToBinding(bindingBuilder, quad, matchConst, vars);
        return Iter.iter(iterMatches).map(binder).removeNulls();
    }

    private static Binding quadToBinding(BindingBuilder bindingBuilder, Quad quad, Node[] matchConst, Var[] vars) {
        bindingBuilder.reset();
        if ( ! slot(bindingBuilder, QG, quad.getGraph(), vars) )
            return null;
        if ( ! slot(bindingBuilder, QS, quad.getSubject(), vars) )
            return null;
        if ( ! slot(bindingBuilder, QP, quad.getPredicate(), vars) )
            return null;
        if ( ! slot(bindingBuilder, QO, quad.getObject(), vars) )
            return null;
        return bindingBuilder.build();
    }

    private static boolean slot(BindingBuilder bindingBuilder, int i, Node value, Var[] vars) {
          Var var = vars[i];
          if ( var == null )
              return true;
          if ( ! compatible(bindingBuilder, var, value))
              return false;
          bindingBuilder.add(var, value);
          return true;
    }

    private static boolean prepareQuad(Binding binding, Node graphName, Triple pattern, Node[] matchConst, Var[] vars) {
        prepare(QG, binding, graphName, matchConst, vars);
        prepare(QS, binding, pattern.getSubject(), matchConst, vars);
        prepare(QP, binding, pattern.getPredicate(), matchConst, vars);
        prepare(QO, binding, pattern.getObject(), matchConst, vars);
        return true;
    }

    private static void prepare(int i, Binding binding, Node n, Node[] matchConst, Var[] vars) {
        // Substitution
        // Variables unsubstituted are n
        n = substituteFlat(n, binding);
        if ( Var.isVar(n) )
            vars[i] = Var.alloc(n);
        else
            matchConst[i] = n;
    }

    // Compatible: new variable or sameTerm as existing binding.
    private static boolean compatible(BindingBuilder output, Var var, Node value) {
        Node x = output.get(var);
        if ( x == null )
            return true;
        if ( sameTermAs(x, value) )
            return true;
        return false;
    }

    /** Test equality of two concrete teams. */
    private static boolean sameTermAs(Node node1, Node node2) {
        return NodeFunctions.sameTerm(node1, node2);
    }

    // Variable or not a variable. Not <<?var>>
    private static Node substituteFlat(Node n, Binding binding) {
        return Var.lookup(binding::get, n);
    }
}
