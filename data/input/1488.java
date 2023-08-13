public class SPARCSpecialStoreInstruction
                        extends SPARCSpecialRegisterInstruction
                        implements  SPARCSpecialRegisters {
    final private int specialReg;
    final private int cregNum;
    final private SPARCRegisterIndirectAddress addr;
    public SPARCSpecialStoreInstruction(String name, int specialReg, int cregNum,
                                             SPARCRegisterIndirectAddress addr) {
        super(name);
        this.specialReg = specialReg;
        this.addr = addr;
        this.cregNum = cregNum;
    }
    public SPARCSpecialStoreInstruction(String name, int specialReg,
                                             SPARCRegisterIndirectAddress addr) {
        this(name, specialReg, -1, addr);
    }
    public int getSpecialRegister() {
        return specialReg;
    }
    public int getCoprocessorRegister() {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(specialReg == CREG, "not a special register");
        return cregNum;
    }
    public Address getDestination() {
        return addr;
    }
    protected String getDescription() {
        StringBuffer buf = new StringBuffer(getName());
        buf.append(spaces);
        if (specialReg == CREG) {
            buf.append("creg" + cregNum);
        } else {
            buf.append(getSpecialRegisterName(specialReg));
        }
        buf.append(comma);
        buf.append(addr.toString());
        return buf.toString();
    }
}
