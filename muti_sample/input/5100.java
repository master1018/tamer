public class SPARCRestoreInstruction extends SPARCFormat3AInstruction {
    final private boolean trivial;
    public SPARCRestoreInstruction(SPARCRegister rs1, ImmediateOrRegister operand2, SPARCRegister rd) {
        super("restore", RESTORE, rs1, operand2, rd);
        SPARCRegister G0 = SPARCRegisters.G0;
        trivial = (rs1 == G0 && operand2 == G0 && rd == G0);
    }
    public boolean isTrivial() {
        return trivial;
    }
    protected String getDescription() {
        return (trivial) ? getName() : super.getDescription();
    }
}
