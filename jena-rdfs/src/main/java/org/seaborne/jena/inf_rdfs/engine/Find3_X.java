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

import org.apache.jena.atlas.lib.tuple.Tuple;

/** WIP - Generalised */
public abstract class Find3_X<T,X> implements StreamGraph<Tuple<X>, X> {


// THINK
// Attempt to refactor to work in X space.
//   Issues: All the constants in X terms -> add to setup. (Missing from data -> not in X space?)
// InferenceEngineRDFS isin Node space.
//
//    private final StreamGraph<Tuple<X>, X> graph;
//    private final SetupRDFS<X> setup;
//    //private final InferenceEngineRDFS engine;
//    //private final Function<Triple, Stream<Tuple<X>>> engine;
//    private final boolean includeDerivedDataRDFS;
//
//    public Find3_X(SetupRDFS<X> setup, StreamGraph<Tuple<X>, X> base, boolean includeDerivedDataRDFS) {
//        this.setup = setup;
//        this.graph = base;
//        this.includeDerivedDataRDFS = includeDerivedDataRDFS;
//    }
//
//    @Override
//    public Stream<Tuple<X>> find(X s, X p, X o) {
//        // Quick route to conversion.
//        return find2(s,p,o);
//    }
//
//    private static <X> X ANY() { return null; }
//
//    // Add to setup.
//    private static <X> X rdfType()              { return null; } // XXX
//    private static <X> X rdfsDomain()           { return null; }
//    private static <X> X rdfsRange()            { return null; }
//    private static <X> X rdfsSubClassOf()       { return null; }
//    private static <X> X rdfsSubPropertyOf()    { return null; }
//
//    // Iter?
//    protected abstract Stream<Tuple<X>> sourceFind(X s, X p, X o);
//    protected boolean sourceContains(X s, X p, X o) {
//        return sourceFind(s,p,o).findFirst().isPresent();
//    }
//
//    private Stream<Tuple<X>> find2(X _subject, X _predicate, X _object) {
//        //log.info("find("+_subject+", "+_predicate+", "+_object+")");
//        X subject = any(_subject);
//        X predicate = any(_predicate);
//        X object = any(_object);
//
//        // Subproperties of rdf:type
//
//        // ?? rdf:type ??
//        if ( rdfType().equals(predicate) ) {
//            if ( isTerm(subject) ) {
//                if ( isTerm(object) )
//                    return find_X_type_T(subject, object);
//                else
//                    return find_X_type_ANY(subject);
//            } else {
//                if ( isTerm(object) )
//                    return find_ANY_type_T(object);
//                else
//                    return find_ANY_type_ANY();
//            }
//        }
//
//        // ?? ANY ??
//        if ( isANY(predicate) ) {
//            if ( isTerm(subject) ) {
//                if ( isTerm(object) )
//                    return find_X_ANY_Y(subject, object);
//                else
//                    return find_X_ANY_ANY(subject);
//            } else {
//                if ( isTerm(object) )
//                    return find_ANY_ANY_T(object);
//                else
//                    return find_ANY_ANY_ANY();
//            }
//        }
//
//        // ?? term ??
//        return find_subproperty(subject, predicate, object);
//    }
//
//    private Stream<Tuple<X>> find_subproperty(X subject, X predicate, X object) {
//        // Find with subproperty
//        // We assume no subproperty of rdf:type
//        Set<X> predicates = setup.getSubProperties(predicate);
//        if ( predicates == null || predicates.isEmpty() )
//            return sourceFind(subject, predicate, object);
//
//        // Hard work, not scalable.
//        // Don't forget predicate itself!
//        // XXX Rewrite
//        Stream<Tuple<X>> triples = sourceFind(subject, predicate, object);
//
//        for ( X p : predicates ) {
//            Stream<Tuple<X>> stream = sourceFind(subject, p, object);
//            triples = Stream.concat(triples, stream);
//        }
//        return triples;
//    }
//
//    private Iterator<Tuple<X>> singletonIterator(X s, X p, X o) {
//        return new SingletonIterator<>(TupleFactory.create3(s, p, o));
//    }
//
//    private Iterator<Tuple<X>> nullIterator() {
//        return new NullIterator<>();
//    }
//
//    private Stream<Tuple<X>> find_X_type_T(X subject, X object) {
//        // XXX Check if accumulation is the correct approach
//        // Don't need to accumate. Find on insertion.
//        if (sourceContains(subject, rdfType(), object) )
//            return Stream.of(TupleFactory.create3(subject, rdfType(), object));
//        Set<X> types = new HashSet<>();
//        accTypesRange(types, subject);
//        if ( types.contains(object) )
//            return Stream.of(TupleFactory.create3(subject, rdfType(), object));
//        accTypesDomain(types, subject);
//        if ( types.contains(object) )
//            return Stream.of(TupleFactory.create3(subject, rdfType(), object));
//        accTypes(types, subject);
//        // expand supertypes
//        types = superTypes(types);
//        if ( types.contains(object) )
//            return Stream.of(TupleFactory.create3(subject, rdfType(), object));
//        return Stream.empty();
//    }
//
//    private Stream<Tuple<X>> find_X_type_ANY(X subject) {
//        // XXX Check if accumulation is the correct approach
//        Set<X> types = new HashSet<>();
//        accTypesRange(types, subject);
//        accTypesDomain(types, subject);
//        accTypes(types, subject);
//        // expand supertypes
//        types = superTypes(types);
//        return types.stream().map( type -> TupleFactory.create3(subject, rdfType(), type));
//    }
//
//    private Stream<Tuple<X>> find_ANY_type_T(X type) {
//        // Fast path no subClasses
//        Set<X> types = subTypes(type);
//        Set<Tuple<X>> triples = new HashSet<>();
//
//        accInstances(triples, types, type);
//        accInstancesRange(triples, types, type);
//        accInstancesDomain(triples, types, type);
//        return triples.stream();
//    }
//
//    private Stream<Tuple<X>> find_ANY_type_ANY() {
//        // Better?
//        // Duplicates?
//        // XXX InferenceProcessorStreamRDFS as a stream
//        Stream<Tuple<X>> stream = sourceFind(ANY(), ANY(), ANY());
//        stream = infFilter(stream, null, rdfType(), null);
//        return stream;
//    }
//
//    private Stream<Tuple<X>> find_X_ANY_Y(X subject, X object) {
//        // Start at X.
//        // (X ? ?) - inference - project "X ? T"
//        // also (? ? X) if there is a range clause.
//        Stream<Tuple<X>> stream = sourceFind(subject, ANY(), ANY());
//        // + reverse (used in object position and there is a range clause)
//        // domain was taken care of above.
//        if ( setup.hasRangeDeclarations() )
//            stream = Stream.concat(stream, sourceFind(ANY(), ANY(), subject));
//        return infFilter(stream, subject, ANY(), object);
//    }
//
//    private Stream<Tuple<X>> find_X_ANY_ANY(X subject) {
//        // Can we do better?
//        return find_X_ANY_Y(subject, ANY());
//    }
//
//    private Stream<Tuple<X>> find_ANY_ANY_T(X object) {
//        Stream<Tuple<X>> stream = sourceFind(ANY(), ANY(), object);
//        stream = stream.filter( triple -> ! predicate(triple).equals(rdfType())) ;
//        // and get via inference.
//        // Exclude rdf:type and do by inference?
//        stream = Stream.concat(stream, find_ANY_type_T(object));
//
//        // ? ? P (range) does not find :x a :P when :P is a class
//        // and "some p range P"
//        // Include from setup?
//        boolean ensureDistinct = false;
//        if ( includeDerivedDataRDFS ) {
//            // These cause duplicates.
//            ensureDistinct = true;
//            stream = Stream.concat(stream, sourceFind(ANY(), rdfsRange(), object));
//            stream = Stream.concat(stream, sourceFind(ANY(), rdfsDomain(), object));
//            //stream = Stream.concat(stream, sourceFind(ANY(), rdfsRange, object));
//            stream = Stream.concat(stream, sourceFind(object, rdfsSubClassOf(), ANY()));
//            stream = Stream.concat(stream, sourceFind(ANY(), rdfsSubClassOf(), object));
//        }
//        return infFilter(stream, ANY(), ANY(), object).distinct();
//    }
//
//    //    private static <X> Iterator<X> distinct(Iterator<X> iter) {
//    //        return Iter.distinct(iter);
//    //    }
//
//    private Stream<Tuple<X>> find_ANY_ANY_ANY() {
//        Stream<Tuple<X>> stream = sourceFind(ANY(), ANY(), ANY());
//        // XXX Rewrite
//        //engine.process
//        // sequential.
//        stream = inf(stream);
//        if ( includeDerivedDataRDFS )
//            stream = stream.distinct();
//        return stream;
//    }
//
//    private Stream<Tuple<X>> infFilter(Stream<Tuple<X>> stream, X subject, X predicate, X object) {
//        // XXX Rewrite ??
//        stream = inf(stream);
//        if ( isTerm(predicate) )
//            stream = stream.filter(triple -> { return predicate(triple).equals(predicate) ; } );
//        if ( isTerm(object) )
//            stream = stream.filter(triple -> { return object(triple).equals(object) ; } );
//        if ( isTerm(subject) )
//            stream = stream.filter(triple -> { return subject(triple).equals(subject) ; } );
//        return stream;
//    }
//
//    // Work round the fact can't write "setup" directly into applyInf (
//    private final InferenceEngineRDFS engine(List<Tuple<X>> acc) {
//        return LibInf.engine(setup, acc);
//    }
//
//    // Unlike LibInf.process, this is a fixed function create once when the class is instantiated.
//    private final Function<Tuple<X>, Stream<Tuple<X>>> applyInf = triple-> {
//        List<Tuple<X>> x = new ArrayList<>();
//        engine(x).process(triple);
//        return x.stream();
//    };
//
//    private Stream<Tuple<X>> inf(Stream<Tuple<X>> stream) {
//        return stream.flatMap(applyInf::apply);
//    }
//
//    // XXX Rewrite ?? "Set" might mean this is best, as is materialization.
//    private void accInstances(Set<Tuple<X>> triples, Set<X> types, X requestedType) {
//        for ( X type : types ) {
//            Stream<Tuple<X>> stream = sourceFind(ANY(), rdfType(), type);
//            stream.forEach(triple -> triples.add(TupleFactory.create3(subject(triple), rdfType(), requestedType)) );
//        }
//    }
//
//    private void accInstancesDomain(Set<Tuple<X>> triples, Set<X> types, X requestedType) {
//        for ( X type : types ) {
//            Set<X> predicates = setup.getPropertiesByDomain(type);
//            if ( predicates == null )
//                continue;
//            predicates.forEach(p -> {
//                Stream<Tuple<X>> stream = sourceFind(ANY(), p, ANY());
//                stream.forEach(triple -> triples.add(TupleFactory.create3(subject(triple), rdfType(), requestedType)) );
//            });
//        }
//    }
//
//    private void accInstancesRange(Set<Tuple<X>> triples, Set<X> types, X requestedType) {
//        for ( X type : types ) {
//            Set<X> predicates = setup.getPropertiesByRange(type);
//            if ( predicates == null )
//                continue;
//            predicates.forEach(p -> {
//                Stream<Tuple<X>> stream = sourceFind(ANY(), p, ANY());
//                stream.forEach(triple -> triples.add(TupleFactory.create3(object(triple), rdfType(), requestedType)) );
//            });
//        }
//    }
//
//    private void accTypes(Set<X> types, X subject) {
//        Stream<Tuple<X>> stream = sourceFind(subject, rdfType(), ANY());
//        stream.forEach(triple -> types.add(object(triple)));
//    }
//
//    private void accTypesDomain(Set<X> types, X node) {
//        Stream<Tuple<X>> stream = sourceFind(node, ANY(), ANY());
//        stream.forEach(triple -> {
//            X p = predicate(triple);
//            Set<X> x = setup.getDomain(p);
//            types.addAll(x);
//        });
//    }
//
//    private void accTypesRange(Set<X> types, X node) {
//       Stream<Tuple<X>> stream = sourceFind(ANY(), ANY(), node);
//        stream.forEach(triple -> {
//            X p = predicate(triple);
//            Set<X> x = setup.getRange(p);
//            types.addAll(x);
//        });
//    }
//
//    private Set<X> subTypes(Set<X> types) {
//        Set<X> x = new HashSet<>();
//        for ( X type : types ) {
//            Set<X> y = setup.getSubClasses(type);
//            x.addAll(y);
//            x.add(type);
//        }
//        return x;
//    }
//
//    private Set<X> superTypes(Set<X> types) {
//        Set<X> x = new HashSet<>();
//        for ( X type : types ) {
//            Set<X> y = setup.getSuperClasses(type);
//            x.addAll(y);
//            x.add(type);
//        }
//        return x;
//    }
//
//    private Set<X> subTypes(X type) {
//        Set<X> x = new HashSet<>();
//        x.add(type);
//        Set<X> y = setup.getSubClasses(type);
//        x.addAll(y);
//        return x;
//    }
//
//    private Set<X> superTypes(X type) {
//        Set<X> x = new HashSet<>();
//        x.add(type);
//        Set<X> y = setup.getSuperClasses(type);
//        x.addAll(y);
//        return x;
//    }
//
//    // null is ANY
//
//    private static <X> X subject(Tuple<X> tuple) { return tuple.get(0); }
//    private static <X> X predicate(Tuple<X> tuple) { return tuple.get(1); }
//    private static <X> X object(Tuple<X> tuple) { return tuple.get(2); }
//
//    private static <X> X any(X node) {
//        return node;
//    }
//
//    private static <X> boolean isANY(X node) {
//        return ( node == null );
//    }
//
//    private static <X> boolean isTerm(X node) {
//        return ( node != null );
//    }
}