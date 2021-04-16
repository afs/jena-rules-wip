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

package org.seaborne.jena.inf_rdfs.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.atlas.lib.StreamOps;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * Constants relating to inference.
 */
public class InfGlobal {

    public static final Node ANY               = Node.ANY;
    public static final Node rdfType           = RDF.Nodes.type;
    public static final Node rdfsRange         = RDFS.Nodes.range;
    public static final Node rdfsDomain        = RDFS.Nodes.domain;
    public static final Node rdfsSubClassOf    = RDFS.Nodes.subClassOf;
    public static final Node rdfsSubPropertyOf = RDFS.Nodes.subPropertyOf;

    private static Set<Node> vocabTerms        = new HashSet<>();
    static {
        vocabTerms.add(rdfsRange);
        vocabTerms.add(rdfsDomain);
        vocabTerms.add(rdfsSubClassOf);
        vocabTerms.add(rdfsSubPropertyOf);
    }

    private static Predicate<Triple> filterRDFS =
        triple -> vocabTerms.contains(triple.getPredicate());

    private static Predicate<Triple> filterNotRDFS = filterRDFS.negate();

    /**
     * Remove triples with a predicate which is RDFS schema vocabulary.
     */
    public static List<Triple> removeRDFS(List<Triple> x) {
        return StreamOps.toList(x.stream().filter(filterNotRDFS));
    }
}
