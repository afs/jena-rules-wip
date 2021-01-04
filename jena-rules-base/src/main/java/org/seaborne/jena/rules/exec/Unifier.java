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

package org.seaborne.jena.rules.exec;

public class Unifier {}

// public class Unifier {
//    private List<Pair<Node,Node>> x = new ArrayList<>();
//
//    public static Unifier create() { return new Unifier(); }
//
//    private Unifier() {}
//
//    public void add(Node n1, Node n2) {
//        Objects.requireNonNull(n1);
//        Objects.requireNonNull(n2);
//        x.add(Pair.create(n1, n2));
//    }
//
//    public Node mapLR(Node n) {
//        Objects.requireNonNull(n);
//        for (int i = 0 ; i<x.size() ; i++ ) {
//            if ( n.equals(x.get(i).getLeft()) )
//                return x.get(i).getRight();
//        }
//        return n;
//    }
//
//    public boolean containsLR(Node n) {
//        Objects.requireNonNull(n);
//        for (int i = 0 ; i<x.size() ; i++ ) {
//            if ( n.equals(x.get(i).getLeft()) )
//                return true;
//        }
//        return false;
//    }
//
//    public Node mapRL(Node n) {
//        Objects.requireNonNull(n);
//        for (int i = 0 ; i<x.size() ; i++ ) {
//            if ( n.equals(x.get(i).getRight()) )
//                return x.get(i).getLeft();
//        }
//        return n;
//    }
//
//    public boolean containsRL(Node n) {
//        Objects.requireNonNull(n);
//        for (int i = 0 ; i<x.size() ; i++ ) {
//            if ( n.equals(x.get(i).getRight()) )
//                return true;
//        }
//        return false;
//    }
//
//    @Override
//    public String toString() {
//        StringJoiner sj = new StringJoiner(", ", "{ ", " }");
//        x.forEach(p->sj.add(p.toString()));
//        return sj.toString();
//    }
// }

