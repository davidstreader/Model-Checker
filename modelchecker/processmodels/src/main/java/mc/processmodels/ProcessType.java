package mc.processmodels;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;

/**
 * This holds the type information for every {@link ProcessModel}.
 * <p>
 * This also contains methods to convert between the different {@link ProcessModel}s.
 *
 * @author Jacob Beal
 * @see TokenRule
 * @see MultiProcessModel
 */
@RequiredArgsConstructor
public enum ProcessType {
  AUTOMATA(Automaton.class),
  PETRINET(Petrinet.class),
  MULTI_PROCESS(MultiProcessModel.class);

  /**
   * This contains a static reference to every conversion map.
   */
  private static Map<ProcessType,Map> conversionsPossible = new HashMap<>();

  /**
   * This contains all the functions to convert from an automaton to another process model.
   */
  private static Map<ProcessType,Function<Automaton,? extends ProcessModel>>
      AUTOMATA_CONVERSIONS = new HashMap<>();


  /**
   * This contains all the functions to convert from a petrinet to another process model.
   */
  private static Map<ProcessType,Function<Petrinet,? extends ProcessModel>>
      PETRINET_CONVERSIONS = new HashMap<>();

  /**
   * This contains all the functions to convert from a multiprocess into a constituent form.
   */
  private static Map<ProcessType,Function<MultiProcessModel, ? extends ProcessModel>>
      MULTIPROCESS_CONVERSIONS = new HashMap<>();

  /**
   * Initialise the conversions maps.
   * Cannot be done statically as Enums don't allow this behaviour.
   */
  private static void init() {
    PETRINET_CONVERSIONS.put(AUTOMATA, TokenRule::tokenRule);
    MULTIPROCESS_CONVERSIONS.put(AUTOMATA, p -> p.getProcess(AUTOMATA));
    MULTIPROCESS_CONVERSIONS.put(PETRINET, p -> p.getProcess(PETRINET));

    conversionsPossible.put(AUTOMATA,AUTOMATA_CONVERSIONS);
    conversionsPossible.put(PETRINET,PETRINET_CONVERSIONS);
    conversionsPossible.put(MULTI_PROCESS,MULTIPROCESS_CONVERSIONS);
  }

  private Map conversion = null;
  public final Class<? extends ProcessModel> type;

  /**
   * This function returns a process model,
   * converted from a given type into the provided process type.
   *
   * @param convertTo the Process Type the provided process will be converted into
   * @param pm the Process being converted
   * @param <R> The return type ({@code covertTo}
   * @param <P> The process model type ({@code pm})
   * @return {@code pm} converted into type {@code <R>}
   * @throws CompilationException when there is no valid conversion between the given types
   */
  @SuppressWarnings("unchecked")
  public <R extends ProcessModel,P extends ProcessModel> R convertTo(ProcessType convertTo, P pm)
      throws CompilationException {

    if (this == convertTo) {
      return (R) pm;
    }
    //
    if (!canConvertTo(convertTo)) {
      throw new CompilationException(ProcessType.class,
          "Cannot convert " + this + " to a " + convertTo);
    }
    return ((Function<P,R>) conversion.get(convertTo)).apply(pm);
  }

  public boolean canConvertTo(ProcessType type) {
    if (conversion == null) {
      init();
      conversion = conversionsPossible.get(this);
    }

    return conversion.containsKey(type);
  }
}
