package mc.compiler;

import lombok.Getter;
import mc.process_models.ProcessModel;

import java.util.List;
import java.util.Map;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class CompilationObject {

    @Getter
    private Map<String, ProcessModel> processMap;
    @Getter
    private List<Boolean> operationResults;

    public CompilationObject(Map<String, ProcessModel> processMap, List<Boolean> operationResults){
        this.processMap = processMap;
        this.operationResults = operationResults;
    }

}
