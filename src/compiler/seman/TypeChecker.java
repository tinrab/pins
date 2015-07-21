package compiler.seman;

import compiler.Report;
import compiler.abstr.Visitor;
import compiler.abstr.tree.*;
import compiler.seman.type.*;
import java.util.HashSet;
import java.util.Vector;

public class TypeChecker implements Visitor {

  @Override
  public void visit(AbsArrType acceptor) {
    acceptor.type.accept(this);

    SymbDesc.setType(acceptor, new SemArrType(acceptor.length,
                                              SymbDesc.getType(acceptor.type)));
  }

  @Override
  public void visit(AbsAtomConst acceptor) {
    SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
  }

  @Override
  public void visit(AbsAtomType acceptor) {
    SymbDesc.setType(acceptor, new SemAtomType(acceptor.type));
  }

  @Override
  public void visit(AbsBinExpr acceptor) {
    if (acceptor.oper == AbsBinExpr.ASSIGN) {
      acceptor.expr1.accept(this);
      SemType type1 = SymbDesc.getType(acceptor.expr1);

      if (!(type1 instanceof SemAtomType)) {
        Report.error(acceptor.expr1.position,
                     "Type mismatch: left-hand side of an assignment must be " +
                     "either LOGICAL, INTEGER or STRING");
      }

      acceptor.expr2.accept(this);
      SemType type2 = SymbDesc.getType(acceptor.expr2);

      if (!type2.sameStructureAs(type1)) {
        Report.error(
            acceptor.expr2.position,
            String.format("Type mismatch: cannot convert from %s to %s", type2,
                          type1));
      }

      SymbDesc.setType(acceptor, type1);
    } else if (acceptor.oper == AbsBinExpr.ARR) {
      acceptor.expr1.accept(this);
      SemType type1 = SymbDesc.getType(acceptor.expr1);

      if (!(type1 instanceof SemArrType)) {
        Report.error(acceptor.expr1.position,
                     String.format("The type of the expression must be an " +
                                   "array type but it resolved to %s",
                                   type1));
      }

      acceptor.expr2.accept(this);
      SemType type2 = SymbDesc.getType(acceptor.expr2);

      if (!type2.sameStructureAs(SemAtomType.INT_TYPE)) {
        Report.error(
            acceptor.expr2.position,
            String.format("Type mismatch: cannot convert from %s to INTEGER",
                          type2));
      }

      SemArrType arrType = (SemArrType)type1;
      SymbDesc.setType(acceptor, arrType.type);
    } else {
      acceptor.expr1.accept(this);
      acceptor.expr2.accept(this);

      SemType type1 = SymbDesc.getType(acceptor.expr1);
      SemType type2 = SymbDesc.getType(acceptor.expr2);
      boolean same = type1.sameStructureAs(type2);

      String op = AbsBinExpr.operatorToString(acceptor.oper);

      if (acceptor.oper >= 8 && acceptor.oper <= 12) {
        // {+, -, *, /, %}
        if (!same || !type1.sameStructureAs(SemAtomType.INT_TYPE)) {
          Report.error(acceptor.position,
                       String.format("The operator %s is undefined for the " +
                                     "argument type(s) %s, %s",
                                     op, type1, type2));
        }

        SymbDesc.setType(acceptor, SemAtomType.INT_TYPE);
      } else if (acceptor.oper >= 2 && acceptor.oper <= 7) {
        // {==, !=, <=, >=, <, >}
        if (!same || !(type1.sameStructureAs(SemAtomType.INT_TYPE) ||
                       type1.sameStructureAs(SemAtomType.LOG_TYPE))) {
          Report.error(acceptor.position,
                       String.format("The operator %s is undefined for the " +
                                     "argument type(s) %s, %s",
                                     op, type1, type2));
        }

        SymbDesc.setType(acceptor, SemAtomType.LOG_TYPE);
      } else {
        // {&, |}
        if (!same || !type1.sameStructureAs(SemAtomType.LOG_TYPE)) {
          Report.error(acceptor.position,
                       String.format("The operator %s is undefined for the " +
                                     "argument type(s) %s, %s",
                                     op, type1, type2));
        }

        SymbDesc.setType(acceptor, SemAtomType.LOG_TYPE);
      }
    }
  }

  @Override
  public void visit(AbsDefs acceptor) {
    for (int i = 0; i < acceptor.numDefs(); i++) {
      AbsDef def = acceptor.def(i);

      if (def instanceof AbsTypeDef) {
        AbsTypeDef td = (AbsTypeDef)def;

        SymbDesc.setType(td, new SemTypeName(td.name));
      } else if (def instanceof AbsFunDef) {
        AbsFunDef fd = (AbsFunDef)def;

        Vector<SemType> parTypes = new Vector<SemType>();

        for (int j = 0; j < fd.numPars(); j++) {
          AbsPar par = fd.par(j);

          par.accept(this);

          if (!(SymbDesc.getType(par) instanceof SemAtomType)) {
            Report.error(par.position, "Parameters can only be of atom type");
          }

          parTypes.add(SymbDesc.getType(par));
        }

        fd.type.accept(this);

        if (!(SymbDesc.getType(fd.type) instanceof SemAtomType)) {
          Report.error(fd.position, "Function can only be of atom type");
        }

        SymbDesc.setType(fd,
                         new SemFunType(parTypes, SymbDesc.getType(fd.type)));
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

    SymbDesc.setType(acceptor,
                     SymbDesc.getType(acceptor.expr(acceptor.numExprs() - 1)));
  }

  @Override
  public void visit(AbsFor acceptor) {
    acceptor.count.accept(this);

    if (!SymbDesc.getType(acceptor.count)
             .sameStructureAs(SemAtomType.INT_TYPE)) {
      Report.error(acceptor.count.position,
                   "Type mismatch: 'count' must be of type INTEGER");
    }

    acceptor.lo.accept(this);

    if (!SymbDesc.getType(acceptor.lo).sameStructureAs(SemAtomType.INT_TYPE)) {
      Report.error(acceptor.lo.position,
                   "Type mismatch: 'low' must be of type INTEGER");
    }

    acceptor.hi.accept(this);

    if (!SymbDesc.getType(acceptor.hi).sameStructureAs(SemAtomType.INT_TYPE)) {
      Report.error(acceptor.hi.position,
                   "Type mismatch: 'high' must be of type INTEGER");
    }

    acceptor.step.accept(this);

    if (!SymbDesc.getType(acceptor.step)
             .sameStructureAs(SemAtomType.INT_TYPE)) {
      Report.error(acceptor.step.position,
                   "Type mismatch: 'step' must be of type INTEGER");
    }

    acceptor.body.accept(this);

    SymbDesc.setType(acceptor, SemAtomType.VOID_TYPE);
  }

  @Override
  public void visit(AbsFunCall acceptor) {
    SemFunType funType =
        (SemFunType)SymbDesc.getType(SymbDesc.getNameDef(acceptor));

    if (acceptor.numArgs() != funType.getNumPars()) {
      Report.error(acceptor.position,
                   String.format("Function '%s' does not take %d argument%s",
                                 acceptor.name, acceptor.numArgs(),
                                 acceptor.numArgs() > 1 ? "s" : ""));
    }

    for (int i = 0; i < acceptor.numArgs(); i++) {
      AbsExpr arg = acceptor.arg(i);
      SemType par = funType.getParType(i);

      arg.accept(this);

      if (!SymbDesc.getType(arg).sameStructureAs(par)) {
        StringBuffer sb = new StringBuffer();

        for (int j = 0; j < acceptor.numArgs(); j++) {
          sb.append(SymbDesc.getType(acceptor.arg(j)));
        }

        Report.error(
            arg.position,
            String.format(
                "Function '%s' is not applicable for the arguments (%s)",
                acceptor.name, sb.toString()));
      }
    }

    SymbDesc.setType(acceptor, funType.resultType);
  }

  @Override
  public void visit(AbsFunDef acceptor) {

    SemType type1 = SymbDesc.getType(acceptor.type);

    acceptor.expr.accept(this);
    SemType type2 = SymbDesc.getType(acceptor.expr);

    if (!type2.sameStructureAs(type1)) {
      Report.error(
          acceptor.expr.position,
          String.format(
              "Expression of function '%s' must be of type %s as defined",
              acceptor.name, type1));
    }
  }

  @Override
  public void visit(AbsIfThen acceptor) {
    acceptor.cond.accept(this);

    if (!SymbDesc.getType(acceptor.cond)
             .sameStructureAs(SemAtomType.LOG_TYPE)) {
      Report.error(acceptor.position,
                   "Type mismatch: condition must be of type LOGICAL");
    }

    acceptor.thenBody.accept(this);
    SymbDesc.setType(acceptor, SemAtomType.VOID_TYPE);
  }

  @Override
  public void visit(AbsIfThenElse acceptor) {
    acceptor.cond.accept(this);

    if (!SymbDesc.getType(acceptor.cond)
             .sameStructureAs(SemAtomType.LOG_TYPE)) {
      Report.error(acceptor.position,
                   "Type mismatch: condition must be of type LOGICAL");
    }

    acceptor.thenBody.accept(this);
    acceptor.elseBody.accept(this);
    SymbDesc.setType(acceptor, SemAtomType.VOID_TYPE);
  }

  @Override
  public void visit(AbsPar acceptor) {
    acceptor.type.accept(this);

    SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
  }

  @Override
  public void visit(AbsTypeDef acceptor) {
    acceptor.type.accept(this);

    SemTypeName tn = (SemTypeName)SymbDesc.getType(acceptor);

    if (SymbDesc.getType(acceptor.type) != null) {
      tn.setType(SymbDesc.getType(acceptor.type));

      SymbDesc.setType(acceptor, tn);
    } else {
      AbsTypeDef def = (AbsTypeDef)SymbDesc.getNameDef(acceptor.type);
      HashSet<AbsTypeDef> path = new HashSet<AbsTypeDef>();

      while (SymbDesc.getType(def.type) == null) {
        AbsTypeDef d = (AbsTypeDef)SymbDesc.getNameDef(def.type);

        if (d == null) {
          def.type.accept(this);
          break;
        }

        if (path.contains(d)) {
          StringBuilder sb = new StringBuilder();

          for (AbsTypeDef td : path) {
            AbsTypeName atn = (AbsTypeName)td.type;

            sb.append(
                String.format("%s:%s [%s], ", td.name, atn.name, td.position));
          }

          sb.delete(sb.length() - 2, sb.length());

          Report.error(acceptor.position,
                       "Recursive relationship between the following type " +
                       "definitions detected: " +
                           sb.toString());
        }

        path.add(d);
        def = d;
      }

      tn.setType(SymbDesc.getType(def.type));
    }
  }

  @Override
  public void visit(AbsUnExpr acceptor) {
    acceptor.expr.accept(this);
    SemType type = SymbDesc.getType(acceptor.expr);

    if (acceptor.oper == AbsUnExpr.NOT) {
      if (!type.sameStructureAs(SemAtomType.LOG_TYPE)) {
        Report.error(
            acceptor.position,
            String.format(
                "The operator ! is undefined for the argument type %s", type));
      }

      SymbDesc.setType(acceptor, SemAtomType.LOG_TYPE);
    } else {
      if (!type.sameStructureAs(SemAtomType.INT_TYPE)) {
        Report.error(
            acceptor.position,
            String.format(
                "The operator %s is undefined for the argument type %s",
                acceptor.oper == AbsUnExpr.ADD ? "+" : "-", type));
      }

      SymbDesc.setType(acceptor, SemAtomType.INT_TYPE);
    }
  }

  @Override
  public void visit(AbsVarDef acceptor) {
    acceptor.type.accept(this);

    SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.type));
  }

  @Override
  public void visit(AbsWhere acceptor) {
    acceptor.defs.accept(this);
    acceptor.expr.accept(this);

    SymbDesc.setType(acceptor, SymbDesc.getType(acceptor.expr));
  }

  @Override
  public void visit(AbsWhile acceptor) {
    acceptor.cond.accept(this);

    if (!SymbDesc.getType(acceptor.cond)
             .sameStructureAs(SemAtomType.LOG_TYPE)) {
      Report.error(acceptor.cond.position,
                   "Type mismatch: condition must be of type LOGICAL");
    }

    acceptor.body.accept(this);

    SymbDesc.setType(acceptor, SemAtomType.VOID_TYPE);
  }

  @Override
  public void visit(AbsTypeName acceptor) {
    HashSet<AbsTypeDef> path = new HashSet<AbsTypeDef>();
    AbsTypeDef def = (AbsTypeDef)SymbDesc.getNameDef(acceptor);

    while (def.type instanceof AbsTypeName) {
      AbsTypeDef d = (AbsTypeDef)SymbDesc.getNameDef(def.type);

      if (path.contains(d)) {
        break;
      }

      path.add(d);
      def = d;
    }

    def.type.accept(this);

    SymbDesc.setType(acceptor, SymbDesc.getType(def.type));
  }

  @Override
  public void visit(AbsVarName acceptor) {
    AbsDef def = (AbsDef)SymbDesc.getNameDef(acceptor);
    AbsType type = null;

    if (def instanceof AbsPar) {
      type = ((AbsPar)def).type;
    } else {
      type = ((AbsVarDef)def).type;
    }

    type.accept(this);

    SymbDesc.setType(acceptor, SymbDesc.getType(type));
  }
}
