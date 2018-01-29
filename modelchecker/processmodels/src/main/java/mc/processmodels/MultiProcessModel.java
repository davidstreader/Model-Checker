package mc.processmodels;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.EnumMap;
import java.util.Map;
import mc.util.Location;

public class MultiProcessModel extends ProcessModelObject implements ProcessModel {

  private Map<ProcessType,ProcessModel> processes = new EnumMap<>(ProcessType.class);
  private static Multimap<ProcessType,ProcessType> conversions = MultimapBuilder
      .enumKeys(ProcessType.class)
      .arrayListValues()
      .build();


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

  public boolean hasProcess(ProcessType type) {
    return processes.containsKey(type);
  }

  public ProcessModel getProcess(ProcessType type) {
    if (!hasProcess(type)) {
      //TODO: provide automatic conversion between types
    }
    return processes.get(type);
  }


  @Override
  public ProcessType getProcessType() {
    return null;
  }

  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public void setLocation(Location toThis) {
  }
}
