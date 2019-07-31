package mc;

public class Constant {

    public static final String HIDDEN = "\u03C4";  // tau event
    public static final String DEADLOCK = "\u03B4";  // delta event
    public static final String BROADCASTSoutput = "!";
    public static final String BROADCASTSinput = "?";
    public static final String ACTIVE = "^";
    public static final String EPSILON  = "\u03B5";  // empty trace
    public static final String EPSILONr  = "\u03B5r";  // empty trace
    public static final String EPSILONe  = "\u03B5e";  // empty trace
    public static final String STOP = "STOP";
    public static final String END = "END";  // only for non cong but completeTrace
    public static final String ERROR = "ERROR";
    public static final String Start = "Start";
    public static final String Quiescent = "Quiescent";

    public static final String CONGURENT = "cong";
    public static final String OBSEVATIONAL = "obs";
    public static final String FAIR = "fair";
    public static final String UNFAIR = "unfair";
    public static final String OWNED = "owned";
    public static final String SEQUENTIAL = "sequential";
    public static final String CONCURRENT = "concurrent";
    public static final String FORCED = "forced";

    public static final String AUTOMATA = "automata";
    public static final String PETRINET = "petrinet";

    public static boolean externalOrEND(String lab) {
        return external(lab) || lab.equals(Constant.END);
    }

    public static boolean external(String lab) {

        return lab.equals(Constant.DEADLOCK) ||
          lab.equals(Constant.STOP) ||
          lab.equals(Constant.Start) ||
          lab.equals(Constant.ERROR)||
          lab.equals(Constant.END);
    }
    public static boolean observable(String lab) {

        return
          lab.equals(Constant.STOP) ||
          lab.equals(Constant.Start) ;
    }
    public static boolean terminal(String lab) {
        return lab.equals(Constant.DEADLOCK) ||
          lab.equals(Constant.STOP) ||
          lab.equals(Constant.ERROR);
    }
    public static boolean start(String lab) {
        return lab.equals(Constant.Start);
    }

}
