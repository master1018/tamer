class LoadDecoder extends MemoryInstructionDecoder {
    LoadDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                     SPARCRegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCInstructionFactory factory) {
        return factory.newLoadInstruction(name, op3, addr, rd, dataType);
    }
}
