package mc.webserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import lombok.Getter;
import org.slf4j.LoggerFactory;

@Getter
public class Context {
  private boolean isFairAbstraction;
  private boolean isLocal;
  private boolean pruning;
  private GraphSettings graphSettings;

  static Context fromJSON(Object context) {
    JsonElement jsonElement = new Gson().toJsonTree(context);
    return new Gson().fromJson(jsonElement, Context.class);
  }

  @Getter
  class GraphSettings {
    private int autoMaxNode;
    private int petriMaxPlace;
    private int petriMaxTrans;
  }
}
