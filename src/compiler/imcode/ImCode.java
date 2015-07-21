package compiler.imcode;

import compiler.*;
import java.util.*;

public class ImCode {

  private boolean dump;

  public ImCode(boolean dump) { this.dump = dump; }

  public void dump(LinkedList<ImcChunk> chunks) {
    if (!dump)
      return;
    if (Report.dumpFile() == null)
      return;
    for (int chunk = 0; chunk < chunks.size(); chunk++)
      chunks.get(chunk).dump();
  }
}
