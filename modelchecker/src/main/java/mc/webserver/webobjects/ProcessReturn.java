package mc.webserver.webobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mc.compiler.OperationResult;
import mc.process_models.ProcessModel;
import mc.webserver.webobjects.Context;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
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
