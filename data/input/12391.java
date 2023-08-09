class StoreDecoder extends MemoryInstructionDecoder {
    StoreDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeMemoryInstruction(int instruction,
                                     SPARCRegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCInstructionFactory factory) {
        return factory.newStoreInstruction(name, op3, addr, rd, dataType);
    }
}
