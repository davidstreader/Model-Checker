package mc;

public class Constant {

    public static final String HIDDEN = "\u03C4";
    public static final String DEADLOCK = "\u03B4";
    public static final String BROADCASTSoutput = "!";
    public static final String BROADCASTSinput = "?";
    public static final String ACTIVE = "^";
    public static final String STOP = "STOP";
    public static final String ERROR = "ERROR";
    public static final String Start = "Start";
    public static final String Quiescent = "Quiescent";

    public static final String CONGURENT = "cong";
    public static final String FAIR = "fair";
    public static final String UNFAIR = "unfair";

    public static boolean external(String lab) {

        return lab.equals(Constant.DEADLOCK) ||
          lab.equals(Constant.STOP) ||
          lab.equals(Constant.Start) ||
          lab.equals(Constant.ERROR);
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
