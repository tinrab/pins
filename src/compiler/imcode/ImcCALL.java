package compiler.imcode;

import compiler.*;
import compiler.frames.*;
import java.util.*;

public class ImcCALL extends ImcExpr {

  public FrmLabel label;

  public LinkedList<ImcExpr> args;

  public ImcCALL(FrmLabel label) {
    this.label = label;
    this.args = new LinkedList<ImcExpr>();
  }

  @Override
  public void dump(int indent) {
    Report.dump(indent, "CALL label=" + label.name());
    Iterator<ImcExpr> args = this.args.iterator();
    while (args.hasNext()) {
      ImcExpr arg = args.next();
      arg.dump(indent + 2);
    }
  }

  @Override
  public ImcESEQ linear() {
    ImcSEQ linStmt = new ImcSEQ();
    ImcCALL linCall = new ImcCALL(label);
    Iterator<ImcExpr> args = this.args.iterator();
    while (args.hasNext()) {
      FrmTemp temp = new FrmTemp();
      ImcExpr arg = args.next();
      ImcESEQ linArg = arg.linear();
      linStmt.stmts.addAll(((ImcSEQ)linArg.stmt).stmts);
      linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linArg.expr));
      linCall.args.add(new ImcTEMP(temp));
    }
    FrmTemp temp = new FrmTemp();
    linStmt.stmts.add(new ImcMOVE(new ImcTEMP(temp), linCall));
    return new ImcESEQ(linStmt, new ImcTEMP(temp));
  }
}
