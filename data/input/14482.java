class V9AlternateSpaceLoadDecoder extends V9AlternateSpaceDecoder {
    V9AlternateSpaceLoadDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeV9AsiLoadStore(int instruction,
                                     SPARCV9RegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCV9InstructionFactory factory) {
        return factory.newLoadInstruction(name, op3, addr, rd, dataType);
    }
}
