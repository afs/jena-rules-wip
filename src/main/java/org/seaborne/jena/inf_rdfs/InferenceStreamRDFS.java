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

import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWrapper;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.seaborne.jena.inf_rdfs.engine.InferenceEngineRDFS;
import org.seaborne.jena.inf_rdfs.engine.InferenceSetupRDFS;
import org.seaborne.jena.inf_rdfs.engine.StreamTriple;

/** StreamRDF that applies RDFS to the stream.
 * 
 * Receive triples and quads (incoming because this is a StreamRDF); 
 * apply RDFS;
 * output to the StreamRDF provided.
 * Stream output may include duplicates.
 */
public class InferenceStreamRDFS extends StreamRDFWrapper {
    private final InferenceSetupRDFS  rdfsSetup;
    private final InferenceEngineRDFS rdfs;
    private Node                      currentGraph;

    public InferenceStreamRDFS(final StreamRDF output, InferenceSetupRDFS rdfsSetup) {
        super(output);
        this.rdfsSetup = rdfsSetup;
        this.rdfs = new InferenceEngineRDFS(rdfsSetup, dest);
    }

    private final StreamTriple dest = triple -> {
        if ( currentGraph == null )
            super.triple(triple);
        else
            super.quad(Quad.create(currentGraph, triple));
    };

    @Override
    public void triple(Triple triple) {
        super.triple(triple);
        currentGraph = null;
        rdfs.process(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
    public void quad(Quad quad) {
        super.quad(quad);
        currentGraph = quad.getGraph();
        rdfs.process(quad.getSubject(), quad.getPredicate(), quad.getObject());
        currentGraph = null;
    }
}
