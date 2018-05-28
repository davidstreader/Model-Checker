package mc.processmodels;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.EnumMap;
import java.util.Map;

import lombok.Getter;
import mc.util.Location;

public class MultiProcessModel extends ProcessModelObject implements ProcessModel {


  private Map<ProcessType,ProcessModel> processes = new EnumMap<>(ProcessType.class);
  private static Multimap<ProcessType,ProcessType> conversions = MultimapBuilder
      .enumKeys(ProcessType.class)
      .arrayListValues()
      .build();

  @Getter
  private Mapping processNodesMapping;



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
  }


    /**
     *  This method is for the case where we have converted one type to another,
     *  it gives us the mapping of which node
     *  is responsible for what, and visa versa for markings to node.
     */
  public void addProcessesMapping(Mapping pnm) {
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

  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public void setLocation(Location toThis) {
  }
}
