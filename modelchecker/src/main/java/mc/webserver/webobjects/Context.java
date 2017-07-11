package mc.webserver.webobjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Context {
    private boolean liveCompiling, fairAbstraction, pruning, darkTheme, saveCode, saveLayout, autoSave;
    private int nodeSep, autoMaxNode, failCount, passCount;
    private String currentFile;
}
