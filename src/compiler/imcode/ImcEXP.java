package compiler.imcode;

import compiler.*;

public class ImcEXP extends ImcStmt {

  public ImcExpr expr;

  public ImcEXP(ImcExpr expr) { this.expr = expr; }

  @Override
  public void dump(int indent) {
    Report.dump(indent, "EXP");
    expr.dump(indent + 2);
  }

  @Override
  public ImcSEQ linear() {
    ImcSEQ lin = new ImcSEQ();
    ImcESEQ linExpr = expr.linear();
    lin.stmts.addAll(((ImcSEQ)linExpr.stmt).stmts);
    lin.stmts.add(new ImcEXP(linExpr.expr));
    return lin;
  }
}
