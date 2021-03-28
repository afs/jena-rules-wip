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

package org.seaborne.jena.inf_rdfs.zero;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.seaborne.jena.inf_rdfs.SetupRDFS;
import org.seaborne.jena.inf_rdfs.engine.ApplyRDFS;
import org.seaborne.jena.inf_rdfs.engine.Mappers;

/**
 * Apply RDFS to a triple and send output to a destination {@code Consumer<Triple>}.
 * This is the core of stream processing for RDFS.
 * This is inference on the A-Box (the data) with respect to a fixed T-Box
 * (the vocabulary, ontology).
 * <p>
 * This class implements:
 * <ul>
 * <li>rdfs:subClassOf (transitive)</li>
 * <li>rdfs:subPropertyOf (transitive)</li>
 * <li>rdfs:domain</li>
 * <li>rdfs:range</li>
 * </ul>
 * Usage: {@link #process(Node, Node, Node)}, which calls an action ({@code Consumer<Triple>})
 */

class ApplyRDFS_0 {

    private final Consumer<Triple> delivery;
    private final ApplyRDFS<Node, Triple> inf;

    public ApplyRDFS_0(SetupRDFS<Node> setup, Consumer<Triple> delivery) {
        this.delivery = Objects.requireNonNull(delivery);
        ApplyRDFS.Output<Node> proc = (s,p,o)->delivery.accept(Triple.create(s,p,o));
        this.inf = new ApplyRDFS<>(setup, Mappers.mapperNode, proc, proc);
    }

    /** Apply RDFS rules based on the triple, including output of the triple itself. */
    public void process(Triple triple) {
        inf.process(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    public void process(Node s, Node p, Node o) {
        inf.process(s, p, o);
    }

    /** Apply RDFS rules based on the triple; do not include the triple itself. */
    public void infer(Triple triple) {
        infer(triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    /** Apply RDFS rules based on the triple; do not include the triple itself. */
    public void infer(Node s, Node p, Node o) {
        inf.infer(s, p, o);
    }
}

