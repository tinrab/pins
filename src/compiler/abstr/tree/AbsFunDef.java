package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;
import java.util.*;

public class AbsFunDef extends AbsDef {

  public final String name;

  private final AbsPar pars[];

  public final AbsType type;

  public final AbsExpr expr;

  public AbsFunDef(Position pos, String name, Vector<AbsPar> pars, AbsType type,
                   AbsExpr expr) {
    super(pos);
    this.name = name;
    this.pars = new AbsPar[pars.size()];
    for (int par = 0; par < pars.size(); par++)
      this.pars[par] = pars.elementAt(par);
    this.type = type;
    this.expr = expr;
  }

  public AbsPar par(int index) { return pars[index]; }

  public int numPars() { return pars.length; }

  @Override
  public void accept(Visitor visitor) {
    visitor.visit(this);
  }
}
