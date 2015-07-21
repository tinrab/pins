package compiler.seman.type;

import compiler.*;

public class SemAtomType extends SemType {

  public static final int LOG = 0;
  public static final int INT = 1;
  public static final int STR = 2;
  public static final int VOID = 3;

  public static final SemAtomType LOG_TYPE = new SemAtomType(LOG);
  public static final SemAtomType INT_TYPE = new SemAtomType(INT);
  public static final SemAtomType STR_TYPE = new SemAtomType(STR);
  public static final SemAtomType VOID_TYPE = new SemAtomType(VOID);

  public final int type;

  public SemAtomType(int type) { this.type = type; }

  @Override
  public boolean sameStructureAs(SemType type) {
    if (type.actualType() instanceof SemAtomType) {
      SemAtomType atomType = (SemAtomType)(type.actualType());
      return this.type == atomType.type;
    } else
      return false;
  }

  @Override
  public String toString() {
    switch (type) {
    case LOG:
      return "LOGICAL";
    case INT:
      return "INTEGER";
    case STR:
      return "STRING";
    case VOID:
      return "VOID";
    }
    Report.error(
        "Internal error :: compiler.seman.type.SemAtomType.toString()");
    return "";
  }

  @Override
  public int size() {
    return 4;
  }
}
