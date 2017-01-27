package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mc.compiler.OperationResult;
import mc.process_models.ProcessModel;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
public class ProcessReturn {
  public Map<String,ProcessModel> processes;
  public List<OperationResult> operations;
  public Object analysis;
  public Context context;
  public List<SkipObject> skipped;
  @AllArgsConstructor
  @Getter
  @Setter
  static class SkipObject {
    String id;
    String type;
    int length;
    int maxLength;
  }
}
