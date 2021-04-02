/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.seaborne.jena.inf_rdfs;

import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;
import org.apache.jena.sparql.core.Quad;
import org.seaborne.jena.inf_rdfs.engine.ApplyRDFS;
import org.seaborne.jena.inf_rdfs.engine.Mappers;
import org.seaborne.jena.inf_rdfs.engine.Output;

/**
 * A {@link StreamRDF} that applies RDFS to the stream.
 * <p>
 * It receive triples and quads (incoming because this is a {@link StreamRDF}),
 * applies RDFS,
 * and outputs to the StreamRDF provided.
 * The output stream may include duplicates.
 */
public class InfStreamRDFS extends StreamRDFWrapper {
    private final SetupRDFS<Node>     rdfsSetup;
    private final ApplyRDFS<Node, Triple> rdfs;
    private final Output<Node> outputTriple;
    private final boolean includeInput = true;

    private Node currentGraph;
    private Output<Node> currentGraphOutput;
    private ApplyRDFS<Node, Quad> rdfsQuad = null;

    public InfStreamRDFS(final StreamRDF output, SetupRDFS<Node> rdfsSetup) {
        super(output);
        this.rdfsSetup = rdfsSetup;
        outputTriple = (s,p,o)->output.triple(Triple.create(s,p,o));
        this.rdfs = new ApplyRDFS<>(rdfsSetup, Mappers.mapperTriple());
    }

    /** Triple output function. Send to StreamRDF. */
    private final Consumer<Triple> dest = triple -> {
        if ( currentGraph == null )
            super.triple(triple);
        else
            super.quad(Quad.create(currentGraph, triple));
    };
    @Override
    public void triple(Triple triple) {
        if ( includeInput )
            super.triple(triple);
        rdfs.infer(triple.getSubject(), triple.getPredicate(), triple.getObject(), outputTriple);
    }

    @Override
    public void quad(Quad quad) {
        if ( includeInput )
            super.quad(quad);
        if ( currentGraph != quad.getGraph() ) {
            currentGraph = quad.getGraph();
            currentGraphOutput = (s,p,o)->Quad.create(currentGraph, s,p,o);
        }
        rdfs.infer(quad.getSubject(), quad.getPredicate(), quad.getObject(), currentGraphOutput);
    }
}
