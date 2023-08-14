public class SPARCV9CasInstruction extends SPARCAtomicLoadStoreInstruction
                    implements SPARCV9Instruction {
    final private SPARCRegister rs2;
    final private int dataType;
    public SPARCV9CasInstruction(String name, SPARCRegisterIndirectAddress addr,
                      SPARCRegister rs2, SPARCRegister rd, int dataType) {
        super(name, addr, rd);
        this.rs2 = rs2;
        this.dataType = dataType;
    }
    public int getDataType() {
        return dataType;
    }
    public boolean isConditional() {
        return true;
    }
    public SPARCRegister getComparisonRegister() {
        return rs2;
    }
}
