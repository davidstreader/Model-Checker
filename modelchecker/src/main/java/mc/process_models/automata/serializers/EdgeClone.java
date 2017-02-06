package mc.process_models.automata.serializers;


import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;

public class EdgeClone {
    private String to,from;
    public EdgeClone(AutomatonEdge edge) {
        this.to = edge.getTo().getId();
        this.from = edge.getFrom().getId();
    }
    public void apply(AutomatonEdge edge, Automaton automaton) throws CompilationException {
        edge.setFrom(automaton.getNode(from));
        edge.setTo(automaton.getNode(to));
    }
}
