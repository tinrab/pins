package compiler.imcode;

import compiler.abstr.Visitor;
import compiler.abstr.tree.AbsArrType;
import compiler.abstr.tree.AbsAtomConst;
import compiler.abstr.tree.AbsAtomType;
import compiler.abstr.tree.AbsBinExpr;
import compiler.abstr.tree.AbsDef;
import compiler.abstr.tree.AbsDefs;
import compiler.abstr.tree.AbsExpr;
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
import compiler.frames.FrmAccess;
import compiler.frames.FrmDesc;
import compiler.frames.FrmFrame;
import compiler.frames.FrmLabel;
import compiler.frames.FrmLocAccess;
import compiler.frames.FrmParAccess;
import compiler.frames.FrmTemp;
import compiler.frames.FrmVarAccess;
import compiler.seman.SymbDesc;
import compiler.seman.type.SemArrType;
import compiler.seman.type.SemAtomType;
import compiler.seman.type.SemType;
import java.util.LinkedList;
import java.util.Stack;

public class ImcCodeGen implements Visitor {

  public LinkedList<ImcChunk> chunks;

  private FrmFrame currentFrame;
  private ImcCode result;
  private Stack<Boolean> skipMem;

  public ImcCodeGen() {
    chunks = new LinkedList<ImcChunk>();
    skipMem = new Stack<Boolean>();
  }

  @Override
  public void visit(AbsArrType acceptor) {}

  @Override
  public void visit(AbsAtomType acceptor) {}

  @Override
  public void visit(AbsPar acceptor) {}

  @Override
  public void visit(AbsTypeDef acceptor) {}

  @Override
  public void visit(AbsTypeName acceptor) {}

  @Override
  public void visit(AbsDefs acceptor) {
    skipMem.push(false);

    for (int i = 0; i < acceptor.numDefs(); i++) {
      AbsDef def = acceptor.def(i);

      if (def instanceof AbsVarDef) {
        AbsVarDef varDef = (AbsVarDef)def;
        FrmVarAccess access = (FrmVarAccess)FrmDesc.getAccess(varDef);
        SemType type = SymbDesc.getType(varDef.type).actualType();

        chunks.add(new ImcDataChunk(access.label, type.size()));
      }

      def.accept(this);
    }
  }

  @Override
  public void visit(AbsVarDef acceptor) {}

  @Override
  public void visit(AbsFunDef acceptor) {
    FrmFrame frame = FrmDesc.getFrame(acceptor);
    FrmFrame tmp = currentFrame;
    currentFrame = frame;

    acceptor.expr.accept(this);

    ImcMOVE move = new ImcMOVE(new ImcTEMP(frame.RV), (ImcExpr)result);
    chunks.add(new ImcCodeChunk(frame, move));

    currentFrame = tmp;
  }

  @Override
  public void visit(AbsWhere acceptor) {
    for (int i = 0; i < acceptor.defs.numDefs(); i++) {
      AbsDef def = acceptor.defs.def(i);

      def.accept(this);
    }

    acceptor.expr.accept(this);
  }

  @Override
  public void visit(AbsAtomConst acceptor) {
    switch (acceptor.type) {
    case AbsAtomConst.INT:
      result = new ImcCONST(Integer.parseInt(acceptor.value));
      break;
    case AbsAtomConst.LOG:
      result = new ImcCONST(acceptor.value.equals("true") ? 1 : 0);
      break;
    case AbsAtomConst.STR:
      result = new ImcCONST(
          acceptor.value.substring(1, acceptor.value.length() - 1));
      break;
    }
  }

  @Override
  public void visit(AbsBinExpr acceptor) {
    switch (acceptor.oper) {
    case AbsBinExpr.ARR:
      skipMem.push(false);
      acceptor.expr1.accept(this);
      ImcExpr arr = (ImcExpr)result;
      SemArrType aType =
          (SemArrType)SymbDesc.getType(acceptor.expr1).actualType();
      skipMem.pop();

      skipMem.push(false);
      acceptor.expr2.accept(this);
      ImcExpr index = (ImcExpr)result;
      skipMem.pop();

      ImcBINOP offset =
          new ImcBINOP(ImcBINOP.MUL, index, new ImcCONST(aType.type.size()));

      result = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, arr, offset));
      break;
    case AbsBinExpr.ASSIGN:
      skipMem.push(true);
      acceptor.expr1.accept(this);
      ImcExpr dst = (ImcExpr)result;
      skipMem.pop();

      // fix for arrays
      if (dst instanceof ImcMEM) {
        dst = ((ImcMEM)dst).expr;
      }

      acceptor.expr2.accept(this);
      ImcExpr src = (ImcExpr)result;

      ImcTEMP t1 = new ImcTEMP(new FrmTemp());
      ImcTEMP t2 = new ImcTEMP(new FrmTemp());
      ImcSEQ seq = new ImcSEQ();

      seq.stmts.add(new ImcMOVE(t1, dst));
      seq.stmts.add(new ImcMOVE(t2, src));
      seq.stmts.add(new ImcMOVE(new ImcMEM(t1), t2));

      result = new ImcESEQ(seq, t2);
      break;
    default:
      acceptor.expr1.accept(this);
      ImcExpr limc = (ImcExpr)result;

      acceptor.expr2.accept(this);
      ImcExpr rimc = (ImcExpr)result;

      int oper = -1;

      switch (acceptor.oper) {
      case AbsBinExpr.ADD:
        oper = ImcBINOP.ADD;
        break;
      case AbsBinExpr.SUB:
        oper = ImcBINOP.SUB;
        break;
      case AbsBinExpr.MUL:
        oper = ImcBINOP.MUL;
        break;
      case AbsBinExpr.DIV:
        oper = ImcBINOP.DIV;
        break;
      case AbsBinExpr.EQU:
        oper = ImcBINOP.EQU;
        break;
      case AbsBinExpr.NEQ:
        oper = ImcBINOP.NEQ;
        break;
      case AbsBinExpr.LTH:
        oper = ImcBINOP.LTH;
        break;
      case AbsBinExpr.GTH:
        oper = ImcBINOP.GTH;
        break;
      case AbsBinExpr.LEQ:
        oper = ImcBINOP.LEQ;
        break;
      case AbsBinExpr.GEQ:
        oper = ImcBINOP.GEQ;
        break;
      case AbsBinExpr.AND:
        oper = ImcBINOP.AND;
        break;
      case AbsBinExpr.IOR:
        oper = ImcBINOP.OR;
        break;
      }

      if (oper == -1) {
        // mod
        result =
            new ImcBINOP(ImcBINOP.SUB, limc,
                         new ImcBINOP(ImcBINOP.MUL, rimc,
                                      new ImcBINOP(ImcBINOP.DIV, limc, rimc)));
      } else {
        result = new ImcBINOP(oper, limc, rimc);
      }

      break;
    }
  }

  @Override
  public void visit(AbsExprs acceptor) {
    ImcSEQ seq = new ImcSEQ();

    for (int i = 0; i < acceptor.numExprs() - 1; i++) {
      AbsExpr expr = acceptor.expr(i);

      expr.accept(this);
      seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                              : (ImcStmt)result);
    }

    acceptor.expr(acceptor.numExprs() - 1).accept(this);
    ImcExpr expr = (ImcExpr)result;

    result = new ImcESEQ(seq, expr);
  }

  @Override
  public void visit(AbsFor acceptor) {
    ImcSEQ seq = new ImcSEQ();

    acceptor.count.accept(this);
    ImcExpr name = (ImcExpr)result;

    acceptor.lo.accept(this);
    ImcExpr lo = (ImcExpr)result;

    acceptor.hi.accept(this);
    ImcExpr hi = (ImcExpr)result;

    ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());

    seq.stmts.add(new ImcMOVE(name, lo));
    seq.stmts.add(sl);
    seq.stmts.add(
        new ImcCJUMP(new ImcBINOP(ImcBINOP.LEQ, name, hi), tl.label, fl.label));
    seq.stmts.add(tl);
    acceptor.body.accept(this);
    seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                            : (ImcStmt)result);

    acceptor.step.accept(this);
    ImcExpr step = (ImcExpr)result;
    seq.stmts.add(new ImcMOVE(name, new ImcBINOP(ImcBINOP.ADD, name, step)));

    seq.stmts.add(new ImcJUMP(sl.label));
    seq.stmts.add(fl);

    result = seq;
  }

  @Override
  public void visit(AbsFunCall acceptor) {
    FrmFrame frame = FrmDesc.getFrame(SymbDesc.getNameDef(acceptor));
    ImcCALL call = new ImcCALL(frame.label);
    ImcExpr fp = new ImcTEMP(currentFrame.FP);

    for (int i = 0; i < currentFrame.level; i++) {
      fp = new ImcMEM(fp);
    }

    call.args.add(fp);

    for (int i = 0; i < acceptor.numArgs(); i++) {
      AbsExpr arg = acceptor.arg(i);
      arg.accept(this);

      call.args.add((ImcExpr)result);
    }

    result = call;
  }

  @Override
  public void visit(AbsIfThen accpetor) {
    ImcSEQ seq = new ImcSEQ();

    accpetor.cond.accept(this);
    ImcExpr cond = (ImcExpr)result;

    ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());

    seq.stmts.add(new ImcCJUMP(cond, tl.label, fl.label));
    seq.stmts.add(tl);
    accpetor.thenBody.accept(this);
    seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                            : (ImcStmt)result);
    seq.stmts.add(fl);

    result = seq;
  }

  @Override
  public void visit(AbsIfThenElse accpetor) {
    ImcSEQ seq = new ImcSEQ();

    accpetor.cond.accept(this);
    ImcExpr cond = (ImcExpr)result;

    ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL el = new ImcLABEL(FrmLabel.newLabel());

    seq.stmts.add(new ImcCJUMP(cond, tl.label, fl.label));
    seq.stmts.add(tl);
    accpetor.thenBody.accept(this);
    seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                            : (ImcStmt)result);
    seq.stmts.add(new ImcJUMP(el.label));
    seq.stmts.add(fl);
    accpetor.elseBody.accept(this);
    seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                            : (ImcStmt)result);
    seq.stmts.add(el);

    result = seq;
  }

  @Override
  public void visit(AbsUnExpr acceptor) {
    acceptor.expr.accept(this);

    switch (acceptor.oper) {
    case AbsUnExpr.ADD:
      result = new ImcBINOP(ImcBINOP.ADD, new ImcCONST(0), (ImcExpr)result);
      break;
    case AbsUnExpr.SUB:
      result = new ImcBINOP(ImcBINOP.SUB, new ImcCONST(0), (ImcExpr)result);
      break;
    case AbsUnExpr.NOT:
      result = new ImcBINOP(ImcBINOP.EQU, new ImcCONST(0), (ImcExpr)result);
      break;
    }
  }

  @Override
  public void visit(AbsVarName acceptor) {
    AbsDef def = SymbDesc.getNameDef(acceptor);
    FrmFrame frame = FrmDesc.getFrame(def);
    FrmAccess access = FrmDesc.getAccess(def);

    if (access instanceof FrmVarAccess) {
      if (skipMem.peek()) {
        result = new ImcNAME(((FrmVarAccess)access).label);
      } else {
        result = new ImcMEM(new ImcNAME(((FrmVarAccess)access).label));
      }
    } else if (access instanceof FrmParAccess) {
      FrmParAccess parAccess = (FrmParAccess)access;
      ImcExpr t = new ImcTEMP(currentFrame.FP);

      for (int i = 0; i < currentFrame.level - parAccess.frame.level; i++) {
        t = new ImcMEM(t);
      }

      if (skipMem.peek()) {
        result = new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(parAccess.offset));
      } else {
        result = new ImcMEM(
            new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(parAccess.offset)));
      }
    } else if (access instanceof FrmLocAccess) {
      FrmLocAccess locAccess = (FrmLocAccess)access;

      ImcExpr t = new ImcTEMP(currentFrame.FP);

      for (int i = 0; i < currentFrame.level - locAccess.frame.level; i++) {
        t = new ImcMEM(t);
      }

      if (skipMem.peek()) {
        result = new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(locAccess.offset));
      } else {
        result = new ImcMEM(
            new ImcBINOP(ImcBINOP.ADD, t, new ImcCONST(locAccess.offset)));
      }
    }

    if (def instanceof AbsFunDef) {
      if (skipMem.peek()) {
        result = new ImcTEMP(frame.RV);
      } else {
        result = new ImcMEM(new ImcTEMP(frame.RV));
      }
    }
  }

  @Override
  public void visit(AbsWhile acceptor) {
    ImcSEQ seq = new ImcSEQ();

    acceptor.cond.accept(this);
    ImcExpr cond = (ImcExpr)result;

    ImcLABEL tl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL fl = new ImcLABEL(FrmLabel.newLabel());
    ImcLABEL sl = new ImcLABEL(FrmLabel.newLabel());

    seq.stmts.add(sl);
    seq.stmts.add(new ImcCJUMP(cond, tl.label, fl.label));
    seq.stmts.add(tl);

    acceptor.body.accept(this);
    seq.stmts.add(result instanceof ImcExpr ? new ImcEXP((ImcExpr)result)
                                            : (ImcStmt)result);
    seq.stmts.add(new ImcJUMP(sl.label));

    seq.stmts.add(fl);

    result = seq;
  }
}
