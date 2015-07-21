package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;
import java.util.*;

public class AbsDefs extends AbsTree {

  private AbsDef defs[];

  public AbsDefs(Position pos, Vector<AbsDef> defs) {
    super(pos);
    this.defs = new AbsDef[defs.size()];
    for (int def = 0; def < defs.size(); def++)
      this.defs[def] = defs.elementAt(def);
  }

  public AbsDef def(int index) { return defs[index]; }

  public int numDefs() { return defs.length; }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
