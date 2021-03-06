package mc.processmodels;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.util.Location;

public class MultiProcessModel extends ProcessModelObject implements ProcessModel  {


  private Map<ProcessType,ProcessModel> processes = new EnumMap<>(ProcessType.class);
  private static Multimap<ProcessType,ProcessType> conversions = MultimapBuilder
      .enumKeys(ProcessType.class)
      .arrayListValues()
      .build();

  private boolean sequential = false;
  public boolean isSequential() {return sequential;}
  @Getter
  private MappingNdMarking processNodesMapping;

  public MultiProcessModel reId(String tag)  throws  CompilationException{

      MultiProcessModel mod = new MultiProcessModel(getId() + tag);
      mod.addProcess(((Petrinet) this.processes.get(ProcessType.PETRINET)).reId(tag));
      Automaton a = ((Automaton) this.processes.get(ProcessType.AUTOMATA));
      mod.addProcess(a);
      mod.processNodesMapping =  MappingNdMarking.reId(this.processNodesMapping,tag,a.getId());


      return mod;

  }

  public MultiProcessModel(String id) {
    super(id, "multiProcess");
  }

  public void addProcess(ProcessModel pm) {
    if (pm instanceof MultiProcessModel) {
      //TODO: provide automatic conversion between types
      processes.putAll(((MultiProcessModel) pm).processes);
      return;
    }
    processes.put(pm.getProcessType(),pm);
    sequential = pm.isSequential();
  }


    /**
     *  This method is for the case where we have converted one type to another,
     *  it gives us the mapping of which node
     *  is responsible for what, and visa versa for markings to node.
     */
  public void addProcessesMapping(MappingNdMarking pnm) {
    processNodesMapping = pnm;
  }

  public boolean hasProcess(ProcessType type) {
    return processes.containsKey(type);
  }

  public ProcessModel getProcess(ProcessType type) {
    if (!hasProcess(type)) {
      //TODO always build both automata and PetriNets
    }
    return processes.get(type);
  }


  @Override
  public ProcessType getProcessType() {
    return ProcessType.MULTI_PROCESS;
  }

  //I think this is all void now Automata less important than PetriNets
  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public void setLocation(Location toThis) {
  }
}
