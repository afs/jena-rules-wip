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

package org.seaborne.jena.inf_rdfs;

import java.io.IOException;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.graph.Graph;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileUtils;
/** Test graphs  */
public abstract class AbstractTestGraphRDFS extends AbstractTestRDFS {
    // [RDFS] As parameterized tests

    static final String DIR = "testing/Inf";
    static final String DATA_FILE = DIR+"/rdfs-data.ttl";
    static final String VOCAB_FILE = DIR+"/rdfs-vocab.ttl";
    //static final String RULES_FILE = DIR+"/rdfs-min-backwards.rules";
    static final String RULES_FILE = DIR+"/rdfs-min.rules";
    private static Graph infGraph;
    protected static Graph vocab;
    protected static Graph data;
    static {
        vocab = RDFDataMgr.loadGraph(VOCAB_FILE);
        data = RDFDataMgr.loadGraph(DATA_FILE);
        infGraph = createRulesGraph(data, vocab, RULES_FILE);
    }

    /** Create a Jena-rules backed graph */
    protected static Graph createRulesGraph(Graph data, Graph vocab, String rulesFile) {
        try {
            String rules = FileUtils.readWholeFileAsUTF8(rulesFile);
            rules = rules.replaceAll("#[^\\n]*", "");
            Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rules));
            return reasoner.bindSchema(vocab).bind(data);
        }
        catch (IOException ex) { IO.exception(ex) ; return null ; }
    }

    @Override
    final
    protected Graph getReferenceGraph() {
        return infGraph;
    }

    @Override
    protected String getReferenceLabel() {
        return "InfGraph";
    }


}
