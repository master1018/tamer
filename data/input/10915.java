class UnimpDecoder extends InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        return factory.newUnimpInstruction(instruction & DISP_22_MASK);
    }
}
