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

package org.seaborne.jena.inf2;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Map.Entry ;

import org.apache.jena.ext.com.google.common.collect.ArrayListMultimap ;
import org.apache.jena.ext.com.google.common.collect.Multimap ;
import org.apache.jena.graph.Node ;
import org.apache.jena.sparql.ARQInternalErrorException ;
import org.apache.jena.sparql.core.Var ;
import org.apache.jena.sparql.engine.ExecutionContext ;
import org.apache.jena.sparql.engine.QueryIterator ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.engine.binding.BindingFactory ;
import org.apache.jena.sparql.engine.binding.BindingMap ;
import org.apache.jena.sparql.engine.iterator.QueryIter ;
import org.apache.jena.sparql.engine.iterator.QueryIterExtendByVar ;
import org.apache.jena.sparql.pfunction.PFuncSimple ;
import org.apache.jena.sparql.util.IterLib ;

/**
 *  Transitive property - done by look-aside
 */
public class PFbyTable extends PFuncSimple {

    // 2 way multihash table.
    static Multimap<Node, Node> subj2obj = ArrayListMultimap.create() ;
    static Multimap<Node, Node> obj2subj = ArrayListMultimap.create() ;
    
    // Dev hack
    public static void add(Node node1, Node node2) {
        subj2obj.put(node1, node2) ;
        obj2subj.put(node2, node1) ;
    }
    
    public PFbyTable() {}
    
    @Override
    public QueryIterator execEvaluated(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        
        
        if ( subject.isVariable() ) {
            if ( object.isVariable() )
                return execVarVar(binding, subject, predicate, object, execCxt) ;
            else
                return execVarTerm(binding, subject, predicate, object, execCxt) ;
        } else {
            if ( object.isVariable() )
                return execTermVar(binding, subject, predicate, object, execCxt) ;
            else
                return execTermTerm(binding, subject, predicate, object, execCxt) ;
        }
    }

    private QueryIterator execVarVar(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        Iterator<Entry<Node,Node>> iter = subj2obj.entries().iterator() ;
        return new QueryIterExtendByVar2(binding, Var.alloc(subject), Var.alloc(object), iter, execCxt) ;
    }

    private QueryIterator execVarTerm(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        Collection<Node> x = obj2subj.get(object) ;
        return new QueryIterExtendByVar(binding, Var.alloc(subject), x.iterator(), execCxt) ;
    }

    private QueryIterator execTermVar(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        Collection<Node> x = subj2obj.get(subject) ;
        return new QueryIterExtendByVar(binding, Var.alloc(object), x.iterator(), execCxt) ;
    }

    private QueryIterator execTermTerm(Binding binding, Node subject, Node predicate, Node object, ExecutionContext execCxt) {
        Collection<Node> x = subj2obj.get(subject) ;
        return x.contains(object) ? IterLib.result(binding, execCxt) : IterLib.noResults(execCxt) ;
    }
    
    static class QueryIterExtendByVar2 extends QueryIter
    {
        // Use QueryIterProcessBinding?
        private Binding binding ;
        private Var var1 ;
        private Var var2 ;
        private Iterator<Entry<Node,Node>> entries ;
        
        public QueryIterExtendByVar2(Binding binding, Var var1, Var var2, Iterator<Entry<Node,Node>> entries, ExecutionContext execCxt) {
            super(execCxt);
            if ( true ) { // Assume not too costly.
                if ( binding.contains(var1) )
                    throw new ARQInternalErrorException("Var " + var1 + " already set in " + binding);
                if ( binding.contains(var2) )
                    throw new ARQInternalErrorException("Var " + var2 + " already set in " + binding);
            }
            this.binding = binding;
            this.var1 = var1 ;
            this.var2 = var2 ;
            this.entries = entries;
        }

        @Override
        protected boolean hasNextBinding() {
            return entries.hasNext();
        }

        @Override
        protected Binding moveToNextBinding() {
            Entry<Node, Node> e = entries.next();
            BindingMap b = BindingFactory.create(binding) ;
            b.add(var1, e.getKey()) ; 
            b.add(var2, e.getValue()) ;
            return b;
        }

        @Override
        protected void closeIterator() {
            entries = null ;
        }

        @Override
        protected void requestCancel() {}
    }

}
