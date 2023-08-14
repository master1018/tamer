class V9DoneRetryDecoder extends InstructionDecoder
              implements V9InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        Instruction instr = null;
        int rdNum = getDestinationRegister(instruction);
        switch (rdNum) {
            case 0:
                instr = v9factory.newV9DoneInstruction();
                break;
            case 1:
                instr = v9factory.newV9RetryInstruction();
                break;
            default:
                instr = v9factory.newIllegalInstruction(instruction);
                break;
        }
        return instr;
    }
}
