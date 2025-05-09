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

package org.seaborne.rules.lang;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.seaborne.rules.*;
import org.seaborne.rules.lang.parser.javacc.ParseException;
import org.seaborne.rules.lang.parser.javacc.RulesJavacc;
import org.seaborne.rules.lang.parser.javacc.TokenMgrError;

/**
 * Syntax:<br/>
 * <pre>
 *   fact(...).
 *   head(...) &lt;- body .
 * </pre>
 * where <tt>body</tt> is a number of atoms (class {@link Rel}).
 * Rule clause terms can be named or unnamed.
 * <pre>
 *     (:s :p ?o) &lt;- (:s :q1 ?z ) (?z :q2 ?o) .
 * <pre>
 *
 */
public class RulesParser {

    static { JenaSystem.init(); }

    interface ParserAction<X> { X parse(RulesJavacc parser) throws ParseException; }

    public static RuleSet parseRuleSet(InputStream input, String baseURI) {
        PrefixMap prefixMap = PrefixMapFactory.create();
        RulesJavacc parser = new RulesJavacc(input, StandardCharsets.UTF_8.name());
        RuleSet ruleSet = parse$(parser, RIOT.getContext(), RulesJavacc::parseRuleSet, baseURI, prefixMap);
        return ruleSet;
    }

    // Convenience: add some default prefixes.

    public static RuleSet parseRuleSet(String string) {
        Reader reader = new StringReader(string) ;
        RulesJavacc parser = new RulesJavacc(reader);
        RuleSet ruleSet = parse$(parser, RIOT.getContext(), RulesJavacc::parseRuleSet, null);
        return ruleSet;
    }

    public static RuleSet rules(String ... strings) {
        return parseRules(strings);
    }

    // Multiple rules, one string per rule.
    public static RuleSet parseRules(String ... strings) {
        StringBuilder sb = new StringBuilder();
        for ( String s : strings ) {
            if ( s.isBlank() ) {
                sb.append(s);
                continue;
            }
            sb.append(s);
            if ( ! s.matches("\\\\.\\s*$") )
                sb.append(" .");
            sb.append("\n");
        }
        return parseRuleSet(sb.toString());
    }

    public static Rule parseRule(String string) {
        Reader reader = new StringReader(string) ;
        RulesJavacc parser = new RulesJavacc(reader);
        Rule rule = parse$(parser, RIOT.getContext(), RulesJavacc::parseRule, null);
        return rule;
    }

    public static Rel parseRel(String string) {
        return parseAtom(string);
    }

    public static Rel parseAtom(String string) {
        Reader reader = new StringReader(string) ;
        RulesJavacc parser = new RulesJavacc(reader);
        Rel rel = parse$(parser, RIOT.getContext(), RulesJavacc::parseAtom, null);
        return rel;
    }

    public static RelStore parseData(String dataStr) {
        Reader reader = new StringReader(dataStr) ;
        RulesJavacc parser = new RulesJavacc(reader);
        return parse$(parser, RIOT.getContext(), RulesJavacc::parseData, null);
    }

    public static RelStore data(String...facts) {
        RelStoreBuilder builder = RelStoreFactory.create();
        for ( String s : facts) {
            Rel rel = parseRel(s);
            builder.add(rel);
        }
        return builder.build();
    }

    // Convenience
    private static <X> X parse$(RulesJavacc parser, Context context, ParserAction<X> action, String baseURI) {
        PrefixMap prefixMap = PrefixMapFactory.create();
        addStandardPrefixes(prefixMap);
        return parse$(parser, context, action, baseURI, prefixMap);
    }

    private static <X> X parse$(RulesJavacc parser, Context context, ParserAction<X> action, String baseURI, PrefixMap prefixMap) {
        IRIx base = (baseURI!=null) ? IRIs.reference(baseURI) : null;
        IRIxResolver resolver = IRIxResolver.create(base).resolve(true).allowRelative(false).build();
        ParserProfile profile = new ParserProfileStd(RiotLib.factoryRDF(),
                                                     ErrorHandlerFactory.errorHandlerStd,
                                                     resolver,
                                                     prefixMap,
                                                     context, false, false);
        parser.setProfile(profile);
        try {
            return action.parse(parser);
        } catch (ParseException ex) {
            throw new RuleParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
        }
        catch ( TokenMgrError tErr) {
            int col = parser.token.endColumn ;
            int line = parser.token.endLine ;
            throw new RuleParseException(tErr.getMessage(), line, col) ;
        }
    }

    private static void addStandardPrefixes(PrefixMap prefixMap) {
        /** Update {@link PrefixMap} with the SHACLC standard prefixes */
        prefixMap.add("rdf",  RDF.getURI());
        prefixMap.add("rdfs", RDFS.getURI());
        prefixMap.add("sh",   SHACL.getURI());
        prefixMap.add("xsd",  XSD.getURI());
        // And for development data
        prefixMap.add("",     "http://example/");
    }
}
