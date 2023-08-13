abstract class SpecialLoadStoreDecoder extends MemoryInstructionDecoder {
    final int specialRegNum;
    SpecialLoadStoreDecoder(int op3, String name, int specialRegNum) {
        super(op3, name, RTLDT_UNKNOWN);
        this.specialRegNum = specialRegNum;
    }
    final Instruction decodeMemoryInstruction(int instruction,
                                      SPARCRegisterIndirectAddress addr,
                                      SPARCRegister rd, SPARCInstructionFactory factory) {
        int cregNum = getSourceRegister1(instruction);
        return decodeSpecialLoadStoreInstruction(cregNum, addr, factory);
    }
    abstract Instruction decodeSpecialLoadStoreInstruction(int cregNum,
                                      SPARCRegisterIndirectAddress addr,
                                      SPARCInstructionFactory factory);
}
