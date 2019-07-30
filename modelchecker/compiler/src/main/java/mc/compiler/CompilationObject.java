package mc.compiler;

import mc.processmodels.ProcessModel;

import java.util.List;
import java.util.Map;

/**
 *  This class is a return type for the calculations regarding diagram creation and operation output.
 *  Used to pass information from the back end to be displayed
 */
public class CompilationObject {
    /**
     * processMap stores the model name to ProcessModel. I.e "a" -> diagram
     */
    private Map<String, ProcessModel> processMap;
    /**
     *  operationResults stores the outcome of operation {}, user label transition system (LTS) code.
     */
    private List<OperationResult> operationResults;

    private List<OperationResult> equationResults;

  public CompilationObject(Map<String, ProcessModel> processMap, List<OperationResult> operationResults, List<OperationResult> equationResults) {
    this.processMap = processMap;
    this.operationResults = operationResults;
    this.equationResults = equationResults;
  }

  public Map<String, ProcessModel> getProcessMap() {
    return this.processMap;
  }

  public List<OperationResult> getOperationResults() {
    return this.operationResults;
  }

  public List<OperationResult> getEquationResults() {
    return this.equationResults;
  }
  //private List<ImpliesResult> impliesResults;
}
