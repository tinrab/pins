package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcJUMP extends ImcStmt {

  public FrmLabel label;

  public ImcJUMP(FrmLabel label) { this.label = label; }

  @Override
  public void dump(int indent) {
    Report.dump(indent, "JUMP label=" + label.name());
  }

  @Override
  public ImcSEQ linear() {
    ImcSEQ lin = new ImcSEQ();
    lin.stmts.add(this);
    return lin;
  }
}
