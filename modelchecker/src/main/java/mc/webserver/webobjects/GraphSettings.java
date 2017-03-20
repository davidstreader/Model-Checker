package mc.webserver.webobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class GraphSettings {
    private int autoMaxNode;
    private int passCount;
    private int failCount;
}
