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

package previous.rules1;

import static java.util.stream.Collectors.toList ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.sse.SSE ;
import org.seaborne.jena.rules.Rel;
import org.seaborne.jena.rules.Rule;
import org.seaborne.jena.rules.RuleSet;

public class Backwards {
    /** Evaluate to match a pattern */
    public static void eval(Triple triplePattern, Graph source, RuleSet rules) {
        List<Rel> heads = rules.getHeads();

        Rel query = tripleToRel(triplePattern);


        System.out.println() ;
        System.out.println("Query: "+SSE.str(triplePattern)) ;

        List<Triple> answers = new ArrayList<>() ;


        // Find rules.
        List<Rule> matches = rules.stream().filter((r) -> matchHead(query, r)).collect(toList()) ;
        System.out.println() ;
        if ( matches.isEmpty() )
            System.out.println("<empty>") ;
        else
            System.out.println(Rule.str(matches)) ;

        matches.forEach((m)-> {
            if ( checkForRecursion1(m, rules) )
                System.out.println("R: "+m);
        }) ;
    }

    private static Rel tripleToRel(Triple triple) {
        Tuple<Node> tuple = TupleFactory.create3(triple.getSubject(), triple.getPredicate(), triple.getObject());
        return new Rel("", tuple);
    }

    private static Triple relToTriple(Rel rel) {
        Tuple<Node> tuple = rel.getTuple();
        if ( tuple.len() != 3 )
            throw new IllegalArgumentException("relToTriple: Rel is not of length 3");
        return Triple.create(tuple.get(0), tuple.get(1), tuple.get(2));
    }


    // ---- Recursion (immediate)
    // Does rule have a recursion?
    // Does the rule body refer to another rule?
    public static boolean checkForRecursion1(Rule m, RuleSet rules) {
        //m.getBody().forEach(mb->checkForRecursion(mb, rules)) ;
        return m.getBody().stream().anyMatch(mb->checkForRecursion1(mb, rules)) ;
    }

    // Direct recursion only.
    private static boolean checkForRecursion1(Rel clause, RuleSet rules) {
        //rules.stream().filter((r)-> recursion(clause, r)) ;
        return  rules.stream().anyMatch((r)-> recursive1(clause, r)) ;
    }

    private static boolean recursive1(Rel clause, Rule r) {
        if ( allVars(clause) )
            return false ;
        if ( allVars(r.getHead()) )
            return false ;
        return match(clause, r.getHead()) ;
    }

    // --- Recursion (cyclic)
    /** Return rules that form a recursion with this rule.
     *
     */
    public static Collection<Rule> equivalenceSet(Rule rule, List<Rule> rules) {
        return null ;
//
//        Set<Rule> cycle = new HashSet<>() ;
//        for ( Triple t : rule.getBody() ) {
//            follow()
//        }

    }


    private static boolean matchUses(Rel clause, Rel ruleHead) {
        if ( allVars(clause) )
            // TEMP
            return false ;
        return match(clause, ruleHead) ;
    }

//    private static boolean allVars(Triple triple) {
//        return isVar(triple.getSubject()) && isVar(triple.getPredicate()) && isVar(triple.getObject()) ;
//    }

    private static boolean allVars(Rel rel) {
        return rel.getTuple().stream().allMatch(Backwards::isVar);

    }
    private static boolean isVar(Node n) { return ! n.isConcrete() ; }

    /** Does triple match the head of the rule ?
     * "match" means "can unify with" i.e. each slot where there are constants in each match up.
     */
    private static boolean matchHead(Rel rel, Rule r) {
        return match(rel, r.getHead()) ;
    }

    /** Test whether one triple/clause matches another, where "match" means same concrete term or variable.
     * @param triple1
     * @param triple2
     * @return boolean
     */
    public static boolean match(Rel triple1, Rel triple2) {
        return match(triple1, triple2, predicateSameTermOrSomeVar) ;
    }

    /**
     * Test whether two {@link Node}s are the same concrete term or one is a variable (maybe both) .
     */
    public static BiFunction<Node, Node, Boolean> predicateSameTermOrSomeVar = (node1, node2) ->
        (node1.isVariable() || node2.isVariable() || node1.equals(node2));

     /**
      * Test whether two {@link Node}s are the both variables or same concrete term.
      * If one is a variable, and one not, return false.
      */
    public static BiFunction<Node, Node, Boolean> predicateSameTermOrBothVars =
        (node1, node2)-> (node1.isVariable() && node2.isVariable()) || node1.equals(node2) ;


    private static boolean match(Rel triple1, Rel triple2, BiFunction<Node, Node, Boolean> condition) {
        if ( triple1.len() != triple2.len() )
            return false;
        Tuple<Node> tuple1 = triple1.getTuple();
        Tuple<Node> tuple2 = triple2.getTuple();
        if ( tuple1.len() != tuple2.len() )
            return false;
        int N = tuple1.len();
        for ( int i = 0 ; i < N ; i++ ) {
            if ( ! condition.apply(tuple1.get(i), tuple2.get(i)) )
                return false;
        }
        return true;
    }

}
