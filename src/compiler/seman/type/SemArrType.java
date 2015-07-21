package compiler.seman.type;

public class SemArrType extends SemType {

  public final SemType type;

  public final int size;

  public SemArrType(int size, SemType type) {
    this.type = type;
    this.size = size;
  }

  @Override
  public boolean sameStructureAs(SemType type) {
    if (type.actualType() instanceof SemArrType) {
      SemArrType arrayType = (SemArrType)(type.actualType());
      return (arrayType.size == size) &&
          (arrayType.type.sameStructureAs(this.type));
    } else
      return false;
  }

  @Override
  public String toString() {
    return "ARR(" + size + "," + type.toString() + ")";
  }

  @Override
  public int size() {
    return type.size() * size;
  }
}
