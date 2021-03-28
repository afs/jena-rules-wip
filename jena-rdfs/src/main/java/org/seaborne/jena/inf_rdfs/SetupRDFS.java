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

import java.util.Set;
import org.apache.jena.graph.Node;
import org.apache.jena.tdb2.store.NodeId;

/**
 * Inference setup for RDFS over some space of 3-tuples of type {@code <T>}.
 * {@code <T>} maybe {@link Node} but it may be some storage idea of a RDF term,
 * such as TDB2's {@link NodeId}.
 */
public interface SetupRDFS<T> {

    /** All super-types of an element.
     * Does not include the element unless there is a cycle of length >1.
     * Returns an empty set of there are no declared superclasses.
     */
    public Set<T> getSuperClasses(T elt);
    /** All super-types of an element, including the element itself */
    public Set<T> getSuperClassesInc(T elt);
    /** All sub-types of an element.  Does not include the element unless there is a cycle of length >1 */
    public Set<T> getSubClasses(T elt);
    /** All sub-types of an element, including the element itself */
    public Set<T> getSubClassesInc(T elt);
    /** All super-properties.  Does not include the property itself unless there is a cycle of length >1. */
    public Set<T> getSuperProperties(T elt);
    /** All super-properties including the property itself. */
    public Set<T> getSuperPropertiesInc(T elt);
    /** All sub-properties.  Does not include the property itself unless there is a cycle of length >1. */
    public Set<T> getSubProperties(T elt);
    /** All sub-properties including the property itself. */
    public Set<T> getSubPropertiesInc(T elt);
    /** Does this setup have any range declarations? */
    public boolean hasRangeDeclarations();
    /** Does this setup have any domain declarations? */
    public boolean hasDomainDeclarations();
    /** Get the range(s) of a property - only includes mentioned range types, not supertypes. */
    public Set<T> getRange(T elt);
    /** Get the domain(s) of a property - only includes mentioned domain types, not supertypes. */
    public Set<T> getDomain(T elt);
    /** Get the properties that directly mention 'type' as their range. */
    public Set<T> getPropertiesByRange(T elt);
    /** Get the properties that directly mention 'type' as their domain. */
    public Set<T> getPropertiesByDomain(T elt);
    /** */
    public boolean includeDerivedDataRDFS();

//    /** Get the range(s) of a property including supertypes. */
//    public Set<T> getRangeAll(T elt);
//
//    /** Get the domain(s) of a property including supertypes. */
//    public Set<T> getDomainAll(T elt);
}

