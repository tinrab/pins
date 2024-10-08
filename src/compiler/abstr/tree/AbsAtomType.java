package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsAtomType extends AbsType {

  public static final int LOG = 0;
  public static final int INT = 1;
  public static final int STR = 2;

  public final int type;

  public AbsAtomType(Position pos, int type) {
    super(pos);
    this.type = type;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
