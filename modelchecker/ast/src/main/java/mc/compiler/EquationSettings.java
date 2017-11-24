package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class EquationSettings {
    boolean alphabet;
    int nodeCount;
    int alphabetCount;
    int maxTransitionCount;
}
