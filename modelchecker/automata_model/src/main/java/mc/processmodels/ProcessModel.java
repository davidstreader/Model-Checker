package mc.processmodels;

import mc.util.Location;

public interface ProcessModel {

  String getId();

  ProcessType getProcessType();

  Location getLocation();

  void setLocation(Location toThis);
}
