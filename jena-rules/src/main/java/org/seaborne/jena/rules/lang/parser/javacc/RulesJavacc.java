/* RulesJavacc.java */
/* Generated By:JavaCC: Do not edit this line. RulesJavacc.java */
/**
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

package org.seaborne.jena.rules.lang.parser.javacc;

import org.apache.jena.graph.*;
import org.seaborne.jena.rules.lang.parser.RulesParserBase;
import org.apache.jena.sparql.core.Var ;
import org.seaborne.jena.rules.*;
import org.seaborne.jena.rules.store.*;
import static org.apache.jena.riot.lang.extra.LangParserLib.*;

public class RulesJavacc extends RulesParserBase implements RulesJavaccConstants {

// Entry points

// A whole ruleset
  final public RuleSet parseRuleSet() throws ParseException {RuleSet ruleSet;
    ByteOrderMark();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case BASE:
      case PREFIX:{

        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      Directive();
    }
    ruleSet = RuleSet();
    jj_consume_token(0);
{if ("" != null) return ruleSet;}
    throw new Error("Missing return statement in function");
}

  final public Rel parseAtom() throws ParseException {Rel rel;
    ByteOrderMark();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case BASE:
      case PREFIX:{

        break;
        }
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      Directive();
    }
    rel = Atom();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case DOT:{
      jj_consume_token(DOT);
      break;
      }
    default:
      jj_la1[2] = jj_gen;

    }
    jj_consume_token(0);
{if ("" != null) return rel;}
    throw new Error("Missing return statement in function");
}

  final public Rule parseRule() throws ParseException {Rule rule;
    ByteOrderMark();
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case BASE:
      case PREFIX:{

        break;
        }
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      Directive();
    }
    rule = Rule();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case DOT:{
      jj_consume_token(DOT);
      break;
      }
    default:
      jj_la1[4] = jj_gen;

    }
    jj_consume_token(0);
{if ("" != null) return rule;}
    throw new Error("Missing return statement in function");
}

  final public RelStore parseData() throws ParseException {Rel atom; RelStoreBuilder builder = RelStoreSimple.create();
    ByteOrderMark();
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case BASE:
      case PREFIX:{

        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
      Directive();
    }
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NAME:
      case LPAREN:{

        break;
        }
      default:
        jj_la1[6] = jj_gen;
        break label_5;
      }
      atom = Atom();
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case DOT:{
        jj_consume_token(DOT);
        break;
        }
      default:
        jj_la1[7] = jj_gen;

      }
builder.add(atom);
    }
    jj_consume_token(0);
{if ("" != null) return builder.build();}
    throw new Error("Missing return statement in function");
}

// ----
  final public 
void ByteOrderMark() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case BOM:{
      jj_consume_token(BOM);
      break;
      }
    default:
      jj_la1[8] = jj_gen;

    }
}

// Turtle [3] directive
// Rules: SPARQL style only.
// Rules: Must be first.
  final public 
void Directive() throws ParseException {Token t ; String iri ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case PREFIX:{
      jj_consume_token(PREFIX);
      t = jj_consume_token(PNAME_NS);
      iri = IRIREF();
String s = canonicalPrefix(t.image, t.beginLine, t.beginColumn) ;
      setPrefix(s, iri, t.beginLine, t.beginColumn) ;
      break;
      }
    case BASE:{
      t = jj_consume_token(BASE);
      iri = IRIREF();
setBase(iri, t.beginLine, t.beginColumn) ;
      break;
      }
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
}

  final public RuleSet RuleSet() throws ParseException {Rule rule; RuleSet ruleSet;
startRuleSet();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NAME:
      case LPAREN:{

        break;
        }
      default:
        jj_la1[10] = jj_gen;
        break label_6;
      }
      rule = Rule();
      jj_consume_token(DOT);
accumulateRule(rule);
    }
ruleSet = finishRuleSet();
      {if ("" != null) return ruleSet;}
    throw new Error("Missing return statement in function");
}

  final public Rule Rule() throws ParseException {Rel a;
startRule();
    a = Atom();
ruleHead(a);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case RDEF:
    case BCK_ARROW:{
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case RDEF:{
        jj_consume_token(RDEF);
        break;
        }
      case BCK_ARROW:{
        jj_consume_token(BCK_ARROW);
        break;
        }
      default:
        jj_la1[11] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NAME:
      case LPAREN:{
        a = Atom();
ruleBodyAtom(a);
        label_7:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case NAME:
          case LPAREN:
          case COMMA:{

            break;
            }
          default:
            jj_la1[12] = jj_gen;
            break label_7;
          }
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case COMMA:{
            jj_consume_token(COMMA);
            break;
            }
          default:
            jj_la1[13] = jj_gen;

          }
          a = Atom();
ruleBodyAtom(a);
        }
        break;
        }
      default:
        jj_la1[14] = jj_gen;

      }
      break;
      }
    default:
      jj_la1[15] = jj_gen;

    }
Rule rule = finishRule();
     {if ("" != null) return rule;}
    throw new Error("Missing return statement in function");
}

  final public Rel Atom() throws ParseException {Token t ; String name = "" ; Node termOrVar ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case NAME:{
      t = jj_consume_token(NAME);
name = t.image;
      break;
      }
    default:
      jj_la1[16] = jj_gen;

    }
    jj_consume_token(LPAREN);
startAtom(name);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case TRUE:
    case FALSE:
    case UNDERSCORE:
    case IRIref:
    case INTEGER:
    case DECIMAL:
    case DOUBLE:
    case STRING_LITERAL1:
    case STRING_LITERAL2:
    case STRING_LITERAL_LONG1:
    case STRING_LITERAL_LONG2:
    case LT2:
    case PNAME_NS:
    case PNAME_LN:
    case VAR1:
    case VAR2:{
      termOrVar = TermOrVar();
atomTerm(termOrVar) ;
      label_8:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case TRUE:
        case FALSE:
        case UNDERSCORE:
        case IRIref:
        case INTEGER:
        case DECIMAL:
        case DOUBLE:
        case STRING_LITERAL1:
        case STRING_LITERAL2:
        case STRING_LITERAL_LONG1:
        case STRING_LITERAL_LONG2:
        case COMMA:
        case LT2:
        case PNAME_NS:
        case PNAME_LN:
        case VAR1:
        case VAR2:{

          break;
          }
        default:
          jj_la1[17] = jj_gen;
          break label_8;
        }
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case COMMA:{
          jj_consume_token(COMMA);
          break;
          }
        default:
          jj_la1[18] = jj_gen;

        }
        termOrVar = TermOrVar();
atomTerm(termOrVar) ;
      }
      break;
      }
    default:
      jj_la1[19] = jj_gen;

    }
    jj_consume_token(RPAREN);
Rel atom = finishAtom();
     {if ("" != null) return atom;}
    throw new Error("Missing return statement in function");
}

  final public Node TermOrVar() throws ParseException {Node n ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case TRUE:
    case FALSE:
    case UNDERSCORE:
    case IRIref:
    case INTEGER:
    case DECIMAL:
    case DOUBLE:
    case STRING_LITERAL1:
    case STRING_LITERAL2:
    case STRING_LITERAL_LONG1:
    case STRING_LITERAL_LONG2:
    case LT2:
    case PNAME_NS:
    case PNAME_LN:{
      n = Term();
      break;
      }
    case VAR1:
    case VAR2:{
      n = Var();
      break;
      }
    default:
      jj_la1[20] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return n ;}
    throw new Error("Missing return statement in function");
}

  final public Node Term() throws ParseException {Node term ; String iri;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case IRIref:
    case PNAME_NS:
    case PNAME_LN:{
      iri = iri();
term = createURI(iri, token.beginLine, token.beginColumn) ;
      break;
      }
    case TRUE:
    case FALSE:
    case INTEGER:
    case DECIMAL:
    case DOUBLE:
    case STRING_LITERAL1:
    case STRING_LITERAL2:
    case STRING_LITERAL_LONG1:
    case STRING_LITERAL_LONG2:{
      term = Literal();
      break;
      }
    case LT2:{
      term = TripleStar();
      break;
      }
    case UNDERSCORE:{
      jj_consume_token(UNDERSCORE);
term = Node.ANY;
      break;
      }
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return term ;}
    throw new Error("Missing return statement in function");
}

  final public Var Var() throws ParseException {Token t ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case VAR1:{
      t = jj_consume_token(VAR1);
      break;
      }
    case VAR2:{
      t = jj_consume_token(VAR2);
      break;
      }
    default:
      jj_la1[22] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return createVariable(t.image, t.beginLine, t.beginColumn) ;}
    throw new Error("Missing return statement in function");
}

  final public Node TripleStar() throws ParseException {Node s , p , o ; Token t ;
    t = jj_consume_token(LT2);
int beginLine = t.beginLine; int beginColumn = t.beginColumn; t = null;
    //  s = Subject()
    //  p = Predicate()
    //  o = ObjectX()
      s = TermOrVar();
    p = TermOrVar();
    o = TermOrVar();
    jj_consume_token(GT2);
checkTripleTerm(s, p, o, beginLine, beginColumn);
    Node n = createQuotedTriple(s, p, o, beginLine, beginColumn);
    {if ("" != null) return n;}
    throw new Error("Missing return statement in function");
}

// Turtle [13] literal
  final public Node Literal() throws ParseException {Node n ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case STRING_LITERAL1:
    case STRING_LITERAL2:
    case STRING_LITERAL_LONG1:
    case STRING_LITERAL_LONG2:{
      n = RDFLiteral();
{if ("" != null) return n ;}
      break;
      }
    case INTEGER:
    case DECIMAL:
    case DOUBLE:{
      n = NumericLiteral();
{if ("" != null) return n ;}
      break;
      }
    case TRUE:
    case FALSE:{
      n = BooleanLiteral();
{if ("" != null) return n ;}
      break;
      }
    default:
      jj_la1[23] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

// Turtle [16] NumericLiteral
  final public Node NumericLiteral() throws ParseException {Token t ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case INTEGER:{
      t = jj_consume_token(INTEGER);
{if ("" != null) return createLiteralInteger(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    case DECIMAL:{
      t = jj_consume_token(DECIMAL);
{if ("" != null) return createLiteralDecimal(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    case DOUBLE:{
      t = jj_consume_token(DOUBLE);
{if ("" != null) return createLiteralDouble(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    default:
      jj_la1[24] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

// Turtle [128s] RDFLiteral
  final public Node RDFLiteral() throws ParseException {Token t ; String lex = null ;
    lex = String();
String lang = null ; String uri = null ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case BASE:
    case PREFIX:
    case DATATYPE:
    case LANGTAG:{
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case BASE:
      case PREFIX:
      case LANGTAG:{
        lang = LangTag();
        break;
        }
      case DATATYPE:{
        jj_consume_token(DATATYPE);
        uri = iri();
        break;
        }
      default:
        jj_la1[25] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
      }
    default:
      jj_la1[26] = jj_gen;

    }
{if ("" != null) return createLiteral(lex, lang, uri, token.beginLine, token.beginColumn) ;}
    throw new Error("Missing return statement in function");
}

  final public String LangTag() throws ParseException {Token t ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case LANGTAG:{
      t = jj_consume_token(LANGTAG);
      break;
      }
    case BASE:
    case PREFIX:{
      t = AnyDirective();
      break;
      }
    default:
      jj_la1[27] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
String lang = stripChars(t.image, 1) ; {if ("" != null) return lang ;}
    throw new Error("Missing return statement in function");
}

  final public Token AnyDirective() throws ParseException {Token t ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case PREFIX:{
      t = jj_consume_token(PREFIX);
      break;
      }
    case BASE:{
      t = jj_consume_token(BASE);
      break;
      }
    default:
      jj_la1[28] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
{if ("" != null) return t ;}
    throw new Error("Missing return statement in function");
}

// Turtle [133s] BooleanLiteral
  final public Node BooleanLiteral() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case TRUE:{
      jj_consume_token(TRUE);
{if ("" != null) return XSD_TRUE ;}
      break;
      }
    case FALSE:{
      jj_consume_token(FALSE);
{if ("" != null) return XSD_FALSE ;}
      break;
      }
    default:
      jj_la1[29] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

// Turtle [17] String
  final public String String() throws ParseException {Token t ; String lex ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case STRING_LITERAL1:{
      t = jj_consume_token(STRING_LITERAL1);
lex = stripQuotes(t.image) ;
      break;
      }
    case STRING_LITERAL2:{
      t = jj_consume_token(STRING_LITERAL2);
lex = stripQuotes(t.image) ;
      break;
      }
    case STRING_LITERAL_LONG1:{
      t = jj_consume_token(STRING_LITERAL_LONG1);
lex = stripQuotes3(t.image) ;
      break;
      }
    case STRING_LITERAL_LONG2:{
      t = jj_consume_token(STRING_LITERAL_LONG2);
lex = stripQuotes3(t.image) ;
      break;
      }
    default:
      jj_la1[30] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
checkString(lex, t.beginLine, t.beginColumn) ;
      lex = unescapeStr(lex,  t.beginLine, t.beginColumn) ;
      {if ("" != null) return lex ;}
    throw new Error("Missing return statement in function");
}

// Turtle [135s] iri
  final public String iri() throws ParseException {String iri ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case IRIref:{
      iri = IRIREF();
{if ("" != null) return iri ;}
      break;
      }
    case PNAME_NS:
    case PNAME_LN:{
      iri = PrefixedName();
{if ("" != null) return iri ;}
      break;
      }
    default:
      jj_la1[31] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

// Turtle [136s] PrefixedName
  final public String PrefixedName() throws ParseException {Token t ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case PNAME_LN:{
      t = jj_consume_token(PNAME_LN);
{if ("" != null) return resolvePName(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    case PNAME_NS:{
      t = jj_consume_token(PNAME_NS);
{if ("" != null) return resolvePName(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    default:
      jj_la1[32] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

// Turtle [137s] BlankNode
  final public Node BlankNode() throws ParseException {Token t = null ;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case BLANK_NODE_LABEL:{
      t = jj_consume_token(BLANK_NODE_LABEL);
{if ("" != null) return createBNode(t.image, t.beginLine, t.beginColumn) ;}
      break;
      }
    case ANON:{
      t = jj_consume_token(ANON);
{if ("" != null) return createBNode(t.beginLine, t.beginColumn) ;}
      break;
      }
    default:
      jj_la1[33] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

  final public String IRIREF() throws ParseException {Token t ;
    t = jj_consume_token(IRIref);
{if ("" != null) return resolveQuotedIRI(t.image, t.beginLine, t.beginColumn) ;}
    throw new Error("Missing return statement in function");
}

  /** Generated Token Manager. */
  public RulesJavaccTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[34];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static private int[] jj_la1_2;
  static {
	   jj_la1_init_0();
	   jj_la1_init_1();
	   jj_la1_init_2();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x600,0x600,0x0,0x600,0x0,0x600,0x10000,0x0,0x4000,0x600,0x10000,0x0,0x10000,0x0,0x10000,0x0,0x10000,0x80e29800,0x0,0x80e29800,0x80e29800,0x80e29800,0x0,0x80e01800,0xe00000,0x600,0x600,0x600,0x600,0x1800,0x80000000,0x20000,0x0,0x0,};
	}
	private static void jj_la1_init_1() {
	   jj_la1_1 = new int[] {0x0,0x0,0x1000,0x0,0x1000,0x0,0x8,0x1000,0x0,0x0,0x8,0x60000,0x808,0x800,0x8,0x60000,0x0,0xc02807,0x800,0xc02007,0xc02007,0xc02007,0x0,0x7,0x0,0x2100000,0x2100000,0x2000000,0x0,0x0,0x7,0xc00000,0xc00000,0x1000200,};
	}
	private static void jj_la1_init_2() {
	   jj_la1_2 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0xc,0x0,0xc,0xc,0x0,0xc,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,};
	}

  /** Constructor with InputStream. */
  public RulesJavacc(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public RulesJavacc(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new RulesJavaccTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public RulesJavacc(java.io.Reader stream) {
	 jj_input_stream = new SimpleCharStream(stream, 1, 1);
	 token_source = new RulesJavaccTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new SimpleCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new RulesJavaccTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public RulesJavacc(RulesJavaccTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(RulesJavaccTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 34; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
	 Token oldToken;
	 if ((oldToken = token).next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 if (token.kind == kind) {
	   jj_gen++;
	   return token;
	 }
	 token = oldToken;
	 jj_kind = kind;
	 throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[74];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 34; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		   if ((jj_la1_1[i] & (1<<j)) != 0) {
			 la1tokens[32+j] = true;
		   }
		   if ((jj_la1_2[i] & (1<<j)) != 0) {
			 la1tokens[64+j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 74; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

 }
