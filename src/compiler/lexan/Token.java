package compiler.lexan;

import compiler.Report;

public class Token {

  public static final int EOF = 0;

  public static final int IDENTIFIER = 1;

  public static final int LOG_CONST = 2;
  public static final int INT_CONST = 3;
  public static final int STR_CONST = 4;

  public static final int AND = 5;
  public static final int IOR = 6;
  public static final int NOT = 7;

  public static final int EQU = 8;
  public static final int NEQ = 9;
  public static final int LTH = 10;
  public static final int GTH = 11;
  public static final int LEQ = 12;
  public static final int GEQ = 13;

  public static final int MUL = 14;
  public static final int DIV = 15;
  public static final int MOD = 16;
  public static final int ADD = 17;
  public static final int SUB = 18;

  public static final int LPARENT = 19;
  public static final int RPARENT = 20;
  public static final int LBRACKET = 21;
  public static final int RBRACKET = 22;
  public static final int LBRACE = 23;
  public static final int RBRACE = 24;

  public static final int DOT = 25;
  public static final int COLON = 26;
  public static final int SEMIC = 27;
  public static final int COMMA = 28;

  public static final int ASSIGN = 29;

  public static final int LOGICAL = 30;
  public static final int INTEGER = 31;
  public static final int STRING = 32;

  public static final int KW_ARR = 33;
  public static final int KW_ELSE = 34;
  public static final int KW_FOR = 35;
  public static final int KW_FUN = 36;
  public static final int KW_IF = 37;
  public static final int KW_THEN = 38;
  public static final int KW_TYP = 39;
  public static final int KW_VAR = 40;
  public static final int KW_WHERE = 41;
  public static final int KW_WHILE = 42;

  public static final int TOKEN_COUNT = 43;

  public static String getTokenName(int token) {
    switch (token) {

    case EOF:
      return "EOF";

    case IDENTIFIER:
      return "IDENTIFIER";

    case LOG_CONST:
      return "LOG_CONST";
    case INT_CONST:
      return "INT_CONST";
    case STR_CONST:
      return "STR_CONST";

    case AND:
      return "AND";
    case IOR:
      return "IOR";
    case NOT:
      return "NOT";

    case EQU:
      return "EQU";
    case NEQ:
      return "NEQ";
    case LTH:
      return "LTH";
    case GTH:
      return "GTH";
    case LEQ:
      return "LEQ";
    case GEQ:
      return "GEQ";

    case MUL:
      return "MUL";
    case DIV:
      return "DIV";
    case MOD:
      return "MOD";
    case ADD:
      return "ADD";
    case SUB:
      return "SUB";

    case LPARENT:
      return "LPARENT";
    case RPARENT:
      return "RPARENT";
    case LBRACKET:
      return "LBRACKET";
    case RBRACKET:
      return "RBRACKET";
    case LBRACE:
      return "LBRACE";
    case RBRACE:
      return "RBRACE";

    case DOT:
      return "DOT";
    case COLON:
      return "COLON";
    case SEMIC:
      return "SEMIC";
    case COMMA:
      return "COMMA";

    case ASSIGN:
      return "ASSIGN";

    case LOGICAL:
      return "LOGICAL";
    case INTEGER:
      return "INTEGER";
    case STRING:
      return "STRING";

    case KW_ARR:
      return "ARR";
    case KW_ELSE:
      return "ELSE";
    case KW_FOR:
      return "FOR";
    case KW_FUN:
      return "FUN";
    case KW_IF:
      return "IF";
    case KW_THEN:
      return "THEN";
    case KW_TYP:
      return "TYP";
    case KW_VAR:
      return "VAR";
    case KW_WHERE:
      return "WHERE";
    case KW_WHILE:
      return "WHILE";

    default:
      Report.error("Internal error: token=" + token +
                   " in compiler.lexan.Symbol.toString().");
      return null;
    }
  }
}
