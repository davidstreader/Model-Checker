package mc.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ModelStatus {
  private static int sid = 1;

  int passCount;
  int failCount;
  int impliesConclusionTrue;
  int impliesAssumptionFalse;
  int id;
  int doneCount;
  long timeStamp;
    List<String> failures = new ArrayList<>();
    Stack<String> trace = new Stack<>();

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

  public List<String> getFailures() {return failures;}
    public void setFailures(List<String> f) { failures = f;}
    public void addFailure(String f) { failures.add(f);}
    public Stack<String> getTrace(){return trace;}
  public void setTrace(Stack<String> t){trace = t;}
  public void clearTrace() {trace = new Stack<>();}
  public int getPassCount() {
    return this.passCount;
  }

  public int getFailCount() {
    return this.failCount;
  }

  public int getImpliesConclusionTrue() {
    return this.impliesConclusionTrue;
  }

  public int getImpliesAssumptionFalse() {
    return this.impliesAssumptionFalse;
  }

  public int getId() {
    return this.id;
  }

  public int getDoneCount() {
    return this.doneCount;
  }

  public long getTimeStamp() {
    return this.timeStamp;
  }

  public void setPassCount(int passCount) {
    this.passCount = passCount;
  }

  public void setFailCount(int failCount) {
    this.failCount = failCount;
  }

  public void setImpliesConclusionTrue(int impliesConclusionTrue) {
    this.impliesConclusionTrue = impliesConclusionTrue;
  }

  public void setImpliesAssumptionFalse(int impliesAssumptionFalse) {
    this.impliesAssumptionFalse = impliesAssumptionFalse;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setDoneCount(int doneCount) {
    this.doneCount = doneCount;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ModelStatus)) return false;
    final ModelStatus other = (ModelStatus) o;
    if (!other.canEqual((Object) this)) return false;
    if (this.getPassCount() != other.getPassCount()) return false;
    if (this.getFailCount() != other.getFailCount()) return false;
    if (this.getImpliesConclusionTrue() != other.getImpliesConclusionTrue()) return false;
    if (this.getImpliesAssumptionFalse() != other.getImpliesAssumptionFalse()) return false;
    if (this.getId() != other.getId()) return false;
    if (this.getDoneCount() != other.getDoneCount()) return false;
    if (this.getTimeStamp() != other.getTimeStamp()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ModelStatus;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    result = result * PRIME + this.getPassCount();
    result = result * PRIME + this.getFailCount();
    result = result * PRIME + this.getImpliesConclusionTrue();
    result = result * PRIME + this.getImpliesAssumptionFalse();
    result = result * PRIME + this.getId();
    result = result * PRIME + this.getDoneCount();
    final long $timeStamp = this.getTimeStamp();
    result = result * PRIME + (int) ($timeStamp >>> 32 ^ $timeStamp);
    return result;
  }

  public String toString() {
    return "ModelStatus(passCount=" + this.getPassCount() + ", failCount=" + this.getFailCount() + ", impliesConclusionTrue=" + this.getImpliesConclusionTrue() + ", impliesAssumptionFalse=" + this.getImpliesAssumptionFalse() + ", id=" + this.getId() + ", doneCount=" + this.getDoneCount() + ", timeStamp=" + this.getTimeStamp() + ")";
  }
}
