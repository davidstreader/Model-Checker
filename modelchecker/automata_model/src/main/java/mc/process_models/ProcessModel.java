package mc.process_models;

import mc.util.Location;

public interface ProcessModel {

    String getId();
    ProcessType getProcessType();

    void setLocation(Location toThis);
    Location getLocation();
}
