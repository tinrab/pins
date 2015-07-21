package compiler;

import compiler.abstr.Abstr;
import compiler.abstr.tree.AbsTree;
import compiler.frames.Frames;
import compiler.frames.FrmDesc;
import compiler.frames.FrmEvaluator;
import compiler.frames.FrmLabel;
import compiler.imcode.ImCode;
import compiler.imcode.ImcChunk;
import compiler.imcode.ImcCodeChunk;
import compiler.imcode.ImcCodeGen;
import compiler.interpreter.Interpreter;
import compiler.lexan.LexAn;
import compiler.lexan.Token;
import compiler.seman.NameChecker;
import compiler.seman.SemAn;
import compiler.seman.TypeChecker;
import compiler.synan.SynAn;

public class Main {

  private static String sourceFileName;

  private static String allPhases = "(lexan|synan|ast|seman|frames|imcode|run)";

  private static String execPhase = "run";

  private static String dumpPhases = "run";

  public static void main(String[] args) {
    System.out.printf("This is PREV compiler, v0.1:\n");

    for (int argc = 0; argc < args.length; argc++) {
      if (args[argc].startsWith("--")) {
        if (args[argc].startsWith("--phase=")) {
          String phase = args[argc].substring("--phase=".length());
          if (phase.matches(allPhases))
            execPhase = phase;
          else
            Report.warning("Unknown exec phase '" + phase + "' ignored.");
          continue;
        }
        if (args[argc].startsWith("--dump=")) {
          String phases = args[argc].substring("--dump=".length());
          if (phases.matches(allPhases + "(," + allPhases + ")*"))
            dumpPhases = phases;
          else
            Report.warning("Illegal dump phases '" + phases + "' ignored.");
          continue;
        }
        Report.warning("Unrecognized switch in the command line.");
      } else {
        if (sourceFileName == null)
          sourceFileName = args[argc];
        else
          Report.warning("Source file name '" + sourceFileName + "' ignored.");
      }
    }
    if (sourceFileName == null)
      Report.error("Source file name not specified.");

    if (dumpPhases != null)
      Report.openDumpFile(sourceFileName);

    ImcCodeGen imcodegen = null;

    while (true) {
      LexAn lexAn = new LexAn(sourceFileName, dumpPhases.contains("lexan"));
      if (execPhase.equals("lexan")) {
        while (lexAn.lexAn().token != Token.EOF) {
        }
        break;
      }
      SynAn synAn = new SynAn(lexAn, dumpPhases.contains("synan"));
      AbsTree source = synAn.parse();
      if (execPhase.equals("synan"))
        break;
      Abstr ast = new Abstr(dumpPhases.contains("ast"));
      ast.dump(source);
      if (execPhase.equals("ast"))
        break;
      SemAn semAn = new SemAn(dumpPhases.contains("seman"));
      source.accept(new NameChecker());
      source.accept(new TypeChecker());
      semAn.dump(source);
      if (execPhase.equals("seman"))
        break;
      Frames frames = new Frames(dumpPhases.contains("frames"));
      source.accept(new FrmEvaluator());
      frames.dump(source);
      if (execPhase.equals("frames"))
        break;
      ImCode imcode = new ImCode(dumpPhases.contains("imcode"));
      imcodegen = new ImcCodeGen();
      source.accept(imcodegen);
      imcode.dump(imcodegen.chunks);

      if (execPhase.equals("imcode"))
        break;

      for (ImcChunk chunk : imcodegen.chunks) {
        if (chunk instanceof ImcCodeChunk) {
          ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

          codeChunk.lincode = codeChunk.imcode.linear();
        }
      }

      System.out.println("Running...");

      Interpreter interpreter = new Interpreter(imcodegen.chunks);
      interpreter.run();
      System.exit(0);
    }

    if (dumpPhases != null)
      Report.closeDumpFile();

    System.out.println("Done");

    System.exit(0);
  }
}
