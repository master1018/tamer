class AlternateSpaceStoreDecoder extends StoreDecoder {
    AlternateSpaceStoreDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    protected Instruction decodeMemoryInstruction(int instruction,
                                               SPARCRegisterIndirectAddress addr,
                                               SPARCRegister rd,
                                               SPARCInstructionFactory factory) {
        setAddressSpace(instruction, addr);
        return factory.newStoreInstruction(name, op3, addr, rd, dataType);
    }
}
