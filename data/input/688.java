class SaveDecoder extends Format3ADecoder {
    SaveDecoder() {
        super(SAVE, "save", RTLOP_UNKNOWN);
    }
    Instruction decodeFormat3AInstruction(int instruction,
                                       SPARCRegister rs1,
                                       ImmediateOrRegister operand2,
                                       SPARCRegister rd,
                                       SPARCInstructionFactory factory) {
        return factory.newSaveInstruction(rs1, operand2, rd);
    }
}
