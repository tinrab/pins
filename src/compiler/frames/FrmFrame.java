package compiler.frames;

import compiler.abstr.tree.*;
import java.util.*;

public class FrmFrame {

  public AbsFunDef fun;

  public int level;

  public FrmLabel label;

  public int numPars;

  public int sizePars;

  LinkedList<FrmLocAccess> locVars;

  public int sizeLocs;

  public int sizeFPRA;

  public int sizeTmps;

  public int sizeRegs;

  public int sizeArgs;

  public FrmTemp FP;

  public FrmTemp RV;

  public FrmFrame(AbsFunDef fun, int level) {
    this.fun = fun;
    this.level = level;
    this.label =
        (level == 1 ? FrmLabel.newLabel(fun.name) : FrmLabel.newLabel());
    this.numPars = 0;
    this.sizePars = 0;
    this.locVars = new LinkedList<FrmLocAccess>();
    this.sizeLocs = 0;
    this.sizeFPRA = 8;
    this.sizeTmps = 0;
    this.sizeRegs = 0;
    this.sizeArgs = 0;
    FP = new FrmTemp();
    RV = new FrmTemp();
  }

  public int size() {
    return sizeLocs + sizeFPRA + sizeTmps + sizeRegs + sizeArgs;
  }

  @Override
  public String toString() {
    return ("FRAME(" + fun.name + ": "
            + "level=" + level + ","
            + "label=" + label.name() + ","
            + "sizeLocs=" + sizeLocs + ","
            + "sizeArgs=" + sizeArgs + ","
            + "size=" + size() + ","
            + "FP=" + FP.name() + ","
            + "RV=" + RV.name() + ")");
  }
}
