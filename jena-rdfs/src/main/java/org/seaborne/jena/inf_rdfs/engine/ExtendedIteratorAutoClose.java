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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Provide autoclose to an {@link ExtendedIterator} so that "close" is called when
 * the iterator ends.
 * <p>
 * Use when making a stream out of an iterator so that {@link ExtendedIterator}'s
 * contract for "close" is provided, assuming the stream runs to the end.
 * <p>
 * Use try-with-resources on the stream.
 * <p>
 * Better: use {@code stream.onCompletion(()->extendedIterator.close())}
 */
public class ExtendedIteratorAutoClose<T> implements Iterator<T> {
    private boolean             isClosed = false;
    private ExtendedIterator<T> underlying;

    public ExtendedIteratorAutoClose(ExtendedIterator<T> underlying) {
        this.underlying = underlying;
    }

    @Override
    public boolean hasNext() {
        boolean b = underlying.hasNext();
        if ( !b )
            closeWrapped();
        return b;
    }

    @Override
    public T next() {
        try {
            return underlying.next();
        } catch (NoSuchElementException ex) {
            closeWrapped();
            throw ex;
        }
    }

    private void closeWrapped() {
        if ( isClosed )
            return;
        isClosed = true;
        underlying.close();

    }
}
