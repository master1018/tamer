public class SPARCLdstubInstruction extends SPARCAtomicLoadStoreInstruction {
    public SPARCLdstubInstruction(String name, SPARCRegisterIndirectAddress addr, SPARCRegister rd) {
        super(name, addr, rd);
    }
    public int getDataType() {
        return RTLDT_UNSIGNED_BYTE;
    }
    public boolean isConditional() {
        return false;
    }
}
