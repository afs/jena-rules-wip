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

package org.seaborne.jena.shacl_rules;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Prologue;
import org.seaborne.jena.shacl_rules.lang.ElementRule;
import org.seaborne.jena.shacl_rules.lang.parser.ShaclRulesParser;

public class ParserShaclRules {

    public static RuleSet parse(String string) {
        Reader in = new StringReader(string);
        ShaclRulesParser parser = new ShaclRulesParser(in);
        // XXX Change to prefix map.
        Prologue prologue = new Prologue();
        parser.setPrologue(prologue);

        try {
            parser.RulesUnit();
            List<ElementRule> rules = parser.getRules();
            RuleSet ruleSet = new RuleSet(prologue, rules);
            return ruleSet;
        } catch (org.seaborne.jena.shacl_rules.lang.parser.ParseException ex) {
            throw new QueryParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginColumn);
        } catch (org.apache.jena.sparql.lang.sparql_12.TokenMgrError tErr) {
            // Last valid token : not the same as token error message - but this
            // should not happen
            int col = parser.token.endColumn;
            int line = parser.token.endLine;
            throw new QueryParseException(tErr.getMessage(), line, col);
        } catch (QueryException ex) {
            throw ex;
        } catch (JenaException ex) {
            throw new QueryException(ex.getMessage(), ex);
        } catch (Error err) {
            // The token stream can throw errors.
            throw new QueryParseException(err.getMessage(), err, -1, -1);
        } catch (Throwable th) {
            Log.warn(ParserShaclRules.class, "Unexpected throwable: ", th);
            throw new QueryException(th.getMessage(), th);
        }
    }
}
