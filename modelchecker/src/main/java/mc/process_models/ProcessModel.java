package mc.process_models;

/**
 * Created by sheriddavi on 24/01/17.
 */
public interface ProcessModel {

    void addMetaData(String key, Object value);
    void removeMetaData(String key);
    boolean hasMetaData(String key);

}
