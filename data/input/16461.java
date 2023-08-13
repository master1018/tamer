class IllegalInstructionDecoder extends InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        return factory.newIllegalInstruction(instruction);
    }
}
