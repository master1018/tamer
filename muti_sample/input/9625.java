public class SPARCReturnInstruction extends SPARCJmplInstruction
    implements ReturnInstruction {
    private final boolean leaf;
    public SPARCReturnInstruction(SPARCRegisterIndirectAddress addr, SPARCRegister rd, boolean leaf) {
        super(leaf? "retl" : "ret", addr, rd);
        this.leaf = leaf;
    }
    public boolean isLeaf() {
        return leaf;
    }
    protected String getDescription() {
        return getName();
    }
}
