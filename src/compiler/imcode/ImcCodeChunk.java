package compiler.imcode;

import compiler.*;
import compiler.frames.*;

public class ImcCodeChunk extends ImcChunk {

  public FrmFrame frame;

  public ImcStmt imcode;

  public ImcStmt lincode;

  public ImcCodeChunk(FrmFrame frame, ImcStmt imcode) {
    this.frame = frame;
    this.imcode = imcode;
    this.lincode = null;
  }

  @Override
  public void dump() {
    Report.dump(0, "CODE CHUNK: label=" + frame.label.name());
    Report.dump(2, frame.toString());
    if (lincode == null)
      imcode.dump(2);
    else
      lincode.dump(2);
  }
}
