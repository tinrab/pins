package compiler.seman;

import compiler.*;
import compiler.abstr.tree.*;
import java.util.*;

public class SymbTable {

  private static HashMap<String, LinkedList<AbsDef>> mapping =
      new HashMap<String, LinkedList<AbsDef>>();

  private static int scope = 0;

  public static void newScope() { scope++; }

  public static void oldScope() {
    LinkedList<String> allNames = new LinkedList<String>();
    allNames.addAll(mapping.keySet());
    for (String name : allNames) {
      try {
        SymbTable.del(name);
      } catch (SemIllegalDeleteException __) {
      }
    }
    scope--;
  }

  public static void ins(String name, AbsDef newDef)
      throws SemIllegalInsertException {
    LinkedList<AbsDef> allNameDefs = mapping.get(name);
    if (allNameDefs == null) {
      allNameDefs = new LinkedList<AbsDef>();
      allNameDefs.addFirst(newDef);
      SymbDesc.setScope(newDef, scope);
      mapping.put(name, allNameDefs);
      return;
    }
    if ((allNameDefs.size() == 0) ||
        (SymbDesc.getScope(allNameDefs.getFirst()) == null)) {
      Thread.dumpStack();
      Report.error("Internal error.");
      return;
    }
    if (SymbDesc.getScope(allNameDefs.getFirst()) == scope)
      throw new SemIllegalInsertException();
    allNameDefs.addFirst(newDef);
    SymbDesc.setScope(newDef, scope);
  }

  public static void del(String name) throws SemIllegalDeleteException {
    LinkedList<AbsDef> allNameDefs = mapping.get(name);
    if (allNameDefs == null)
      throw new SemIllegalDeleteException();
    if ((allNameDefs.size() == 0) ||
        (SymbDesc.getScope(allNameDefs.getFirst()) == null)) {
      Thread.dumpStack();
      Report.error("Internal error.");
      return;
    }
    if (SymbDesc.getScope(allNameDefs.getFirst()) < scope)
      throw new SemIllegalDeleteException();
    allNameDefs.removeFirst();
    if (allNameDefs.size() == 0)
      mapping.remove(name);
  }

  public static AbsDef fnd(String name) {
    LinkedList<AbsDef> allNameDefs = mapping.get(name);
    if (allNameDefs == null)
      return null;
    if (allNameDefs.size() == 0)
      return null;
    return allNameDefs.getFirst();
  }
}
