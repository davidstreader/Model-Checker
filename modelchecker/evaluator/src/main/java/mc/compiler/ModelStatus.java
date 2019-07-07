package mc.compiler;

import lombok.Data;

@Data
public class ModelStatus {
  private static int sid = 1;

  int passCount;
  int failCount;
  int impliesConclusionTrue;
  int impliesAssumptionFalse;
  int id;
  int doneCount;
  long timeStamp;

  public String myString() {
    return "ModelStatus id " + id + " pass " + passCount +
           " fail " + failCount +
           " done " + doneCount +
           " impliesShort "+ impliesAssumptionFalse +
           "/"+impliesConclusionTrue+" ";
  }
  public ModelStatus() {
    id = sid++;
  }
}
