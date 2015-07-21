package compiler.imcode;

import compiler.*;
import java.util.*;

public class ImcSEQ extends ImcStmt {

  public LinkedList<ImcStmt> stmts;

  public ImcSEQ() { stmts = new LinkedList<ImcStmt>(); }

  @Override
  public void dump(int indent) {
    Report.dump(indent, "SEQ");
    Iterator<ImcStmt> stmts = this.stmts.iterator();
    while (stmts.hasNext()) {
      ImcStmt stmt = stmts.next();
      stmt.dump(indent + 2);
    }
  }

  @Override
  public ImcSEQ linear() {
    ImcSEQ lin = new ImcSEQ();
    Iterator<ImcStmt> stmts = this.stmts.iterator();
    while (stmts.hasNext()) {
      ImcStmt stmt = stmts.next();
      ImcSEQ linStmt = stmt.linear();
      lin.stmts.addAll(linStmt.stmts);
    }
    return lin;
  }
}
