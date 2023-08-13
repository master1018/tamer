class V9AlternateSpaceSwapDecoder extends V9AlternateSpaceDecoder {
    V9AlternateSpaceSwapDecoder(int op3, String name, int dataType) {
        super(op3, name, dataType);
    }
    Instruction decodeV9AsiLoadStore(int instruction,
                                     SPARCV9RegisterIndirectAddress addr,
                                     SPARCRegister rd,
                                     SPARCV9InstructionFactory factory) {
        return factory.newSwapInstruction(name, addr, rd);
    }
}
