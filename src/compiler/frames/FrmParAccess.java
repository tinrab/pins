package compiler.frames;

import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.seman.type.*;

public class FrmParAccess extends FrmAccess {

  public AbsPar par;

  public FrmFrame frame;

  public int offset;

  public FrmParAccess(AbsPar par, FrmFrame frame) {
    this.par = par;
    this.frame = frame;

    SemType type = SymbDesc.getType(this.par).actualType();
    this.offset = 4 + frame.sizePars;
    frame.sizePars = frame.sizePars + type.size();
    frame.numPars++;
  }

  @Override
  public String toString() {
    return "PAR(" + par.name + ": offset=" + offset + ")";
  }
}
