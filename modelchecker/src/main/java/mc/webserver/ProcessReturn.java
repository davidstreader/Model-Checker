package mc.webserver;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
public class ProcessReturn {
  public Map<String,Map<String,?>> processes;
  public Map<String,Object> operations;
  public Object analysis;
  public Map<String,Object> context;
  public List<String> skipped;


}
