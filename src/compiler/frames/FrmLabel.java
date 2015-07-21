package compiler.frames;

public class FrmLabel {

  private String name;

  private FrmLabel(String name) { this.name = name; }

  @Override
  public boolean equals(Object l) {
    return name == ((FrmLabel)l).name;
  }

  public String name() { return name; }

  private static int label_count = 0;

  public static FrmLabel newLabel() {
    return new FrmLabel("L" + (label_count++));
  }

  public static FrmLabel newLabel(String name) {
    return new FrmLabel("_" + name);
  }
}
