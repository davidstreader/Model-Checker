package mc.process_models;

public interface ProcessModel {

    void addMetaData(String key, Object value);
    void removeMetaData(String key);
    boolean hasMetaData(String key);
    String getId();
    ProcessType getProcessType();
}
