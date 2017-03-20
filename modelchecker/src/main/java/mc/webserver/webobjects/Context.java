package mc.webserver.webobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Context {
    private boolean isFairAbstraction;
    private boolean isLocal;
    private boolean pruning;
    private GraphSettings graphSettings;

}
