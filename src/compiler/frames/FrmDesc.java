package compiler.frames;

import compiler.abstr.tree.*;
import java.util.*;

public class FrmDesc {

  private static HashMap<AbsFunDef, FrmFrame> frames =
      new HashMap<AbsFunDef, FrmFrame>();

  public static void setFrame(AbsFunDef fun, FrmFrame frame) {
    FrmDesc.frames.put(fun, frame);
  }

  public static FrmFrame getFrame(AbsTree fun) {
    return FrmDesc.frames.get(fun);
  }

  private static HashMap<AbsDef, FrmAccess> acceses =
      new HashMap<AbsDef, FrmAccess>();

  public static void setAccess(AbsDef var, FrmAccess access) {
    FrmDesc.acceses.put(var, access);
  }

  public static FrmAccess getAccess(AbsDef var) {
    return FrmDesc.acceses.get(var);
  }
}
