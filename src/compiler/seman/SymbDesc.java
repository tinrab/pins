package compiler.seman;

import compiler.abstr.tree.*;
import compiler.seman.type.*;
import java.util.*;

public class SymbDesc {

  private static HashMap<AbsTree, Integer> scope =
      new HashMap<AbsTree, Integer>();

  public static void setScope(AbsTree node, int nodeScope) {
    scope.put(node, new Integer(nodeScope));
  }

  public static Integer getScope(AbsTree node) {
    Integer nodeScope = scope.get(node);
    return nodeScope;
  }

  private static HashMap<AbsTree, AbsDef> nameDef =
      new HashMap<AbsTree, AbsDef>();

  public static void setNameDef(AbsTree node, AbsDef def) {
    nameDef.put(node, def);
  }

  public static AbsDef getNameDef(AbsTree node) {
    AbsDef def = nameDef.get(node);
    return def;
  }

  private static HashMap<AbsTree, SemType> type =
      new HashMap<AbsTree, SemType>();

  public static void setType(AbsTree node, SemType typ) { type.put(node, typ); }

  public static SemType getType(AbsTree node) {
    SemType typ = type.get(node);
    return typ;
  }
}
