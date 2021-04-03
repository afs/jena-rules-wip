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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.seaborne.jena.inf_rdfs.SetupRDFS;

/**
 * Match a 3-tuple, which might have widlcards, applying a fixed set of inference rules.
 * This class is the core machinery of matching data using an RDFS schema.
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
 *
 * @see ApplyRDFS ApplyRDFS for the matching algorithm.
 */
public abstract class MatchRDFS<X, T> extends CxtInf<X, T> {
    private final Function<T,Stream<T>> applyInf;

    public MatchRDFS(SetupRDFS<X> setup, MapperX<X,T> mapper) {
        super(setup, mapper);
        this.applyInf = t-> {
            // Revisit use of applyInf.
            //   used in inf from find_ANY_ANY_ANY
            //   used in inf from infFilter from ANY_ANY_T, ANY_type_ANY, X_ANY_Y
            // [RDFS]  Non-collecting stream engine. Replace?
            List<T> x = new ArrayList<>();
            x.add(t);
            Output<X> dest = (s,p,o) -> x.add(dstCcreate(s,p,o));
            ApplyRDFS<X, T> streamInf = new ApplyRDFS<>(setup, mapper);
            //   process needed but infer+explicit current triple saves a create.
            streamInf.infer(mapper.subject(t), mapper.predicate(t), mapper.object(t), dest);
            return x.stream();
        };
    }

    public Stream<T> match(X s, X p, X o) { return matchWithInf(s, p ,o); }

    // Access data.
    protected abstract boolean sourceContains(X s, X p, X o);
    protected abstract Stream<T> sourceFind(X s, X p, X o);
    protected abstract T dstCcreate(X s, X p, X o);

    protected final X subject(T tuple)        { return mapper.subject(tuple); }
    protected final X predicate(T tuple)      { return mapper.predicate(tuple); }
    protected final X object(T tuple)         { return mapper.object(tuple); }

    private Stream<T> matchWithInf(X _subject, X _predicate, X _object) {
        //log.info("find("+_subject+", "+_predicate+", "+_object+")");
        X subject = any(_subject);
        X predicate = any(_predicate);
        X object = any(_object);
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

    private Stream<T> find_subproperty(X subject, X predicate, X object) {
        // Find with subproperty
        // We assume no subproperty of rdf:type

        Set<X> predicates = setup.getSubProperties(predicate);
        if ( predicates == null || predicates.isEmpty() )
            return sourceFind(subject, predicate, object);
        // Hard work, not scalable.
        // Don't forget predicate itself!
        // XXX Rewrite
        Stream<T> tuples = sourceFind(subject, predicate, object);
        for ( X p : predicates ) {
            Stream<T> stream = sourceFind(subject, p, object);
            // Rewrite with predicate
            stream = stream.map(tuple -> dstCcreate(subject(tuple), predicate, object(tuple)) );
            tuples = Stream.concat(tuples, stream);
        }
        return tuples;
    }

    private Stream<T> find_X_type_T(X subject, X object) {
        if ( sourceContains(subject, rdfType, object) )
            return Stream.of(dstCcreate(subject, rdfType, object));
        Set<X> types = new HashSet<>();
        accTypesRange(types, subject);
        if ( types.contains(object) )
            return Stream.of(dstCcreate(subject, rdfType, object));
        accTypesDomain(types, subject);
        if ( types.contains(object) )
            return Stream.of(dstCcreate(subject, rdfType, object));
        accTypes(types, subject);
        // expand supertypes
        types = superTypes(types);
        if ( types.contains(object) )
            return Stream.of(dstCcreate(subject, rdfType, object));
        return Stream.empty();
    }

    private Stream<T> find_X_type_ANY(X subject) {
        Set<X> types = new HashSet<>();
        accTypesRange(types, subject);
        accTypesDomain(types, subject);
        accTypes(types, subject);
        // expand supertypes
        types = superTypes(types);
        return types.stream().map( type -> dstCcreate(subject, rdfType, type));
    }

    private Stream<T> find_ANY_type_T(X type) {
        // Fast path no subClasses
        Set<X> types = subTypes(type);
        Set<T> tuples = new HashSet<>();
        accInstances(tuples, types, type);
        accInstancesRange(tuples, types, type);
        accInstancesDomain(tuples, types, type);
        return tuples.stream();
    }

    private Stream<T> find_ANY_type_ANY() {
        // XXX Better?
        // Duplicates?
        Stream<T> stream = sourceFind(ANY, ANY, ANY);
        stream = infFilter(stream, null, rdfType, null);
        return stream;
    }

    private Stream<T> find_X_ANY_Y(X subject, X object) {
        // Start at X.
        // (X ? ?) - inference - project "X ? T"
        // also (? ? X) if there is a range clause.
        Stream<T> stream = sourceFind(subject, ANY, ANY);
        // + reverse (used in object position and there is a range clause)
        // domain was taken care of above.
        if ( setup.hasRangeDeclarations() )
            stream = Stream.concat(stream, sourceFind(ANY, ANY, subject));
        return infFilter(stream, subject, ANY, object);
    }

    private Stream<T> find_X_ANY_ANY(X subject) {
        // Can we do better?
        return find_X_ANY_Y(subject, ANY);
    }

    private Stream<T> find_ANY_ANY_T(X object) {
        Stream<T> stream = sourceFind(ANY, ANY, object);
        stream = stream.filter( triple -> ! predicate(triple).equals(rdfType)) ;
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
            stream = Stream.concat(stream, sourceFind(ANY, rdfsRange, object));
            stream = Stream.concat(stream, sourceFind(ANY, rdfsDomain, object));
            stream = Stream.concat(stream, sourceFind(ANY, rdfsRange, object));
            stream = Stream.concat(stream, sourceFind(object, rdfsSubClassOf, ANY));
            stream = Stream.concat(stream, sourceFind(ANY, rdfsSubClassOf, object));
        }
        stream = infFilter(stream, ANY, ANY, object);

        if ( ensureDistinct )
            stream = stream.distinct();
        return stream;
    }

    //    private static <X> Iterator<X> distinct(Iterator<X> iter) {
    //        return Iter.distinct(iter);
    //    }

    private Stream<T> find_ANY_ANY_ANY() {
        Stream<T> stream = sourceFind(ANY, ANY, ANY);
        stream = inf(stream);
        if ( setup.includeDerivedDataRDFS() )
            stream = stream.distinct();
        return stream;
    }

    private Stream<T> infFilter(Stream<T> stream, X subject, X predicate, X object) {
        stream = inf(stream);
        boolean check_s = isTerm(subject);
        boolean check_p = isTerm(predicate);
        boolean check_o = isTerm(object);
        Predicate<T> filter = triple ->{
            return (! check_s || subject(triple).equals(subject)) &&
                   (! check_p || predicate(triple).equals(predicate)) &&
                   (! check_o || object(triple).equals(object)) ;
        };
        stream = stream.filter(filter);
        return stream;
    }

    private Stream<T> inf(Stream<T> stream) {
        return stream.flatMap(applyInf::apply);
    }

    // XXX Rewrite ?? "Set" might mean this is best, as is materialization.
    private void accInstances(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Stream<T> stream = sourceFind(ANY, rdfType, type);
            stream.forEach(triple -> tuples.add(dstCcreate(subject(triple), rdfType, requestedType)) );
        }
    }

    private void accInstancesDomain(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Set<X> predicates = setup.getPropertiesByDomain(type);
            if ( predicates == null )
                continue;
            predicates.forEach(p -> {
                Stream<T> stream = sourceFind(ANY, p, ANY);
                stream.forEach(triple -> tuples.add(dstCcreate(subject(triple), rdfType, requestedType)) );
            });
        }
    }

    private void accInstancesRange(Set<T> tuples, Set<X> types, X requestedType) {
        for ( X type : types ) {
            Set<X> predicates = setup.getPropertiesByRange(type);
            if ( predicates == null )
                continue;
            predicates.forEach(p -> {
                Stream<T> stream = sourceFind(ANY, p, ANY);
                stream.forEach(triple -> tuples.add(dstCcreate(object(triple), rdfType, requestedType)) );
            });
        }
    }

    private void accTypes(Set<X> types, X subject) {
        Stream<T> stream = sourceFind(subject, rdfType, ANY);
        stream.forEach(triple -> types.add(object(triple)));
    }

    private void accTypesDomain(Set<X> types, X X) {
        Stream<T> stream = sourceFind(X, ANY, ANY);
        stream.forEach(triple -> {
            X p = predicate(triple);
            Set<X> x = setup.getDomain(p);
            types.addAll(x);
        });
    }

    private void accTypesRange(Set<X> types, X X) {
        Stream<T> stream = sourceFind(ANY, ANY, X);
        stream.forEach(triple -> {
            X p = predicate(triple);
            Set<X> x = setup.getRange(p);
            types.addAll(x);
        });
    }

    private Set<X> subTypes(Set<X> types) {
        Set<X> x = new HashSet<>();
        for ( X type : types ) {
            Set<X> y = setup.getSubClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    private Set<X> superTypes(Set<X> types) {
        Set<X> x = new HashSet<>();
        for ( X type : types ) {
            Set<X> y = setup.getSuperClasses(type);
            x.addAll(y);
            x.add(type);
        }
        return x;
    }

    private Set<X> subTypes(X type) {
        Set<X> x = new HashSet<>();
        x.add(type);
        Set<X> y = setup.getSubClasses(type);
        x.addAll(y);
        return x;
    }

    private Set<X> superTypes(X type) {
        Set<X> x = new HashSet<>();
        x.add(type);
        Set<X> y = setup.getSuperClasses(type);
        x.addAll(y);
        return x;
    }
}
