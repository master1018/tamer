class V9FlushwDecoder extends InstructionDecoder
               implements V9InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        Instruction instr = null;
        if (isIBitSet(instruction)) {
            instr = v9factory.newIllegalInstruction(instruction);
        } else {
            instr = v9factory.newV9FlushwInstruction();
        }
        return instr;
    }
}
