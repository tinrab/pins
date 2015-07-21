package compiler.frames;

import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.SymbDesc;

public class FrmEvaluator implements Visitor {

  private FrmFrame currentFrame;
  private int currentScope = 1;

  @Override
  public void visit(AbsArrType acceptor) {}

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
  public void visit(AbsFunCall acceptor) {
    for (int i = 0; i < acceptor.numArgs(); i++) {
      AbsExpr arg = acceptor.arg(i);
      arg.accept(this);
    }
  }

  @Override
  public void visit(AbsFunDef acceptor) {
    FrmFrame frame = new FrmFrame(acceptor, currentScope);
    currentScope++;

    for (int i = 0; i < acceptor.numPars(); i++) {
      AbsPar p = acceptor.par(i);
      FrmParAccess fpa = new FrmParAccess(p, frame);
      FrmDesc.setAccess(p, fpa);

      p.accept(this);
    }

    frame.sizeArgs = SymbDesc.getType(acceptor.type).size();
    currentFrame = frame;

    acceptor.expr.accept(this);

    FrmDesc.setFrame(acceptor, frame);
    currentScope--;
  }

  @Override
  public void visit(AbsPar acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsTypeDef acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsTypeName acceptor) {
    SymbDesc.getNameDef(acceptor).accept(this);
  }

  @Override
  public void visit(AbsVarDef acceptor) {
    acceptor.type.accept(this);
  }

  @Override
  public void visit(AbsVarName acceptor) {}

  @Override
  public void visit(AbsDefs acceptor) {
    for (int i = 0; i < acceptor.numDefs(); i++) {
      AbsDef def = acceptor.def(i);

      if (def instanceof AbsVarDef) {
        FrmDesc.setAccess(def, new FrmVarAccess((AbsVarDef)def));
      }

      def.accept(this);
    }
  }

  @Override
  public void visit(AbsWhere acceptor) {
    for (int i = 0; i < acceptor.defs.numDefs(); i++) {
      AbsDef def = acceptor.defs.def(i);

      if (def instanceof AbsVarDef) {
        AbsVarDef varDef = (AbsVarDef)def;
        FrmLocAccess locAccess = new FrmLocAccess(varDef, currentFrame);

        currentFrame.locVars.add(locAccess);

        FrmDesc.setAccess(def, locAccess);
      }

      def.accept(this);
    }

    acceptor.expr.accept(this);
  }

  @Override
  public void visit(AbsWhile acceptor) {
    acceptor.cond.accept(this);
    acceptor.body.accept(this);
  }

  @Override
  public void visit(AbsUnExpr acceptor) {
    acceptor.expr.accept(this);
  }

  @Override
  public void visit(AbsIfThen accpetor) {
    accpetor.cond.accept(this);
    accpetor.thenBody.accept(this);
  }

  @Override
  public void visit(AbsIfThenElse accpetor) {
    accpetor.cond.accept(this);
    accpetor.thenBody.accept(this);
    accpetor.elseBody.accept(this);
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
}
