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

package org.seaborne.jena.rules.cmds;

import java.io.IOException;
import java.io.InputStream;

import jena.cmd.CmdGeneral;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.IRILib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.seaborne.jena.rules.RuleSet;
import org.seaborne.jena.rules.lang.RulesParser;
import org.seaborne.jena.rules.lang.RulesWriter;

public class rules extends CmdGeneral {

    static { JenaSystem.init(); }

    public static void main(String...argv) {
        new rules(argv).mainRun();
    }

    protected rules(String[] argv) {
        super(argv);
    }

    @Override
    protected void exec() {
        boolean first = true ;
        for ( String fn : super.positionals ) {
            if ( ! first )
                System.out.println();
            first = false;
            exec1(fn);
        }
    }

    private void exec1(String fn) {
        String baseURI = IRILib.filenameToIRI(fn);
        IndentedWriter out = IndentedWriter.stdout;

        try ( InputStream in = IO.openFile(fn) ) {
            RuleSet ruleSet = RulesParser.parseRuleSet(in, baseURI);
            //addStandardPrefixes(pmap);
            RulesWriter.write(out, ruleSet);
        } catch (IOException ex) {
            out.flush();
            IO.exception(ex);
        }
    }

    private static void addStandardPrefixes(PrefixMap prefixMap) {
        /** Update {@link PrefixMap} with the SHACLC standard prefixes */
        prefixMap.add("rdf",  RDF.getURI());
        prefixMap.add("rdfs", RDFS.getURI());
        prefixMap.add("sh",   SHACL.getURI());
        prefixMap.add("xsd",  XSD.getURI());
        prefixMap.add("",  "http://example/");
    }

    @Override
    protected String getCommandName() {
        return "rules";
    }

    @Override
    protected String getSummary() {
        return "RULES";
    }
}

