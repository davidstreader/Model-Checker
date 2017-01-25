package mc.compiler;

import mc.compiler.ast.*;
import mc.util.Location;
import mc.util.expr.Expression;
import mc.webserver.LogMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONToASTConverter {

  private Map<String, String> variableMap;

  public AbstractSyntaxTree convert(JSONObject ast){
    new LogMessage("Importing AST").send();
    JSONObject variables = ast.getJSONObject("variableMap");
    constructVariableMap(variables);

    List<ProcessNode> processes = new ArrayList<ProcessNode>();
    JSONArray jsonProcesses = ast.getJSONArray("processes");
    for(int i = 0; i < jsonProcesses.length(); i++){
      ProcessNode process = convertProcessNode(jsonProcesses.getJSONObject(i));
      processes.add(process);
    }

    List<OperationNode> operations = new ArrayList<OperationNode>();
    JSONArray jsonOperations = ast.getJSONArray("operations");
    for(int i = 0; i < jsonOperations.length(); i++){
      OperationNode operation = convertOperationNode(jsonOperations.getJSONObject(i));
      operations.add(operation);
    }

    return new AbstractSyntaxTree(processes, operations, variableMap);
  }

  private void constructVariableMap(JSONObject variables){
    variableMap = new HashMap<String, String>();
    Set<String> keys = variables.keySet();

    Pattern pattern = Pattern.compile("\\$v[0-9]+");
    for(String key : keys){
      String expression = variables.getString(key);
      boolean matchFound = true;
      while(matchFound){
        Matcher matcher = pattern.matcher(expression);
        if(matcher.find()){
          String variable = matcher.group();
          expression = expression.replaceFirst(variable, variables.getString(variable));
        }
        else{
          matchFound = false;
        }
      }

      variableMap.put(key, expression);
    }
  }

  public ASTNode convertJSONNode(JSONObject json){
    String type = json.getString("type");
    ASTNode node;
    switch(type){
      case "identifier":
        node = convertIdentifierNode(json);
        break;
      case "action-label":
        node = convertActionLabelNode(json);
        break;
      case "index":
        node = convertIndexNode(json);
        break;
      case "range":
        node = convertRangeNode(json);
        break;
      case "set":
        node = convertSetNode(json);
        break;
      case "process":
        node = convertProcessNode(json);
        break;
      case "composite":
        node = convertCompositeNode(json);
        break;
      case "choice":
        node = convertChoiceNode(json);
        break;
      case "sequence":
        node = convertSequenceNode(json);
        break;
      case "terminal":
        node = convertTerminalNode(json);
        break;
      case "if-statement":
        node = convertIfStatementNode(json);
        break;
      case "function":
        node = convertFunctionNode(json);
        break;
      case "forall":
        node = convertForAllStatementNode(json);
        break;
      case "ranges":
        node = convertRangesNode(json);
        break;
      case "relabel":
        node = convertRelabelNode(json);
        break;
      case "hiding":
        node = convertHidingNode(json);
        break;
      case "interrupt":
        node = convertInterruptNode(json);
        break;
      case "operation":
        node = convertOperationNode(json);
        break;
      default:
        throw new IllegalArgumentException(type + " is not a correct json object");
    }

    if(json.has("label")){
      ActionLabelNode action = convertActionLabelNode(json.getJSONObject("label"));
      node.setLabel(action.getAction());
    }

    if(json.has("relabel")){
      RelabelNode relabel = convertRelabelNode(json.getJSONObject("relabel"));
      node.setRelabelNode(relabel);
    }

    return node;
  }

  public IdentifierNode convertIdentifierNode(JSONObject json){
    String identifier = json.getString("ident");
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new IdentifierNode(identifier, location);
  }

  public ActionLabelNode convertActionLabelNode(JSONObject json){
    String action = json.getString("action");
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new ActionLabelNode(action, location);
  }

  public IndexNode convertIndexNode(JSONObject json){
    String variable = json.getString("variable");
    ASTNode range = convertJSONNode(json.getJSONObject("range"));
    ASTNode process = null;
    if(json.has("process")){
      process = convertJSONNode(json.getJSONObject("process"));
    }
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new IndexNode(variable, range, process, location);
  }

  public RangeNode convertRangeNode(JSONObject json){
    int start = json.getInt("start");
    int end = json.getInt("end");
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new RangeNode(start, end, location);
  }

  public SetNode convertSetNode(JSONObject json){
    JSONArray array = json.getJSONArray("set");
    Set<String> set = new HashSet<String>();
    for(int i = 0; i < array.length(); i++){
      set.add(array.getString(i));
    }
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new SetNode(set, location);
  }

  public ProcessNode convertProcessNode(JSONObject json){
    String type = json.getString("processType");
    IdentifierNode identNode = (IdentifierNode)convertJSONNode(json.getJSONObject("ident"));
    String identifier = identNode.getIdentifier();
    ASTNode process = convertJSONNode(json.getJSONObject("process"));
    JSONArray array = json.getJSONArray("local");
    List<LocalProcessNode> localProcesses = new ArrayList<LocalProcessNode>();
    for(int i = 0; i < array.length(); i++){
      LocalProcessNode localProcess = convertLocalProcessNode(array.getJSONObject(i));
      localProcesses.add(localProcess);
    }

    JSONObject jsonHiding = json.optJSONObject("hiding");
    HidingNode hiding = null;
    if(jsonHiding != null){
      hiding = convertHidingNode(jsonHiding);
    }

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new ProcessNode(type, identifier, process, localProcesses, hiding, location);
  }

  public LocalProcessNode convertLocalProcessNode(JSONObject json){
    IdentifierNode identNode = convertIdentifierNode(json.getJSONObject("ident"));
    String identifier = identNode.getIdentifier();
    RangesNode ranges= null;
    if (json.has("ranges"))
      ranges = convertRangesNode(json.getJSONObject("ranges"));
    ASTNode process = convertJSONNode(json.getJSONObject("process"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new LocalProcessNode(identifier, ranges, process, location);
  }

  public CompositeNode convertCompositeNode(JSONObject json){
    ASTNode firstProcess = convertJSONNode(json.getJSONObject("process1"));
    ASTNode secondProcess = convertJSONNode(json.getJSONObject("process2"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new CompositeNode(firstProcess, secondProcess, location);
  }

  public ChoiceNode convertChoiceNode(JSONObject json){
    ASTNode firstProcess = convertJSONNode(json.getJSONObject("process1"));
    ASTNode secondProcess = convertJSONNode(json.getJSONObject("process2"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new ChoiceNode(firstProcess, secondProcess, location);
  }

  public SequenceNode convertSequenceNode(JSONObject json){
    ActionLabelNode from = (ActionLabelNode)convertJSONNode(json.getJSONObject("from"));
    ASTNode to = convertJSONNode(json.getJSONObject("to"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new SequenceNode(from, to, location);
  }

  public TerminalNode convertTerminalNode(JSONObject json){
    String terminal = json.getString("terminal");
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new TerminalNode(terminal, location);
  }

  public IfStatementNode convertIfStatementNode(JSONObject json){
    String guard = json.getString("guard");
    Expression condition = Expression.constructExpression(variableMap.get(guard));
    ASTNode trueBranch = convertJSONNode(json.getJSONObject("trueBranch"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);

    if(!json.has("falseBranch")){
      return new IfStatementNode(condition, trueBranch, location);
    }

    ASTNode falseBranch = convertJSONNode(json.getJSONObject("falseBranch"));
    return new IfStatementNode(condition, trueBranch, falseBranch, location);
  }

  public FunctionNode convertFunctionNode(JSONObject json){
    String function = json.getString("func");
    ASTNode process = convertJSONNode(json.getJSONObject("process"));
    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new FunctionNode(function, process, location);
  }

  public ForAllStatementNode convertForAllStatementNode(JSONObject json){
    RangesNode ranges = (RangesNode)convertJSONNode(json.getJSONObject("ranges"));
    ASTNode process = convertJSONNode(json.getJSONObject("process"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new ForAllStatementNode(ranges, process, location);
  }

  public RangesNode convertRangesNode(JSONObject json){
    JSONArray array = json.getJSONArray("ranges");
    List<IndexNode> ranges = new ArrayList<IndexNode>();
    for(int i = 0; i < array.length(); i++){
      ASTNode node = convertJSONNode(array.getJSONObject(i));
      if(node instanceof IndexNode) {
        ranges.add((IndexNode)node);
      }
    }

    //JSONObject jsonLocation = json.getJSONObject("location"); // TODO: fix js parser, doesn't assign location information
    //Location location = convertLocation(jsonLocation);
    return new RangesNode(ranges, null);
  }

  public RelabelNode convertRelabelNode(JSONObject json){
    JSONArray array = json.getJSONArray("set");
    List<RelabelElementNode> relabels = new ArrayList<RelabelElementNode>();
    for(int i = 0; i < array.length(); i++){
      RelabelElementNode relabel = convertRelabelElementNode(array.getJSONObject(i));
      relabels.add(relabel);
    }

    //JSONObject jsonLocation = json.getJSONObject("location"); // TODO: fix js parser, doesn't assign location information
    //Location location = convertLocation(jsonLocation);
    return new RelabelNode(relabels, null);
  }

  public RelabelElementNode convertRelabelElementNode(JSONObject json){
    ActionLabelNode newLabel = convertActionLabelNode(json.getJSONObject("newLabel"));
    ActionLabelNode oldLabel = (ActionLabelNode)convertJSONNode(json.getJSONObject("oldLabel"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new RelabelElementNode(newLabel.getAction(), oldLabel.getAction(), location);
  }

  public HidingNode convertHidingNode(JSONObject json){
    String type = json.getString("type");
    JSONArray array = json.getJSONArray("set");
    Set<String> set = new HashSet<String>();
    for(int i = 0; i < array.length(); i++){
      String element = array.getString(i);
      set.add(element);
    }

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);

    return new HidingNode(type, set, location);
  }

  public InterruptNode convertInterruptNode(JSONObject json){
    ActionLabelNode action = convertActionLabelNode(json.getJSONObject("action"));
    ASTNode process = convertJSONNode(json.getJSONObject("process"));

    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new InterruptNode(action, process, location);
  }

  public OperationNode convertOperationNode(JSONObject json){
    String operation = json.getString("operation");
    boolean isNegated = json.getBoolean("isNegated");
    ASTNode firstProcess = convertJSONNode(json.getJSONObject("process1"));
    ASTNode secondProcess = convertJSONNode(json.getJSONObject("process2"));


    JSONObject jsonLocation = json.getJSONObject("location");
    Location location = convertLocation(jsonLocation);
    return new OperationNode(operation, isNegated, firstProcess, secondProcess, location);
  }

  public Location convertLocation(JSONObject json){
    JSONObject start = json.getJSONObject("start");
    JSONObject end = json.getJSONObject("end");
    int lineStart = start.getInt("line");
    int colStart = start.getInt("col");
    int lineEnd = end.getInt("line");
    int colEnd = end.getInt("col");

    return new Location(lineStart, colStart, lineEnd, colEnd);
  }
}
