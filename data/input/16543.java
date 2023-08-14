class SpecialStoreDecoder extends SpecialLoadStoreDecoder {
    SpecialStoreDecoder(int op3, String name, int specialRegNum) {
        super(op3, name, specialRegNum);
    }
    Instruction decodeSpecialLoadStoreInstruction(int cregNum,
                                    SPARCRegisterIndirectAddress addr,
                                    SPARCInstructionFactory factory) {
        return factory.newSpecialStoreInstruction(name, specialRegNum, cregNum, addr);
    }
}
