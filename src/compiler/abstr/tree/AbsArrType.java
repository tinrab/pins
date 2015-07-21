package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

public class AbsArrType extends AbsType {

  public final int length;

  public final AbsType type;

  public AbsArrType(Position pos, int length, AbsType type) {
    super(pos);
    this.length = length;
    this.type = type;
  }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
