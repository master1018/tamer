class RestoreDecoder extends Format3ADecoder {
    RestoreDecoder() {
        super(RESTORE, "restore", RTLOP_UNKNOWN);
    }
    Instruction decodeFormat3AInstruction(int instruction,
                                           SPARCRegister rs1,
                                           ImmediateOrRegister operand2,
                                           SPARCRegister rd,
                                           SPARCInstructionFactory factory) {
        return factory.newRestoreInstruction(rs1, operand2, rd);
    }
}
