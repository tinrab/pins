package compiler.synan;

import compiler.Position;
import compiler.Report;
import compiler.abstr.tree.*;
import compiler.lexan.LexAn;
import compiler.lexan.Symbol;
import compiler.lexan.Token;
import java.util.Vector;

public class SynAn {

  private LexAn lexAn;
  private Symbol currentSymbol, nextSymbol;
  private boolean dump;

  public SynAn(LexAn lexAn, boolean dump) {
    this.lexAn = lexAn;
    this.dump = dump;
  }

  private void check(int... tokens) {
    for (int t : tokens) {
      if (nextSymbol.token == t) {
        currentSymbol = nextSymbol;
        nextSymbol = lexAn.lexAn();
      } else {
        String e = String.format(
            "Expected %s, got %s('%s')", Token.getTokenName(t),
            Token.getTokenName(nextSymbol.token), nextSymbol.lexeme);
        Report.error(currentSymbol.position, e);
      }
    }
  }

  private boolean checkIfNext(int... tokens) {
    for (int t : tokens) {
      if (peek() == t) {
        check(t);
        return true;
      }
    }

    return false;
  }

  private int peek() { return nextSymbol.token; }

  public AbsTree parse() {
    dump("source -> definitions");
    nextSymbol = lexAn.lexAn();

    if (nextSymbol.token == Token.EOF) {
      Report.error("Source file is empty");
    }

    AbsDefs defs = parseDefinitions();

    if (nextSymbol.token != Token.EOF) {
      String e = String.format("Syntax error on token '%s', delete this token",
                               nextSymbol.lexeme);
      Report.error(nextSymbol.position, e);
    }

    return defs;
  }

  private AbsDefs parseDefinitions() {
    Vector<AbsDef> defs = new Vector<AbsDef>();

    dump("definitions -> definition definitions-tail");
    defs.add(parseDefinition());

    while (checkIfNext(Token.SEMIC)) {
      dump("definitions-tail -> ; definition definitions-tail");
      defs.add(parseDefinition());
    }

    Position f = defs.get(0).position;
    int begLine = f.begLine;
    int begColumn = f.begColumn;

    Position l = defs.lastElement().position;
    int endLine = l.endLine;
    int endColumn = l.endColumn;

    return new AbsDefs(new Position(begLine, begColumn, endLine, endColumn),
                       defs);
  }

  private AbsDef parseDefinition() {
    switch (peek()) {
    case Token.KW_VAR:
      dump("definition -> variable-definition");
      return parseVariableDefinition();
    case Token.KW_FUN:
      dump("definition -> function-definition");
      return parseFunctionDefinition();
    case Token.KW_TYP:
      dump("definition -> type-definition");
      return parseTypeDefinition();
    default:
      if (currentSymbol == null) {
        currentSymbol = nextSymbol;
      }

      String e = String.format("Syntax error on token '%s', delete this token",
                               currentSymbol.lexeme);
      Report.error(currentSymbol.position, e);
      return null;
    }
  }

  private AbsTypeDef parseTypeDefinition() {
    dump("type-definition -> typ IDENTIFIER : type");

    check(Token.KW_TYP);

    int begLine = currentSymbol.position.begLine;
    int begColumn = currentSymbol.position.begColumn;

    check(Token.IDENTIFIER);
    String name = currentSymbol.lexeme;
    check(Token.COLON);
    AbsType type = parseType();

    int endLine = type.position.endLine;
    int endColumn = type.position.endColumn;

    return new AbsTypeDef(new Position(begLine, begColumn, endLine, endColumn),
                          name, type);
  }

  private AbsFunDef parseFunctionDefinition() {
    dump("function-definition -> fun IDENTIFIER ( parameters ) : type = " +
         "expression");

    check(Token.KW_FUN);
    int begLine = currentSymbol.position.begLine;
    int begColumn = currentSymbol.position.begColumn;

    check(Token.IDENTIFIER);
    String name = currentSymbol.lexeme;

    check(Token.LPARENT);
    Vector<AbsPar> pars = parseParameters();
    check(Token.RPARENT, Token.COLON);
    AbsType type = parseType();
    check(Token.ASSIGN);
    AbsExpr expr = parseExpression();

    int endLine = expr.position.endLine;
    int endColumn = expr.position.endColumn;

    return new AbsFunDef(new Position(begLine, begColumn, endLine, endColumn),
                         name, pars, type, expr);
  }

  private AbsVarDef parseVariableDefinition() {
    dump("variable-definition -> var IDENTIFIER : type");

    check(Token.KW_VAR);
    int begLine = currentSymbol.position.begLine;
    int begColumn = currentSymbol.position.begColumn;

    check(Token.IDENTIFIER);
    String name = currentSymbol.lexeme;

    check(Token.COLON);
    AbsType type = parseType();

    int endLine = type.position.endLine;
    int endColumn = type.position.endColumn;

    return new AbsVarDef(new Position(begLine, begColumn, endLine, endColumn),
                         name, type);
  }

  private AbsType parseType() {
    if (checkIfNext(Token.IDENTIFIER)) {
      dump("type -> IDENTIFIER");
      return new AbsTypeName(currentSymbol.position, currentSymbol.lexeme);
    }
    if (checkIfNext(Token.LOGICAL)) {
      dump("type -> LOGICAL");
      return new AbsAtomType(currentSymbol.position, AbsAtomType.LOG);
    }

    if (checkIfNext(Token.INTEGER)) {
      dump("type -> INTEGER");
      return new AbsAtomType(currentSymbol.position, AbsAtomType.INT);
    }

    if (checkIfNext(Token.STRING)) {
      dump("type -> STRING");
      return new AbsAtomType(currentSymbol.position, AbsAtomType.STR);
    }

    dump("type -> arr [ INT_CONST ] type");

    check(Token.KW_ARR);
    int begLine = currentSymbol.position.begLine;
    int begColumn = currentSymbol.position.begColumn;

    check(Token.LBRACKET, Token.INT_CONST);
    int length = Integer.parseInt(currentSymbol.lexeme);
    check(Token.RBRACKET);

    AbsType type = parseType();
    int endLine = type.position.endLine;
    int endColumn = type.position.endColumn;

    return new AbsArrType(new Position(begLine, begColumn, endLine, endColumn),
                          length, type);
  }

  private Vector<AbsPar> parseParameters() {
    dump("parameters -> parameter parameters-tail");

    Vector<AbsPar> pars = new Vector<AbsPar>();
    pars.add(parseParameter());

    while (checkIfNext(Token.COMMA)) {
      dump("parameters -> , parameters parameters-tail");
      pars.add(parseParameter());
    }

    return pars;
  }

  private AbsPar parseParameter() {
    dump("parameter -> IDENTIFIER : type");

    check(Token.IDENTIFIER);
    String name = currentSymbol.lexeme;
    int begLine = currentSymbol.position.begLine;
    int begColumn = currentSymbol.position.begColumn;

    check(Token.COLON);
    AbsType type = parseType();
    int endLine = type.position.endLine;
    int endColumn = type.position.endColumn;

    return new AbsPar(new Position(begLine, begColumn, endLine, endColumn),
                      name, type);
  }

  private AbsExpr parseExpression() {
    dump("expression -> logical-or-expression");
    AbsExpr left = parseLogicalOrExpression();

    int begLine = left.position.begLine;
    int begColumn = left.position.begColumn;

    if (checkIfNext(Token.LBRACE)) {
      dump("expression -> logical-or-expression { where definitions }");
      check(Token.KW_WHERE);
      AbsDefs defs = parseDefinitions();
      check(Token.RBRACE);

      int endLine = currentSymbol.position.endLine;
      int endColumn = currentSymbol.position.endColumn;

      return new AbsWhere(new Position(begLine, begColumn, endLine, endColumn),
                          left, defs);
    }

    return left;
  }

  private AbsExpr parseLogicalOrExpression() {
    dump("logical-or-expression -> logical-and-expression " +
         "logical-or-expression-tail");

    AbsExpr left = parseLogicalAndExpression();
    AbsExpr right = parseLogicalOrExpressionTail();

    if (right == null) {
      return left;
    }

    int begLine = left.position.begLine;
    int begColumn = left.position.begColumn;
    int endLine = right.position.endLine;
    int endColumn = right.position.endColumn;

    return new AbsBinExpr(new Position(begLine, begColumn, endLine, endColumn),
                          AbsBinExpr.IOR, left, right);
  }

  private AbsExpr parseLogicalOrExpressionTail() {
    if (checkIfNext(Token.IOR)) {
      AbsExpr left = parseLogicalAndExpression();
      AbsExpr right = parseLogicalOrExpressionTail();

      if (right == null) {
        return left;
      }

      int begLine = left.position.begLine;
      int begColumn = left.position.begColumn;
      int endLine = right.position.endLine;
      int endColumn = right.position.endColumn;

      return new AbsBinExpr(
          new Position(begLine, begColumn, endLine, endColumn), AbsBinExpr.IOR,
          left, right);
    }

    return null;
  }

  private AbsExpr parseLogicalAndExpression() {
    dump("logical-and-expression -> compare-expression " +
         "logical-and-expression-tail");

    AbsExpr left = parseCompareExpression();
    AbsExpr right = parseLogicalAndExpressionTail();

    if (right == null) {
      return left;
    }

    int begLine = left.position.begLine;
    int begColumn = left.position.begColumn;
    int endLine = right.position.endLine;
    int endColumn = right.position.endColumn;

    return new AbsBinExpr(new Position(begLine, begColumn, endLine, endColumn),
                          AbsBinExpr.AND, left, right);
  }

  private AbsExpr parseLogicalAndExpressionTail() {
    if (checkIfNext(Token.AND)) {
      dump("logical-and-expression -> & compare-expression " +
           "logical-and-expression");

      AbsExpr left = parseCompareExpression();
      AbsExpr right = parseLogicalAndExpressionTail();

      if (right == null) {
        return left;
      }

      int begLine = left.position.begLine;
      int begColumn = left.position.begColumn;
      int endLine = right.position.endLine;
      int endColumn = right.position.endColumn;

      return new AbsBinExpr(
          new Position(begLine, begColumn, endLine, endColumn), AbsBinExpr.AND,
          left, right);
    }

    return null;
  }

  private AbsExpr parseCompareExpression() {
    dump("compare-expression -> additive-expression");
    AbsExpr left = parseAdditiveExpression();

    int p = peek();

    if (checkIfNext(Token.EQU, Token.NEQ, Token.LEQ, Token.GEQ, Token.LTH,
                    Token.GTH)) {
      String t = "";
      int oper = 0;

      switch (p) {
      case Token.EQU:
        t = "=";
        oper = AbsBinExpr.EQU;
        break;
      case Token.NEQ:
        t = "!=";
        oper = AbsBinExpr.NEQ;
        break;
      case Token.LEQ:
        t = "<=";
        oper = AbsBinExpr.LEQ;
        break;
      case Token.GEQ:
        t = ">=";
        oper = AbsBinExpr.GEQ;
        break;
      case Token.LTH:
        t = "<";
        oper = AbsBinExpr.LTH;
        break;
      case Token.GTH:
        t = ">";
        oper = AbsBinExpr.GTH;
        break;
      }

      dump("compare-expression -> additive-expression " + t +
           " additive-expression");

      AbsExpr right = parseAdditiveExpression();
      int begLine = left.position.begLine;
      int begColumn = left.position.begColumn;
      int endLine = right.position.endLine;
      int endColumn = right.position.endColumn;

      return new AbsBinExpr(
          new Position(begLine, begColumn, endLine, endColumn), oper, left,
          right);
    }

    return left;
  }

  private AbsExpr parseAdditiveExpression() {
    dump("additive-expression -> multiplicative-expression " +
         "additive-expression-tail");

    AbsExpr expr = parseMultiplicativeExpression();
    int begLine = expr.position.begLine;
    int begColumn = expr.position.begColumn;
    int endLine = expr.position.endLine;
    int endColumn = expr.position.endColumn;

    while (true) {
      if (checkIfNext(Token.ADD, Token.SUB)) {
        dump("additive-expression-tail -> " +
             (currentSymbol.token == Token.ADD ? "+" : "-") +
             " multiplicative-expression additive-expression-tail");

        int oper =
            currentSymbol.token == Token.ADD ? AbsBinExpr.ADD : AbsBinExpr.SUB;
        AbsExpr left = expr;
        AbsExpr right = parseMultiplicativeExpression();

        endLine = right.position.endLine;
        endColumn = right.position.endColumn;

        expr =
            new AbsBinExpr(new Position(begLine, begColumn, endLine, endColumn),
                           oper, left, right);
      } else {
        break;
      }
    }

    return expr;
  }

  private AbsExpr parseMultiplicativeExpression() {
    dump("multiplicative-expression -> prefix-expression " +
         "multiplicative-expression-tail");

    AbsExpr expr = parsePrefixExpression();
    int begLine = expr.position.begLine;
    int begColumn = expr.position.begColumn;
    int endLine = expr.position.endLine;
    int endColumn = expr.position.endColumn;

    while (true) {
      if (checkIfNext(Token.MUL, Token.DIV, Token.MOD)) {
        int t = currentSymbol.token;
        dump("multiplicative-expression-tail -> " +
             (t == Token.MUL   ? "*"
              : t == Token.DIV ? "/"
                               : "%") +
             " prefix-expression multiplicative-expression-tail");

        int oper = t == Token.MUL   ? AbsBinExpr.MUL
                   : t == Token.DIV ? AbsBinExpr.DIV
                                    : AbsBinExpr.MOD;
        AbsExpr left = expr;
        AbsExpr right = parsePrefixExpression();

        endLine = right.position.endLine;
        endColumn = right.position.endColumn;

        expr =
            new AbsBinExpr(new Position(begLine, begColumn, endLine, endColumn),
                           oper, left, right);
      } else {
        break;
      }
    }

    return expr;
  }

  private AbsExpr parsePrefixExpression() {
    if (checkIfNext(Token.ADD, Token.SUB, Token.NOT)) {
      int t = currentSymbol.token;
      dump("prefix-expression -> " +
           (t == Token.ADD   ? "+"
            : t == Token.SUB ? "-"
                             : "!") +
           " prefix-expression");

      int oper = t == Token.SUB   ? AbsUnExpr.SUB
                 : t == Token.ADD ? AbsUnExpr.ADD
                                  : AbsUnExpr.NOT;
      AbsExpr expr = parsePrefixExpression();

      return new AbsUnExpr(expr.position, oper, expr);
    } else {
      dump("prefix-expression -> postfix-expression");
      return parsePostfixExpression();
    }
  }

  private AbsExpr parsePostfixExpression() {
    dump("postfix-expression -> atom-expression postfix-expression-tail");

    AbsExpr expr = parseAtomExpression();
    int begLine = expr.position.begLine;
    int begColumn = expr.position.begColumn;
    int endLine = expr.position.endLine;
    int endColumn = expr.position.endColumn;

    while (true) {
      if (checkIfNext(Token.LBRACKET)) {
        dump("postfix-expression-tail -> [ expression ] " +
             "postfix-expression-tail");

        AbsExpr left = expr;
        AbsExpr right = parseExpression();
        check(Token.RBRACKET);

        endLine = currentSymbol.position.endLine;
        endColumn = currentSymbol.position.endColumn;

        expr =
            new AbsBinExpr(new Position(begLine, begColumn, endLine, endColumn),
                           AbsBinExpr.ARR, left, right);
      } else {
        break;
      }
    }

    return expr;
  }

  private AbsExpr parseAtomExpression() {
    if (checkIfNext(Token.LOG_CONST, Token.INT_CONST, Token.STR_CONST)) {
      int t = currentSymbol.token;

      dump("atom-expression -> " + Token.getTokenName(t));
      switch (currentSymbol.token) {
      case Token.LOG_CONST:
        return new AbsAtomConst(currentSymbol.position, AbsAtomConst.LOG,
                                currentSymbol.lexeme);
      case Token.INT_CONST:
        return new AbsAtomConst(currentSymbol.position, AbsAtomConst.INT,
                                currentSymbol.lexeme);
      case Token.STR_CONST:
        return new AbsAtomConst(currentSymbol.position, AbsAtomConst.STR,
                                currentSymbol.lexeme);
      default:
        return null;
      }
    } else {
      if (checkIfNext(Token.IDENTIFIER)) {
        String name = currentSymbol.lexeme;
        int begLine = currentSymbol.position.begLine;
        int begColumn = currentSymbol.position.begColumn;

        if (checkIfNext(Token.LPARENT)) {
          dump("atom-expression -> IDENTIFIER ( expressions )");
          Vector<AbsExpr> args = parseExpressions();
          check(Token.RPARENT);

          int endLine = currentSymbol.position.endLine;
          int endColumn = currentSymbol.position.endColumn;

          return new AbsFunCall(
              new Position(begLine, begColumn, endLine, endColumn), name, args);
        } else {
          dump("atom-expression -> IDENTIFIER");

          return new AbsVarName(new Position(begLine, begColumn,
                                             currentSymbol.position.endLine,
                                             currentSymbol.position.endColumn),
                                name);
        }
      } else if (checkIfNext(Token.LBRACE)) {
        int begLine = currentSymbol.position.begLine;
        int begColumn = currentSymbol.position.begColumn;

        if (checkIfNext(Token.KW_IF)) {
          dump("atom-expression -> { if expression then expression }");

          AbsExpr cond = parseExpression();

          check(Token.KW_THEN);
          AbsExpr thenBody = parseExpression();

          if (!checkIfNext(Token.RBRACE)) {
            dump("atom-expression -> { if expression then expression else " +
                 "expression }");

            check(Token.KW_ELSE);
            AbsExpr elseBody = parseExpression();
            check(Token.RBRACE);

            int endLine = currentSymbol.position.endLine;
            int endColumn = currentSymbol.position.endColumn;

            return new AbsIfThenElse(
                new Position(begLine, begColumn, endLine, endColumn), cond,
                thenBody, elseBody);
          } else {
            int endLine = currentSymbol.position.endLine;
            int endColumn = currentSymbol.position.endColumn;

            return new AbsIfThen(
                new Position(begLine, begColumn, endLine, endColumn), cond,
                thenBody);
          }
        } else if (checkIfNext(Token.KW_WHILE)) {
          dump("atom-expression -> { while expression : expression }");

          AbsExpr cond = parseExpression();
          check(Token.COLON);
          AbsExpr body = parseExpression();
          check(Token.RBRACE);

          int endLine = currentSymbol.position.endLine;
          int endColumn = currentSymbol.position.endColumn;

          return new AbsWhile(
              new Position(begLine, begColumn, endLine, endColumn), cond, body);
        } else if (checkIfNext(Token.KW_FOR)) {
          dump("atom-expression -> { for IDENTIFIER = expression , " +
               "expression , expression : expression }");

          check(Token.IDENTIFIER);
          AbsVarName count =
              new AbsVarName(currentSymbol.position, currentSymbol.lexeme);
          check(Token.ASSIGN);

          AbsExpr lo = parseExpression();
          check(Token.COMMA);
          AbsExpr hi = parseExpression();
          check(Token.COMMA);
          AbsExpr step = parseExpression();
          check(Token.COLON);
          AbsExpr body = parseExpression();
          check(Token.RBRACE);

          int endLine = currentSymbol.position.endLine;
          int endColumn = currentSymbol.position.endColumn;

          return new AbsFor(
              new Position(begLine, begColumn, endLine, endColumn), count, lo,
              hi, step, body);
        } else {
          dump("atom-expression -> { expression = expression }");

          AbsExpr left = parseExpression();
          check(Token.ASSIGN);
          AbsExpr right = parseExpression();
          check(Token.RBRACE);

          int endLine = currentSymbol.position.endLine;
          int endColumn = currentSymbol.position.endColumn;

          return new AbsBinExpr(
              new Position(begLine, begColumn, endLine, endColumn),
              AbsBinExpr.ASSIGN, left, right);
        }
      } else {
        check(Token.LPARENT);
        int begLine = currentSymbol.position.begLine;
        int begColumn = currentSymbol.position.begColumn;

        Vector<AbsExpr> exprs = parseExpressions();
        check(Token.RPARENT);

        int endLine = currentSymbol.position.endLine;
        int endColumn = currentSymbol.position.endColumn;

        return new AbsExprs(
            new Position(begLine, begColumn, endLine, endColumn), exprs);
      }
    }
  }

  private Vector<AbsExpr> parseExpressions() {
    dump("expressions -> expression expressions-tail");

    Vector<AbsExpr> exprs = new Vector<AbsExpr>();
    exprs.add(parseExpression());

    while (checkIfNext(Token.COMMA)) {
      dump("expressions-tail -> expression expressions-tail");

      exprs.add(parseExpression());
    }

    return exprs;
  }

  private void dump(String production) {
    if (!dump)
      return;
    if (Report.dumpFile() == null)
      return;
    Report.dumpFile().println(production);
  }
}
