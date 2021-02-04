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

package org.seaborne.jena.inf_rdfs.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.graph.Triple;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_Node;

public class LibInf {
//    /**
//     * Accumulate the triples from processing a triple.
//     * Does not include the original triple (unless that is also inferred).
//     */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Triple triple) {
//        InferenceEngineRDFS engine = new InferenceEngineRDFS(setup, t->acc.add(t));
//        engine.process(triple);
//    }
//
//    /**
//     * Accumulate the triples from processing a triple.
//     * Does not include the original triple (unless that is also inferred).
//     */
//    public static void process(InferenceSetupRDFS setup, Collection<Triple> acc, Node s, Node p, Node o) {
//        InferenceEngineRDFS engine = new InferenceEngineRDFS(setup, t->acc.add(t));
//        engine.process(s, p, o);
//    }

    /**
     * Create an {@link StreamInfEngineRDFS} that accumulates into {@code acc}.
     */
    public static StreamInfEngineRDFS engine(SetupRDFS_Node setup, Collection<Triple> acc) {
        return new StreamInfEngineRDFS(setup, t->acc.add(t));
    }

    /**
     * Create a function that maps a triple to a stream of triples (suitable for {@code Stream.flatMap}).
     * Includes the original triple.
     */
    public static Function<Triple, Stream<Triple>> applyInf(SetupRDFS_Node setup) {
        return triple-> {
            List<Triple> x = new ArrayList<>();
            engine(setup, x).process(triple);
            return x.stream();
        };
    }
}
