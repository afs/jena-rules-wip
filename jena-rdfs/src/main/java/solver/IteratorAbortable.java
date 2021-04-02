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

package solver;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.IteratorWrapper;
import org.apache.jena.query.QueryCancelledException;
import org.apache.jena.sparql.engine.iterator.Abortable;

/**
 * Iterator that adds an abort operation which can be called at any time,
 * including from another thread, and causes the iterator to throw an exception
 * when next touched (hasNext, next).
 */
public class IteratorAbortable<T> extends IteratorWrapper<T> implements Abortable {
    private volatile boolean abortFlag = false;

    public IteratorAbortable(Iterator<T> iterator) {
        super(iterator);
    }

    /** Can call asynchronously at anytime */
    @Override
    public void abort() {
        abortFlag = true;
    }

    @Override
    public boolean hasNext() {
        if ( abortFlag )
            throw new QueryCancelledException();
        return iterator.hasNext();
    }

    @Override
    public T next() {
        if ( abortFlag )
            throw new QueryCancelledException();
        return iterator.next();
    }
}
