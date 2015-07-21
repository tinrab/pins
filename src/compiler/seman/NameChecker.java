package compiler.seman;

import compiler.Report;
import compiler.abstr.Visitor;
import compiler.abstr.tree.AbsArrType;
import compiler.abstr.tree.AbsAtomConst;
import compiler.abstr.tree.AbsAtomType;
import compiler.abstr.tree.AbsBinExpr;
import compiler.abstr.tree.AbsDef;
import compiler.abstr.tree.AbsDefs;
import compiler.abstr.tree.AbsExprs;
import compiler.abstr.tree.AbsFor;
import compiler.abstr.tree.AbsFunCall;
import compiler.abstr.tree.AbsFunDef;
import compiler.abstr.tree.AbsIfThen;
import compiler.abstr.tree.AbsIfThenElse;
import compiler.abstr.tree.AbsPar;
import compiler.abstr.tree.AbsTypeDef;
import compiler.abstr.tree.AbsTypeName;
import compiler.abstr.tree.AbsUnExpr;
import compiler.abstr.tree.AbsVarDef;
import compiler.abstr.tree.AbsVarName;
import compiler.abstr.tree.AbsWhere;
import compiler.abstr.tree.AbsWhile;

public class NameChecker implements Visitor {

  @Override
  public void visit(AbsArrType acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsAtomConst acceptor) {}

  @Override
  public void visit(AbsAtomType acceptor) {}

  @Override
  public void visit(AbsBinExpr acceptor) {
    acceptor.expr1.accept(this);
    acceptor.expr2.accept(this);
  }

  @Override
  public void visit(AbsDefs acceptor) {
    for (int i = 0; i < acceptor.numDefs(); i++) {
      AbsDef def = acceptor.def(i);
      String name = null;

      try {
        if (def instanceof AbsFunDef) {
          AbsFunDef funDef = (AbsFunDef)def;
          name = funDef.name;
          SymbTable.ins(name, funDef);
        } else if (def instanceof AbsVarDef) {
          AbsVarDef varDef = (AbsVarDef)def;
          name = varDef.name;
          SymbTable.ins(name, varDef);
        } else {
          AbsTypeDef typeDef = (AbsTypeDef)def;
          name = typeDef.name;
          SymbTable.ins(name, typeDef);
        }
      } catch (SemIllegalInsertException e) {
        AbsDef other = SymbTable.fnd(name);
        String type = "variable";

        if (other instanceof AbsFunDef) {
          type = "function";
        } else if (other instanceof AbsTypeDef) {
          type = "type";
        } else if (other instanceof AbsPar) {
          type = "parameter";
        }

        Report.error(def.position,
                     String.format("'%s' is already defined at [%s] as a %s",
                                   name, other.position, type));
      }
    }

    for (int i = 0; i < acceptor.numDefs(); i++) {
      acceptor.def(i).accept(this);
    }
  }

  @Override
  public void visit(AbsExprs acceptor) {
    for (int i = 0; i < acceptor.numExprs(); i++) {
      acceptor.expr(i).accept(this);
    }
  }

  @Override
  public void visit(AbsFor acceptor) {
    acceptor.count.accept(this);
    acceptor.lo.accept(this);
    acceptor.hi.accept(this);
    acceptor.step.accept(this);
    acceptor.body.accept(this);
  }

  @Override
  public void visit(AbsFunCall acceptor) {
    AbsDef def = SymbTable.fnd(acceptor.name);

    if (def == null || !(def instanceof AbsFunDef)) {
      Report.error(
          acceptor.position,
          String.format("Function '%s' is not defined", acceptor.name));
    } else {
      SymbDesc.setNameDef(acceptor, def);

      for (int i = 0; i < acceptor.numArgs(); i++) {
        acceptor.arg(i).accept(this);
      }
    }
  }

  @Override
  public void visit(AbsFunDef acceptor) {
    SymbTable.newScope();

    for (int i = 0; i < acceptor.numPars(); i++) {
      acceptor.par(i).accept(this);
    }

    acceptor.type.accept(this);
    acceptor.expr.accept(this);

    SymbTable.oldScope();
  }

  @Override
  public void visit(AbsIfThen acceptor) {
    acceptor.cond.accept(this);
    acceptor.thenBody.accept(this);
  }

  @Override
  public void visit(AbsIfThenElse acceptor) {
    acceptor.cond.accept(this);
    acceptor.thenBody.accept(this);
    acceptor.elseBody.accept(this);
  }

  @Override
  public void visit(AbsPar acceptor) {
    try {
      SymbTable.ins(acceptor.name, acceptor);
      acceptor.type.accept(this);
    } catch (SemIllegalInsertException e) {
      Report.error(acceptor.position,
                   String.format("Duplicate parameter '%s'", acceptor.name));
    }
  }

  @Override
  public void visit(AbsTypeDef acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsTypeName acceptor) {
    AbsDef def = SymbTable.fnd(acceptor.name);

    if (def == null || !(def instanceof AbsTypeDef)) {
      Report.error(acceptor.position,
                   String.format("Type '%s' is not defined", acceptor.name));
    } else {
      SymbDesc.setNameDef(acceptor, def);
    }
  }

  @Override
  public void visit(AbsUnExpr acceptor) {
    acceptor.expr.accept(this);
  }

  @Override
  public void visit(AbsVarDef acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsVarName acceptor) {
    AbsDef def = SymbTable.fnd(acceptor.name);

    if (def == null || !(def instanceof AbsVarDef || def instanceof AbsPar)) {
      Report.error(
          acceptor.position,
          String.format("Variable '%s' is not defined", acceptor.name));
    } else {
      SymbDesc.setNameDef(acceptor, def);
    }
  }

  @Override
  public void visit(AbsWhere acceptor) {
    SymbTable.newScope();
    acceptor.defs.accept(this);
    acceptor.expr.accept(this);
    SymbTable.oldScope();
  }

  @Override
  public void visit(AbsWhile acceptor) {
    acceptor.cond.accept(this);
    acceptor.body.accept(this);
  }
}
