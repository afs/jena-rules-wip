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

package org.seaborne.jena.inf_rdfs.zero;

import static org.seaborne.jena.inf_rdfs.engine.InfGlobal.rdfType;
import static org.seaborne.jena.inf_rdfs.engine.InfGlobal.rdfsDomain;
import static org.seaborne.jena.inf_rdfs.engine.InfGlobal.rdfsRange;
import static org.seaborne.jena.inf_rdfs.engine.InfGlobal.rdfsSubClassOf;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.SingletonIterator;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.seaborne.jena.inf_rdfs.engine.MatchGraph;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_Node;

public class Find3_Graph_0 implements MatchGraph<Node, Triple> {
    private final Graph graph;
    private final SetupRDFS_Node setup;

    public Find3_Graph_0(SetupRDFS_Node setup, Graph graph) {
        this.setup = setup;
        this.graph = graph;
    }

    @Override
    public Stream<Triple> match(Node s, Node p, Node o) {
        // Quick route to conversion.
        return find2(s,p,o);
    }

    // Iter?
    protected Stream<Triple> sourceFind(Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = graph.find(s,p,o);
        Stream<Triple> stream = Iter.asStream(iter);
        stream.onClose(()->iter.close());
        return stream;
    }

    private Stream<Triple> find2(Node _subject, Node _predicate, Node _object) {
        //log.info("find("+_subject+", "+_predicate+", "+_object+")");
        Node subject = any(_subject);
        Node predicate = any(_predicate);
        Node object = any(_object);
        // Subproperties of rdf:type

        // ?? rdf:type ??
        if ( rdfType.equals(predicate) ) {
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_type_T(subject, object);
                else
                    return find_X_type_ANY(subject);
            } else {
                if ( isTerm(object) )
                    return find_ANY_type_T(object);
                else
                    return find_ANY_type_ANY();
            }
        }

        // ?? ANY ??
        if ( isANY(predicate) ) {
            if ( isTerm(subject) ) {
                if ( isTerm(object) )
                    return find_X_ANY_Y(subject, object);
                else
                    return find_X_ANY_ANY(subject);
            } else {
                if ( isTerm(object) )
                    return find_ANY_ANY_T(object);
                else
                    return find_ANY_ANY_ANY();
            }
        }

        // ?? term ??
        return find_subproperty(subject, predicate, object);
    }

    private Stream<Triple> find_subproperty(Node subject, Node predicate, Node object) {
        // Find with subproperty
        // We assume no subproperty of rdf:type

        Set<Node> predicates = setup.getSubProperties(predicate);
        if ( predicates == null || predicates.isEmpty() )
            return sourceFind(subject, predicate, object);
        // Hard work, not scalable.
        // Don't forget predicate itself!
        // XXX Rewrite
        Stream<Triple> triples = sourceFind(subject, predicate, object);
        for ( Node p : predicates ) {
            Stream<Triple> stream = sourceFind(subject, p, object);
            stream = stream.map(triple -> Triple.create(triple.getSubject(), predicate, triple.getObject()) );
            triples = Stream.concat(triples, stream);
        }
        return triples;
    }

    private Iterator<Triple> singletonIterator(Node s, Node p, Node o) {
        return new SingletonIterator<>(Triple.create(s, p, o));
    }

    private Iterator<Triple> nullIterator() {
        return new NullIterator<>();
    }

    private Stream<Triple> find_X_type_T(Node subject, Node object) {
        // XXX Check if accumulation is the correct approach
        // Don't need to accumate. Find on insertion.
        if (graph.contains(subject, rdfType, object) )
            return Stream.of(Triple.create(subject, rdfType, object));
        Set<Node> types = new HashSet<>();
        accTypesRange(types, subject);
        if ( types.contains(object) )
            return Stream.of(Triple.create(subject, rdfType, object));
        accTypesDomain(types, subject);
        if ( types.contains(object) )
            return Stream.of(Triple.create(subject, rdfType, object));
        accTypes(types, subject);
        // expand supertypes
        types = superTypes(types);
        if ( types.contains(object) )
            return Stream.of(Triple.create(subject, rdfType, object));
        return Stream.empty();
    }

    private Stream<Triple> find_X_type_ANY(Node subject) {
        // XXX Check if accumulation is the correct approach
        Set<Node> types = new HashSet<>();
        accTypesRange(types, subject);
        accTypesDomain(types, subject);
        accTypes(types, subject);
        // expand supertypes
        types = superTypes(types);
        return types.stream().map( type -> Triple.create(subject, rdfType, type));
    }

    private Stream<Triple> find_ANY_type_T(Node type) {
        // Fast path no subClasses
        Set<Node> types = subTypes(type);
        Set<Triple> triples = new HashSet<>();
        accInstances(triples, types, type);
        accInstancesRange(triples, types, type);
        accInstancesDomain(triples, types, type);
        return triples.stream();
    }

    private Stream<Triple> find_ANY_type_ANY() {
        // Better?
        // Duplicates?
        // XXX InferenceProcessorStreamRDFS as a stream
        Stream<Triple> stream = sourceFind(Node.ANY, Node.ANY, Node.ANY);
        stream = infFilter(stream, null, rdfType, null);
        return stream;
    }

    private Stream<Triple> find_X_ANY_Y(Node subject, Node object) {
        // Start at X.
        // (X ? ?) - inference - project "X ? T"
        // also (? ? X) if there is a range clause.
        Stream<Triple> stream = sourceFind(subject, Node.ANY, Node.ANY);
        // + reverse (used in object position and there is a range clause)
        // domain was taken care of above.
        if ( setup.hasRangeDeclarations() )
            stream = Stream.concat(stream, sourceFind(Node.ANY, Node.ANY, subject));
        return infFilter(stream, subject, Node.ANY, object);
    }

    private Stream<Triple> find_X_ANY_ANY(Node subject) {
        // Can we do better?
        return find_X_ANY_Y(subject, Node.ANY);
    }

    private Stream<Triple> find_ANY_ANY_T(Node object) {
        Stream<Triple> stream = sourceFind(Node.ANY, Node.ANY, object);
        stream = stream.filter( triple -> ! triple.getPredicate().equals(rdfType)) ;
        // and get via inference.
        // Exclude rdf:type and do by inference?
        stream = Stream.concat(stream, find_ANY_type_T(object));
        // ? ? P (range) does not find :x a :P when :P is a class
        // and "some p range P"
        // Include from setup?
        boolean ensureDistinct = false;
        if ( setup.includeDerivedDataRDFS() ) {
            // These cause duplicates.
            ensureDistinct = true;
            stream = Stream.concat(stream, sourceFind(Node.ANY, rdfsRange, object));
            stream = Stream.concat(stream, sourceFind(Node.ANY, rdfsDomain, object));
            stream = Stream.concat(stream, sourceFind(Node.ANY, rdfsRange, object));
            stream = Stream.concat(stream, sourceFind(object, rdfsSubClassOf, Node.ANY));
            stream = Stream.concat(stream, sourceFind(Node.ANY, rdfsSubClassOf, object));
        }
        return infFilter(stream, Node.ANY, Node.ANY, object).distinct();
    }

    //    private static <X> Iterator<X> distinct(Iterator<X> iter) {
    //        return Iter.distinct(iter);
    //    }

    private Stream<Triple> find_ANY_ANY_ANY() {
        Stream<Triple> stream = sourceFind(Node.ANY, Node.ANY, Node.ANY);
        stream = inf(stream);
        if ( setup.includeDerivedDataRDFS() )
            stream = stream.distinct();
        return stream;
    }

    private Stream<Triple> infFilter(Stream<Triple> stream, Node subject, Node predicate, Node object) {
        // XXX Rewrite ??
        stream = inf(stream);
        if ( isTerm(predicate) )
            stream = stream.filter(triple -> { return triple.getPredicate().equals(predicate) ; } );
        if ( isTerm(object) )
            stream = stream.filter(triple -> { return triple.getObject().equals(object) ; } );
        if ( isTerm(subject) )
            stream = stream.filter(triple -> { return triple.getSubject().equals(subject) ; } );
        return stream;
    }

    private final ApplyRDFS_0 engine(List<Triple> acc) {
        return new ApplyRDFS_0(setup, t->acc.add(t));
    }

    // Unlike LibInf.process, this is a fixed function create once when the class is instantiated.
    private final Function<Triple, Stream<Triple>> applyInf = triple-> {
        List<Triple> x = new ArrayList<>();
        engine(x).process(triple);
        return x.stream();
    };

    private Stream<Triple> inf(Stream<Triple> stream) {
        return stream.flatMap(applyInf::apply);
    }

    // XXX Rewrite ?? "Set" might mean this is best, as is materialization.
    private void accInstances(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            Stream<Triple> stream = sourceFind(Node.ANY, rdfType, type);
            stream.forEach(triple -> triples.add(Triple.create(triple.getSubject(), rdfType, requestedType)) );
        }
    }

    private void accInstancesDomain(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            Set<Node> predicates = setup.getPropertiesByDomain(type);
            if ( predicates == null )
                continue;
            predicates.forEach(p -> {
                Stream<Triple> stream = sourceFind(Node.ANY, p, Node.ANY);
                stream.forEach(triple -> triples.add(Triple.create(triple.getSubject(), rdfType, requestedType)) );
            });
        }
    }

    private void accInstancesRange(Set<Triple> triples, Set<Node> types, Node requestedType) {
        for ( Node type : types ) {
            Set<Node> predicates = setup.getPropertiesByRange(type);
            if ( predicates == null )
                continue;
            predicates.forEach(p -> {
                Stream<Triple> stream = sourceFind(Node.ANY, p, Node.ANY);
                stream.forEach(triple -> triples.add(Triple.create(triple.getObject(), rdfType, requestedType)) );
            });
        }
    }

    private void accTypes(Set<Node> types, Node subject) {
        Stream<Triple> stream = sourceFind(subject, rdfType, Node.ANY);
        stream.forEach(triple -> types.add(triple.getObject()));
    }

    private void accTypesDomain(Set<Node> types, Node node) {
        Stream<Triple> stream = sourceFind(node, Node.ANY, Node.ANY);
        stream.forEach(triple -> {
            Node p = triple.getPredicate();
            Set<Node> x = setup.getDomain(p);
            types.addAll(x);
        });
    }

    private void accTypesRange(Set<Node> types, Node node) {
       Stream<Triple> stream = sourceFind(Node.ANY, Node.ANY, node);
        stream.forEach(triple -> {
            Node p = triple.getPredicate();
            Set<Node> x = setup.getRange(p);
            types.addAll(x);
        });
    }

    private Set<Node> subTypes(Set<Node> types) {
        Set<Node> x = new HashSet<>();
        for ( Node type : types ) {
            Set<Node> y = setup.getSubClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    private Set<Node> superTypes(Set<Node> types) {
        Set<Node> x = new HashSet<>();
        for ( Node type : types ) {
            Set<Node> y = setup.getSuperClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    private Set<Node> subTypes(Node type) {
        Set<Node> x = new HashSet<>();
        x.add(type);
        Set<Node> y = setup.getSubClasses(type);
        x.addAll(y);
        return x;
    }

    private Set<Node> superTypes(Node type) {
        Set<Node> x = new HashSet<>();
        x.add(type);
        Set<Node> y = setup.getSuperClasses(type);
        x.addAll(y);
        return x;
    }


    private static Node any(Node node) {
        return ( node == null ) ? Node.ANY : node;
    }

    private static boolean isANY(Node node) {
        return ( node == null ) || Node.ANY.equals(node);
    }

    private static boolean isTerm(Node node) {
        return ( node != null ) && ! Node.ANY.equals(node);
    }
}
