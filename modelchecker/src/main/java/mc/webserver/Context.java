package mc.webserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.slf4j.LoggerFactory;

@Getter
public class Context {
  boolean isFairAbstraction;
  boolean isLocal;
  boolean pruning;
  GraphSettings graphSettings;

  public static Context fromJSON(Object context) {
    LoggerFactory.getLogger(Context.class).info(context+"");
    JsonElement jsonElement = new Gson().toJsonTree(context);
    return new Gson().fromJson(jsonElement, Context.class);
  }

  @Getter
  private class GraphSettings {
    int autoMaxNode;
    int petriMaxPlace;
    int petriMaxTrans;
  }
}
