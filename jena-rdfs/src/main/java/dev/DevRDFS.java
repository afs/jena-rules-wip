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

package dev;

import static org.apache.jena.sparql.sse.SSE.str;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.other.Transitive;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.util.FileUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.seaborne.jena.inf_rdfs.GraphRDFS;
import org.seaborne.jena.inf_rdfs.InfFactory;
import org.seaborne.jena.inf_rdfs.InferenceStreamRDFS;
import org.seaborne.jena.inf_rdfs.engine.InfGlobal;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_Node;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_TDB1;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_TDB2;

public class DevRDFS {
    static { LogCtl.setLogging(); }

    // Extract transitive closure code.
    // InferenceSetupRDFS


    // -------- OLD

    // Case of stuff in the data :
    // :T rdfs:label "TYPE" .
    // Better handling of vocab-in-data.
    //   1 - check on load.
    //   2 - InfGlobal.removeRDFS needs to be better yet? based on data?

    /* makes sense:
     * No vocab + exclude vocab infs
     * Include vocab + include vocab infs
     *
     * Do not make sense?
     * No vocab + include vocab infs
     * Include vocab + exclude vocab infs
     *
     */

    /*
     * QueryExecutionDSG etc over Graphs/DatasetGraphs
     *
     * Setup engine <X> -- abstract class with Node -> X function.
     * StreamRDF versions of basic range/domain;
     *   SubProperty -> R/D -> SubClass
     *
     * When to materialize? Recursive calls into the source.
     * GraphRDFS (stream version) needs cleaning.
     *
     * coverage
     * Duplicates in (? ? ?)combined due to subClassOf
     *   Another rules file - RDFS graph without axioms.
     *
     * rdfs:member, list:member
     */

    // More tests
    //   Coverage
    //   find_X_rdfsSubClassOf_Y

    // Tests for:
    //   InfererenceProcessTriple
    //   InferenceProcessStreamRDF
    //   InfererenceProcessIteratorRDFS

    // Test (D,V) , (D,-), (-, V), (D+V, D+V)
    //   Mode D-extract-V.

    // ANY_ANY_T - filter rdf:type and replace - no distinct needed.

    // Use InfFactory.

    static Graph inf;
    static Graph g_rdfs2;
    static Graph g_rdfs3;
    public static void main(String...argv) throws IOException {
        Graph g = GraphFactory.createDefaultGraph();
        g.add(SSE.parseTriple("(:n1 :p :n2)"));
        g.add(SSE.parseTriple("(:n2 :p :n3)"));
        g.add(SSE.parseTriple("(:n2 :p :n4)"));
        g.add(SSE.parseTriple("(:n3 :p :n5)"));
        g.add(SSE.parseTriple("(:n5 :p :n1)"));
        g.add(SSE.parseTriple("(:n1 :p :n1)"));
//        g.add(SSE.parseTriple("()"));
//        g.add(SSE.parseTriple("()"));

        //Multimap<Node, Node> x= TransitiveX.transitive(g, SSE.parseNode(":p"));
        //x.keySet().forEach(k->System.out.printf("%s   %s\n", k,x.get(k)));
        Map<Node, Collection<Node>> x = Transitive.transitive(g, SSE.parseNode(":p"));
        x.forEach((k,v)->System.out.printf("%s   %s\n", k, v));
        System.out.println("DONE");
        System.exit(0);


        //basic();
        //plain();
        expand();
    }

    private static void basic() {
        String DATA_FILE = "data.ttl";
        String VOCAB_FILE = "vocab.ttl";
        System.out.println("---- Schema");
        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
//        RDFDataMgr.write(System.out, vocab, Lang.TTL);

        System.out.println("---- Data");
        Model data = RDFDataMgr.loadModel(DATA_FILE);
//        RDFDataMgr.write(System.out, data, Lang.TTL);
        System.out.println("----");

        SetupRDFS_Node setup = InfFactory.setupRDF(vocab.getGraph(), false);
        Graph graph = InfFactory.graphRDFS(data.getGraph(), setup);

        Node n_a = SSE.parseNode(":a");
        Node n_T = SSE.parseNode(":T");
        Node n_T2 = SSE.parseNode(":T2");
        Node n_b = SSE.parseNode(":b");

        Iter.print(graph.find(n_a, RDF.Nodes.type, null));
        System.out.println("--");
        Iter.print(graph.find(null, RDF.Nodes.type, n_T2));
        System.out.println("--");
        Iter.print(graph.find(n_b, RDF.Nodes.type, null));
        System.out.println("----");
    }

    public static void mainTDB(String...argv) throws IOException {
        String DIR = "testing/Inf";
        String DATA_FILE = DIR+"/rdfs-data.ttl";
        String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";
        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
        Model data = RDFDataMgr.loadModel(DATA_FILE);
//        String RULES_FILE = DIR+"/rdfs-min.rules";
//        String rules = FileUtils.readWholeFileAsUTF8(RULES_FILE);
//        rules = rules.replaceAll("#[^\\n]*", "");
        // TDB1
        DatasetGraph dsg1 = TDBFactory.createDatasetGraph();
        // @@
        SetupRDFS_TDB1 setup1 = new SetupRDFS_TDB1(vocab.getGraph(), dsg1, false);
        //Graph graph = new GraphRDFS(setup1, data.getGraph());
        // TDB2

        DatasetGraph dsg2 = DatabaseMgr.createDatasetGraph();
        SetupRDFS_TDB2 setup2 = new SetupRDFS_TDB2(vocab.getGraph(), dsg2, false);
    }

    public static void plain(String...argv) throws IOException {
        String DIR = "testing/Inf";
//        String DATA_FILE = DIR+"/rdfs-data.ttl";
//        String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";
//        String RULES_FILE = DIR+"/rdfs-min.rules";
        String DATA_FILE = "data.ttl";
        String VOCAB_FILE = "vocab.ttl";
        String RULES_FILE = DIR+"/rdfs-min.rules";
        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
        Model data = RDFDataMgr.loadModel(DATA_FILE);
        String rules = FileUtils.readWholeFileAsUTF8(RULES_FILE);
        rules = rules.replaceAll("#[^\\n]*", "");
        SetupRDFS_Node setup = InfFactory.setupRDF(vocab.getGraph(), false);
        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
        InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);
        inf = m.getGraph();
        g_rdfs2 = new GraphRDFS(data.getGraph(), setup);
        //g_rdfs3 = new GraphRDFS3(setup, data.getGraph());
        //test(null, null, node("T2"));
        test(node("T"), null, null );
        // Expansion
        //InferenceProcessorRDFS proc = new InferenceProcessorRDFS(setup);
    }

    public static void expand() throws IOException {
        boolean combined = false;
        String DIR = "testing/Inf";
        String DATA_FILE = "data.ttl";
        String VOCAB_FILE = "vocab.ttl";
        String RULES_FILE = DIR+"/rdfs-min.rules";
        System.out.println("---- Schema");
        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
        RDFDataMgr.write(System.out, vocab, Lang.TTL);

        System.out.println("---- Data");
        Model data = RDFDataMgr.loadModel(DATA_FILE);
        RDFDataMgr.write(System.out, data, Lang.TTL);

        // Jena rules RDFS
//        System.out.println("---- Rules");
//        String rules = FileUtils.readWholeFileAsUTF8(RULES_FILE);
//        System.out.print(rules);
//        rules = rules.replaceAll("#[^\\n]*", "");
//        System.out.println();
//        Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
//        InfModel m = ModelFactory.createInfModel(reasoner, vocab, data);

        System.out.println("---- Expansion");
        // Expansion Graph
        Graph graphExpanded = Factory.createDefaultGraph();
        SetupRDFS_Node setup = InfFactory.setupRDF(vocab.getGraph(), combined);
        StreamRDF stream = StreamRDFLib.graph(graphExpanded);
        // Apply inferences.
        stream = new InferenceStreamRDFS(stream, setup);
        sendToStream(data.getGraph(), stream);
        RDFDataMgr.write(System.out, graphExpanded, Lang.TTL);
    }

    private static void sendToStream(Graph graph, StreamRDF stream) {
        graph.getPrefixMapping().getNsPrefixMap().forEach(stream::prefix);
        graph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(stream::triple);
    }

    private static void test(Node s, Node p, Node o) {
        compare("G2", g_rdfs2, inf, s, p, o);
    }

    private static void test3(Node s, Node p, Node o) {
        System.out.println("** GraphRDFS3 **");
        dwim(g_rdfs3, inf, s,p,o);
    }

    static Node node(String str) { return NodeFactory.createURI("http://example/"+str) ; }

    static void dwim(Graph gTest, Graph gInf, Node s, Node p , Node o) {
        dwim$("inference", gInf, s,p,o, true);
        dwim$("test", gTest, s,p,o, false);
        System.out.println();
    }

    static void dwim(Graph graph, Node s, Node p , Node o) {
        dwim$(null, graph, s,p,o, false);
    }


//    static Filter<Triple> filterRDFS = new Filter<Triple>() {
//        @Override
//        public boolean accept(Triple triple) {
//            if ( InfGlobal.includeDerivedDataRDFS ) {
//                Node p = triple.getPredicate();
//                return ! p.equals(RDFS.Nodes.domain) && ! p.equals(RDFS.Nodes.range);
//            }
//            return
//                ! triple.getPredicate().getNameSpace().equals(RDFS.getURI());
//            }
//    };
    static void dwim$(String label, Graph g, Node s, Node p , Node o, boolean filter) {
        if ( label != null )
            System.out.println("** Graph ("+label+"):");
        System.out.printf("find(%s, %s, %s)\n", str(s), str(p), str(o));
        ExtendedIterator<Triple> iter = g.find(s, p, o);
        List<Triple> x = iter.toList();
        if ( filter )
            x = InfGlobal.removeRDFS(x);
        x.forEach(t -> System.out.println("    "+t));
        System.out.println();
    }

    static void compare(String label, Graph testGraph, Graph refGraph, Node s, Node p , Node o) {
        if ( label != null )
            System.out.println("** Compare ("+label+"):");
        else
            System.out.println("** Compare");
        System.out.printf("find(%s, %s, %s)\n", str(s), str(p), str(o));
        List<Triple> x1 = testGraph.find(s, p, o).toList();
        List<Triple> x2 = refGraph.find(s, p, o).toList();
        if ( true )
            x2 = InfGlobal.removeRDFS(x2);
        boolean b = sameElts(x1, x2);
        if ( !b ) {
            System.out.println("  Different:");
            x1.stream().map(SSE::str).forEach(t -> System.out.println("Test:  "+t));
            x2.stream().map(SSE::str).forEach(t -> System.out.println("Ref :  "+t));
        } else {
            System.out.println("  Same:");
            x1.stream().map(SSE::str).forEach(t -> System.out.println("    "+t));
        }

        System.out.println();
    }

    /** Test for same elements, regarless of cardinality */
    public static <T> boolean sameElts(Collection<T> left, Collection<T> right) {
        return right.containsAll(left) && left.containsAll(right);
    }

}

