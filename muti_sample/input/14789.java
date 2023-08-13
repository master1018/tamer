class V9AlternateSpaceStoreDecoder extends V9AlternateSpaceDecoder {
    V9AlternateSpaceStoreDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeV9AsiLoadStore(int instruction,
                                     SPARCV9RegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCV9InstructionFactory factory) {
        return factory.newStoreInstruction(name, op3, addr, rd, dataType);
    }
}
