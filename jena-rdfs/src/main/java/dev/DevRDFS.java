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
import java.util.*;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.graph.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.other.G;
import org.apache.jena.riot.other.Transitive;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterRoot;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.engine.main.QueryEngineMain;
import org.apache.jena.sparql.engine.main.QueryEngineMainQuad;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb2.DatabaseMgr;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.seaborne.jena.inf_rdfs.*;
import org.seaborne.jena.inf_rdfs.engine.InfGlobal;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_TDB1;
import org.seaborne.jena.inf_rdfs.setup.SetupRDFS_TDB2;
import solver.OpExecutorQuads;
import solver.PatternMatchData;

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

    static Graph inf;

    private static void test() {
        Graph g = GraphFactory.createDefaultGraph();
        g.add(SSE.parseTriple("(:n1 :p :n2)"));
        g.add(SSE.parseTriple("(:n2 :p :n3)"));
        g.add(SSE.parseTriple("(:n2 :p :n4)"));
        g.add(SSE.parseTriple("(:n3 :p :n5)"));
        g.add(SSE.parseTriple("(:n5 :p :n1)"));
        g.add(SSE.parseTriple("(:n1 :p :n1)"));
//        g.add(SSE.parseTriple("()"));
//        g.add(SSE.parseTriple("()"));

        //Multimap<Node, Node> x = TransitiveX.transitive(g, SSE.parseNode(":p"));
        //x.keySet().forEach(k->System.out.printf("%s   %s\n", k,x.get(k)));
        Map<Node, Collection<Node>> x = Transitive.transitive(g, SSE.parseNode(":p"));
        x.forEach((k,v)->System.out.printf("%s   %s\n", k, v));
        System.out.println("DONE");
        System.exit(0);
    }

    public static void main(String...argv) throws IOException {
        //matchData();System.exit(0);

        sparql();
        System.exit(0);

        //basic();
        //plain();
        expand();
    }

    static String[] data = {
         "(:g :S :p :x)"
        ,"(:g1 :s1 :p :x)"
        ,"(:g1 :x :p 'B')"
        ,"(:g2 :s1 :p :x)"
        ,"(:g2 :x :p 'C')"
    };

    private static void matchData() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        Arrays.stream(data).forEach(x->dsg.add(SSE.parseQuad(x)));
        ExecutionContext execCxt = new ExecutionContext(null, dsg.getDefaultGraph(), dsg, null);
        BasicPattern bgp = SSE.parseBGP("(bgp (?s ?p ?x) (?x ?q ?o))");

        //Node gn = Quad.unionGraph;
        Node gn = SSE.parseNode("?g");

        //Check input with binding
        Binding start = BindingFactory.binding(Var.alloc("g"), SSE.parseNode(":g2"));
        //Binding start = BindingFactory.root();
        QueryIterator input = QueryIterRoot.create(start, execCxt);
        QueryIterator iter = PatternMatchData.execute(dsg, gn, bgp, input, null, execCxt);

//        Iterator<Binding> iter = StageMatchData.access(BindingFactory.root(),
//                                                       null, SSE.parseTriple("(?s ?p ?o)"),
//                                                       null/*filter*/, true, execContext);
        List<Binding> x = Iter.toList(iter);
        if ( x.isEmpty() )
            System.out.println("[ <empty> ]");
        else {
            StringJoiner sj = new StringJoiner("\n  ", "[\n  ", "\n]");
            x.forEach(b->sj.add(b.toString()));
            System.out.println(sj.toString());
        }
    }

    private static void sparql() {
        Node gn = NodeFactory.createURI("http://example/g");
        String DATA_FILE = "data.ttl";
        String VOCAB_FILE = "vocab.ttl";

        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
        Model data = RDFDataMgr.loadModel(DATA_FILE);

//        System.out.println("---- Schema");
//        RDFDataMgr.write(System.out, vocab, Lang.TTL);
//        System.out.println("---- Data");
//        RDFDataMgr.write(System.out, data, Lang.TTL);
//        System.out.println("----");

        SetupRDFS<Node> setup = InfFactory.setupRDF(vocab.getGraph(), false);

        if ( false )
        {
            System.out.println("Graph: find");
            Graph graph = InfFactory.graphRDFS(data.getGraph(), setup);
            Node n_a = SSE.parseNode(":a");
            System.out.println(G.listSP(graph, n_a, RDF.Nodes.type));
            //System.out.println("--");
        }

        String PREFIX = "PREFIX : <http://example/>  PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>";
        //String qs = PREFIX+"\n"+"SELECT * { { ?s ?p ?o } UNION { GRAPH ?g {?s ?p ?o } } }";
        String qs = PREFIX+"\n"+"SELECT * { :a rdf:type ?type}";
        Query query = QueryFactory.create(qs);

        //DatasetGraph dsg = DatasetGraphFactory.wrap(graph);
        // Union?

        boolean ALL = false;

        if ( ALL )
        {
            System.out.println("-- Plain");
            DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
            GraphUtil.addInto(dsg.getDefaultGraph(), data.getGraph());
            //RDFDataMgr.write(System.out, dsg, Lang.TRIG);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
            QueryExecUtils.executeQuery(qExec);
        }

        if ( true )
        {
            System.out.println("-- Graph");
            Graph g2 = new GraphRDFS(data.getGraph(), setup);
            DatasetGraph dsg = DatasetGraphFactory.wrap(g2);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsg);
            QueryExecUtils.executeQuery(qExec);
        }

        if ( ALL )
        {
            System.out.println("-- DatasetGraph, getGraph");
            DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
            GraphUtil.addInto(dsg.getDefaultGraph(), data.getGraph());
            // Context?
            DatasetGraph dsgx = new DatasetGraphRDFS(dsg, setup);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsgx);
            QueryExecUtils.executeQuery(qExec);
        }

        if ( true ) {
            // Quad mode
            QueryEngineMain.unregister();
            QueryEngineMainQuad.register();
            QC.setFactory(ARQ.getContext(), OpExecutorQuads::new);
            //DatasetGraphRDFS.byGraph = false;
        }

        {
            // Chooses OpExecutorQuads (when default)
            System.out.println("-- DatasetGraph, find/4");
            DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
            GraphUtil.addInto(dsg.getDefaultGraph(), data.getGraph());
            DatasetGraph dsgx = new DatasetGraphRDFS(dsg, setup);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsgx);
            QueryExecUtils.executeQuery(qExec);
        }

        {
            // Chooses OpExecutorTDB1 -> getGraph unless context hides TDB1 dataset choice.
            System.out.println("-- DatasetGraph, find/4, TDB/node");
            DatasetGraph dsg = TDBFactory.createDatasetGraph();
            GraphUtil.addInto(dsg.getDefaultGraph(), data.getGraph());

            QC.setFactory(dsg.getContext(), OpExecutorQuads::new);

            DatasetGraph dsgx = new DatasetGraphRDFS(dsg, setup);
            QueryExecution qExec = QueryExecutionFactory.create(query, dsgx);
            QueryExecUtils.executeQuery(qExec);
        }
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

    public static void expand() throws IOException {
        boolean combined = false;
        String DIR = "testing/Inf";
        String DATA_FILE = "data.ttl";
        String VOCAB_FILE = "vocab.ttl";
        String RULES_FILE = DIR+"/rdfs-min.rules";
        System.out.println("---- Schema");
        Model vocab = RDFDataMgr.loadModel(VOCAB_FILE);
        //RDFDataMgr.write(System.out, vocab, Lang.TTL);

        System.out.println("---- Data");
        Model data = RDFDataMgr.loadModel(DATA_FILE);
        //RDFDataMgr.write(System.out, data, Lang.TTL);

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
        SetupRDFS<Node> setup = InfFactory.setupRDF(vocab.getGraph(), combined);
        StreamRDF stream = StreamRDFLib.graph(graphExpanded);
        // Apply inferences.
        stream = new InfStreamRDFS(stream, setup);
        sendToStream(data.getGraph(), stream);
        RDFDataMgr.write(System.out, graphExpanded, Lang.TTL);
    }

    private static void sendToStream(Graph graph, StreamRDF stream) {
        StreamRDFOps.sendGraphToStream(graph, stream);
//        graph.getPrefixMapping().getNsPrefixMap().forEach(stream::prefix);
//        graph.find(Node.ANY, Node.ANY, Node.ANY).forEachRemaining(stream::triple);
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
    /** Match a graph, maybe remove RDFS vocab */
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

