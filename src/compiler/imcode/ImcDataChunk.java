package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcDataChunk extends ImcChunk {

  public FrmLabel label;

  public int size;

  public ImcDataChunk(FrmLabel label, int size) {
    this.label = label;
    this.size = size;
  }

  @Override
  public void dump() {
    Report.dump(0, "DATA CHUNK: label=" + label.name() + " size=" + size);
  }
}
