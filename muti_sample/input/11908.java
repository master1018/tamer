class LdstubDecoder extends MemoryInstructionDecoder {
    LdstubDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                     SPARCRegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCInstructionFactory factory) {
        return factory.newLdstubInstruction(name, addr, rd);
    }
}
