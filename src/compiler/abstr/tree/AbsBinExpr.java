package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsBinExpr extends AbsExpr {

  public static final int IOR = 0;
  public static final int AND = 1;
  public static final int EQU = 2;
  public static final int NEQ = 3;
  public static final int LEQ = 4;
  public static final int GEQ = 5;
  public static final int LTH = 6;
  public static final int GTH = 7;
  public static final int ADD = 8;
  public static final int SUB = 9;
  public static final int MUL = 10;
  public static final int DIV = 11;
  public static final int MOD = 12;
  public static final int ARR = 14;
  public static final int ASSIGN = 15;

  public static String operatorToString(int oper) {
    if (oper >= 0 && oper <= 12) {
      return new String[] {"|", "&", "==", "!=", "<=", ">=", "<",
                           ">", "+", "-",  "*",  "/",  "%"}[oper];
    }

    return null;
  }

  public final int oper;

  public final AbsExpr expr1;

  public final AbsExpr expr2;

  public AbsBinExpr(Position pos, int oper, AbsExpr expr1, AbsExpr expr2) {
    super(pos);
    this.oper = oper;
    this.expr1 = expr1;
    this.expr2 = expr2;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
