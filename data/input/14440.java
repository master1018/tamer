class SpecialLoadDecoder extends SpecialLoadStoreDecoder {
    SpecialLoadDecoder(int op3, String name, int specialRegNum) {
        super(op3, name, specialRegNum);
    }
    Instruction decodeSpecialLoadStoreInstruction(int cregNum,
                                    SPARCRegisterIndirectAddress addr,
                                    SPARCInstructionFactory factory) {
        return factory.newSpecialLoadInstruction(name, specialRegNum, cregNum, addr);
    }
}
