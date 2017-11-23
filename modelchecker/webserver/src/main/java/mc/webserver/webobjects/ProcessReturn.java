package mc.webserver.webobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.compiler.OperationResult;
import mc.process_models.ProcessModel;
import mc.webserver.Context;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
@ToString
public class ProcessReturn {
  public Map<String,ProcessModel> processes;
  public List<OperationResult> operations;
  public List<OperationResult> equations;
  public Context context;
  public List<SkipObject> skipped;
  @AllArgsConstructor
  @Getter
  @Setter
  public static class SkipObject {
    String id;
    String type;
    int length;
    int maxLength;
  }
}
