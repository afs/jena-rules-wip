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

package org.seaborne.jena.shacl_rules.writer;

import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.apache.jena.atlas.io.IndentedLineBuffer;
import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.graph.Triple;
import org.apache.jena.irix.IRIx;
import org.apache.jena.riot.out.NodeFormatter;
import org.apache.jena.riot.out.NodeFormatterTTL_MultiLine;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.writer.DirectiveStyle;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.serializer.FormatterElement;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.seaborne.jena.shacl_rules.Rule;
import org.seaborne.jena.shacl_rules.RuleSet;

public class ShaclRulesWriter {

    public static void printBasic(RuleSet ruleSet) {
        ruleSet.getRules().forEach(r -> {
            System.out.println(r);
        });
    }

    public static void print(RuleSet ruleSet) {
        print(System.out, ruleSet, true);
    }

    public static void print(RuleSet ruleSet, boolean flatMode) {
        print(System.out, ruleSet, flatMode);
    }


    public static void print(OutputStream outStream, RuleSet ruleSet, boolean flatMode) {

        Style style = flatMode ? Style.Flat : Style.MultiLine;

        IndentedWriter output = new IndentedWriter(outStream);
        try {
            internalPrint(output, ruleSet, style);
        } finally {
            output.flush();
        }
    }

    private static void internalPrint(IndentedWriter out, RuleSet ruleSet, Style style) {

        Prologue prologue = ruleSet.getPrologue().copy();
        prologue.setBaseURI(null);

        String baseURI =  prologue.getBaseURI();
        IRIx baseIRI = prologue.getBase();
        PrefixMap prefixMap = PrefixMapFactory.create(prologue.getPrefixMapping());
        ShaclRulesWriter w = new ShaclRulesWriter(out, prefixMap, baseIRI, prologue, style);
        w.writeRuleSet(ruleSet);
    }

    private final IndentedWriter out;
    private final PrefixMap prefixMap;
    private final IRIx base;
    private final NodeFormatter nodeFormatter;
    private final SerializationContext sCxt;

    enum Style { Flat, MultiLine }

    private final Style style;

    private ShaclRulesWriter(IndentedWriter output, PrefixMap prefixMap, IRIx baseIRI, Prologue prologue, Style style) {
       this.out = Objects.requireNonNull(output);
       this.prefixMap = prefixMap;
       this.base = baseIRI;
       String baseStr = (baseIRI == null) ?null : baseIRI.str();

       this.nodeFormatter = new NodeFormatterTTL_MultiLine(baseStr, prefixMap);
       this.sCxt = new SerializationContext(prologue);
       this.style = Objects.requireNonNull(style);
    }

    private void writeRuleSet(RuleSet ruleSet) {
        Objects.requireNonNull(ruleSet);
        if ( base != null )
            RiotLib.writeBase(out, base.str(), DirectiveStyle.KEYWORD);
        if ( prefixMap != null )
            RiotLib.writePrefixes(out, prefixMap, DirectiveStyle.KEYWORD);
        if ( ( base != null || !prefixMap.isEmpty() ) && !ruleSet.isEmpty() )
            out.println();

        writeData(ruleSet);

        List<Rule> rules = ruleSet.getRules();
        boolean first = true;

        for ( Rule rule : rules ) {
            if ( ! first ) {
                if ( style == Style.MultiLine )
                    out.println();
            }

            first = false;

            writeRule(rule);
        }
    }

    private void writeData(RuleSet ruleSet) {
        List<Triple> data = ruleSet.getDataTriples();
        if ( data.isEmpty() )
            return;

        out.print("DATA {");
        if ( style == Style.Flat || data.size() == 1 ) {
            data.forEach(triple->{
                out.print(" ");
                writeTriple(triple);
            });
            out.println(" }");
            return;
        }
        out.println();
        out.incIndent();
        data.forEach(triple->{
            writeTriple(triple);
            out.println();
        });
        out.decIndent();
        out.println("}");
        out.println();
    }

    private void writeRule(Rule rule) {
        out.print("RULE ");
        writeHead(rule);
        if ( style == Style.MultiLine )
            out.println();
        else
            out.print(" ");
        out.print("WHERE ");
        writeBody(rule);
    }

    private void writeHead(Rule rule) {
        BasicPattern head = rule.getHead();
        out.print("{");
        head.forEach(triple -> {
            out.print(" ");
            writeTriple(triple);
        });
        out.print(" }");
    }

    // Space then triple.
    private void writeTriple(Triple triple) {
        nodeFormatter.format(out, triple.getSubject());
        out.print(" ");
        nodeFormatter.format(out, triple.getPredicate());
        out.print(" ");
        nodeFormatter.format(out, triple.getObject());
        out.print(" .");
    }

    private void writeBody(Rule rule) {
        // The element block in indented. Later ...
        int indent = 0 ;

        switch(style) {
            case Flat -> {
                out.setFlatMode(true);
                out.print("{");
            }
            case MultiLine -> {
                out.print("{");
                out.println();
                //out.incIndent(indent);
            }
        }
        // Without braces.
        IndentedLineBuffer outx = new IndentedLineBuffer();
        FormatterElement.format(outx, sCxt, rule.getBody());
        String x = outx.asString();
        // Remove outer {}s. Put back leading space.
        x = " "+x.substring(1, x.length()-1);
        if ( style == Style.Flat ) {
            //x = x.replace("\n", " ");
            x = x.replaceAll("  +", " ");
        }

        out.print(x);

        switch(style) {
            case Flat -> {
                out.print(" }");
                out.setFlatMode(false);
            }
            case MultiLine ->{
                out.decIndent(indent);
                out.println("}");
            }
        }
        //Blank line
        out.println();
    }
}
