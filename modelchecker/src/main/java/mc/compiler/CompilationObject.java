package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.process_models.ProcessModel;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CompilationObject {
    private Map<String, ProcessModel> processMap;
    private List<OperationResult> operationResults;
    private List<OperationResult> equationResults;
}
