package mc.compiler.ast;

import mc.util.Location;

/**
 * The LocalProcessNode covers "local processes", a processes defined as a child of a parent
 * process.
 * <p>
 * Note: Local Processes may have indexes
 * <p>
 * This is {@code SUBPROCESS :: PROCESS("," PROCESS)*"."}.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see ProcessNode
 */
public class LocalProcessNode extends ASTNode {

    /**
     * The "label" of the process.
     * C[$i][2][$k] implies variable $i and $k are symbolic
     * variable j is expanded for automata node where j=2
     */
    private String identifier;

    public void setIdentifierOnce(String var) {
        //System.out.println("\n   setIdentifierOnce var " + var);
        if (identifier.contains(var)) return;
        else {
            if (identifier.contains("[")) {
                String name = identifier.subSequence(0, identifier.indexOf("[")).toString();
                String end = identifier.subSequence(identifier.indexOf("["), identifier.length()).toString();
                //System.out.println("name " + name + "  end " + end);
                identifier = name + "[" + var + "]" + end;
            }  else {
                identifier =  var;
            }
  /*
      ranges.setRanges( ranges.getRanges().stream()
            .filter(x-> !x.getVariable().equals(var))
            .collect(Collectors.toList())); */
        }
        //System.out.println("   setIdentifierOnce ident " + identifier);
    }

    public void setIdentifierNotForALL(String var) {
        identifier = identifier +"[" + var + "]";
    }
    /**
     * The valid indexes this LocalProcess may have.
     */
    private RangesNode ranges;
    /**
     * The process itself.
     */
    private ASTNode process;

    /**
     * Initialises a new LocalProcessNode.
     *
     * @param identifier the name of the process {@link #identifier}
     * @param ranges     the valid range (if any) this process may use for indexing {@link #ranges}
     * @param process    the contents of the process {@link #process}
     * @param location   Where this LocalProcess is within the users code {@link ASTNode#location}
     */
    public LocalProcessNode(String identifier, RangesNode ranges,
                            ASTNode process, Location location) {
        super(location, "LocelProcess");
        this.identifier = identifier;
        this.ranges = ranges;
        this.process = process;
    }

    @Override
    public String myString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" Local Process " + identifier + " process ");
        if (process != null) sb.append(process.myString());
        sb.append(" ranges ");
        if (ranges != null) sb.append(ranges.myString());
        return sb.toString();
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public RangesNode getRanges() {
        return this.ranges;
    }

    public ASTNode getProcess() {
        return this.process;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setRanges(RangesNode ranges) {
        this.ranges = ranges;
    }

    public void setProcess(ASTNode process) {
        this.process = process;
    }

    public String toString() {
        return "LocalProcessNode(identifier=" + this.getIdentifier() + ", ranges=" + this.getRanges() + ", process=" + this.getProcess() + ")";
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof LocalProcessNode)) return false;
        final LocalProcessNode other = (LocalProcessNode) o;
        if (!other.canEqual((Object) this)) return false;
        if (!super.equals(o)) return false;
        final Object this$identifier = this.getIdentifier();
        final Object other$identifier = other.getIdentifier();
        if (this$identifier == null ? other$identifier != null : !this$identifier.equals(other$identifier))
            return false;
        final Object this$ranges = this.getRanges();
        final Object other$ranges = other.getRanges();
        if (this$ranges == null ? other$ranges != null : !this$ranges.equals(other$ranges)) return false;
        final Object this$process = this.getProcess();
        final Object other$process = other.getProcess();
        if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof LocalProcessNode;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = super.hashCode();
        final Object $identifier = this.getIdentifier();
        result = result * PRIME + ($identifier == null ? 43 : $identifier.hashCode());
        final Object $ranges = this.getRanges();
        result = result * PRIME + ($ranges == null ? 43 : $ranges.hashCode());
        final Object $process = this.getProcess();
        result = result * PRIME + ($process == null ? 43 : $process.hashCode());
        return result;
    }
}
