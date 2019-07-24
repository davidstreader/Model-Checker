package mc.processmodels;

import mc.util.Location;

public interface ProcessModel {

  String getId();

  void setId(String id);

  ProcessType getProcessType();

  Location getLocation();

  void setLocation(Location toThis);
   boolean isSequential();
}
