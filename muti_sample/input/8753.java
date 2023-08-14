public class SPARCSwapInstruction extends SPARCAtomicLoadStoreInstruction {
    public SPARCSwapInstruction(String name, SPARCRegisterIndirectAddress addr, SPARCRegister rd) {
        super(name, addr, rd);
    }
    public int getDataType() {
        return RTLDT_UNSIGNED_WORD;
    }
    public boolean isConditional() {
        return false;
    }
}
