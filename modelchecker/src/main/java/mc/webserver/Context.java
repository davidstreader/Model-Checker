package mc.webserver;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Context {
    private boolean isFairAbstraction;
    private boolean isLocal;
    private boolean pruning;
    private GraphSettings graphSettings;

    @NoArgsConstructor
    @Getter
    class GraphSettings {
        private int autoMaxNode;
        private int petriMaxPlace;
        private int petriMaxTrans;
    }
}
