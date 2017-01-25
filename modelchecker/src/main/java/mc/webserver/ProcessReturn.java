package mc.webserver;

import lombok.AllArgsConstructor;
import mc.process_models.ProcessModel;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
public class ProcessReturn {
  public Map<String,ProcessModel> processes;
  public Map<String,Object> operations;
  public Object analysis;
  public Map<String,Object> context;
  public List<String> skipped;
}
