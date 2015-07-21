package compiler.interpreter;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import java.util.*;

public class Interpreter {

  private static long startTime = System.currentTimeMillis();

  private HashMap<Integer, Object> mem = new HashMap<Integer, Object>();
  private HashMap<FrmTemp, Object> temps = new HashMap<FrmTemp, Object>();
  private Scanner scanner;
  private int fp = 1024;
  private int sp = 1024;

  public void setMem(Integer address, Object value) { mem.put(address, value); }

  public Object loadMem(Integer address) {
    return mem.containsKey(address) ? mem.get(address) : 0;
  }

  public void storeTemp(FrmTemp temp, Object value) { temps.put(temp, value); }

  public Object loadTemp(FrmTemp temp) {
    return temps.containsKey(temp) ? temps.get(temp) : 0;
  }

  public HashMap<String, Integer> labels = new HashMap<String, Integer>();
  public HashMap<String, ImcCodeChunk> chunks =
      new HashMap<String, ImcCodeChunk>();

  public Interpreter(LinkedList<ImcChunk> chunks) {
    scanner = new Scanner(System.in);
    int heap = 0;

    for (ImcChunk chunk : chunks) {
      if (chunk instanceof ImcCodeChunk) {
        ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

        this.chunks.put(codeChunk.frame.label.name(), codeChunk);
      } else {
        ImcDataChunk dataChunk = (ImcDataChunk)chunk;

        labels.put(dataChunk.label.name(), heap);
        heap += dataChunk.size;
      }
    }
  }

  public void run() {
    if (!chunks.containsKey("_main")) {
      Report.error("Cannot find main function");
    }

    setMem(sp + 4, 0);
    executeFun("_main");

    scanner.close();
  }

  public Object executeFun(FrmLabel label) { return executeFun(label.name()); }

  public Object executeFun(String label) {
    ImcCodeChunk chunk = chunks.get(label);
    FrmFrame frame = chunk.frame;

    if (label.equals("_exit")) {
      int status = (Integer)loadMem(sp + 4);
      System.out.println("\nExiting with status " + status);
      System.exit(status);
    }

    if (label.equals("_put_int")) {
      System.out.print(loadMem(sp + 4));
      return 0;
    }

    if (label.equals("_put_str")) {
      System.out.print(loadMem(sp + 4));
      return "";
    }

    if (label.equals("_put_nl")) {
      System.out.println();
      return 0;
    }

    if (label.equals("_get_int")) {
      try {
        return scanner.nextInt();
      } catch (Exception e) {
        Report.error("Invalid integer value");
        return null;
      }
    }

    if (label.equals("_get_str")) {
      return scanner.nextLine();
    }

    if (label.equals("_time")) {
      return (int)(System.currentTimeMillis() - startTime);
    }

    HashMap<FrmTemp, Object> lastTemps = new HashMap<FrmTemp, Object>(temps);
    temps = new HashMap<FrmTemp, Object>();

    setMem(sp - frame.sizeLocs - 4, fp);
    fp = sp;
    sp = sp - frame.size();
    storeTemp(frame.FP, fp);

    LinkedList<ImcStmt> stmts = ((ImcSEQ)(chunk.lincode)).stmts;
    int pc = 0;

    while (pc < stmts.size()) {
      FrmLabel newLabel = executeStmt(stmts.get(pc));

      if (newLabel != null) {
        pc = stmts.indexOf(new ImcLABEL(newLabel));
      } else {
        pc++;
      }
    }

    sp = sp + frame.size();
    fp = (Integer)loadMem(sp - frame.sizeLocs - 4);
    Object returnValue = loadTemp(frame.RV);

    temps = lastTemps;

    return returnValue;
  }

  public FrmLabel executeStmt(ImcStmt stmt) {
    if (stmt instanceof ImcCJUMP) {
      ImcCJUMP cjump = (ImcCJUMP)stmt;
      Object cond = executeExpr(cjump.cond);

      if ((Integer)cond != 0) {
        return cjump.trueLabel;
      } else {
        return cjump.falseLabel;
      }
    }

    if (stmt instanceof ImcMOVE) {
      ImcMOVE move = (ImcMOVE)stmt;

      if (move.dst instanceof ImcTEMP) {
        ImcTEMP dst = (ImcTEMP)move.dst;
        Object src = executeExpr(move.src);

        storeTemp(dst.temp, src);
        return null;
      } else if (move.dst instanceof ImcMEM) {
        Integer dst = (Integer)executeExpr(((ImcMEM)move.dst).expr);
        Object src = executeExpr(move.src);

        setMem(dst, src);
        return null;
      }
    }

    if (stmt instanceof ImcEXP) {
      ImcEXP exp = (ImcEXP)stmt;

      executeExpr(exp.expr);
      return null;
    }

    if (stmt instanceof ImcJUMP) {
      ImcJUMP jump = (ImcJUMP)stmt;

      return jump.label;
    }

    return null;
  }

  public Object executeExpr(ImcExpr expr) {
    if (expr instanceof ImcBINOP) {
      ImcBINOP binExpr = (ImcBINOP)expr;

      int a = (Integer)executeExpr(binExpr.limc);
      int b = (Integer)executeExpr(binExpr.rimc);

      switch (binExpr.op) {
      case ImcBINOP.OR:
        return ((a != 0) || (b != 0) ? 1 : 0);
      case ImcBINOP.AND:
        return ((a != 0) && (b != 0) ? 1 : 0);
      case ImcBINOP.EQU:
        return (a == b ? 1 : 0);
      case ImcBINOP.NEQ:
        return (a != b ? 1 : 0);
      case ImcBINOP.LTH:
        return (a < b ? 1 : 0);
      case ImcBINOP.GTH:
        return (a > b ? 1 : 0);
      case ImcBINOP.LEQ:
        return (a <= b ? 1 : 0);
      case ImcBINOP.GEQ:
        return (a >= b ? 1 : 0);
      case ImcBINOP.ADD:
        return (a + b);
      case ImcBINOP.SUB:
        return (a - b);
      case ImcBINOP.MUL:
        return (a * b);
      case ImcBINOP.DIV:
        return (a / b);
      }

      return null;
    }

    if (expr instanceof ImcCALL) {
      ImcCALL call = (ImcCALL)expr;

      for (int a = 0, offset = 0; a < call.args.size(); a++) {
        Object val = executeExpr(call.args.get(a));
        setMem(sp + 4 * offset, val);
        offset++;
      }

      return executeFun(call.label);
    }

    if (expr instanceof ImcCONST) {
      ImcCONST c = (ImcCONST)expr;

      return c.value;
    }

    if (expr instanceof ImcNAME) {
      ImcNAME name = (ImcNAME)expr;

      return labels.get(name.label.name());
    }

    if (expr instanceof ImcTEMP) {
      ImcTEMP temp = (ImcTEMP)expr;

      return loadTemp(temp.temp);
    }

    if (expr instanceof ImcMEM) {
      ImcMEM mem = (ImcMEM)expr;

      return loadMem((Integer)executeExpr(mem.expr));
    }

    return null;
  }
}