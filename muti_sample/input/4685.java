class CallDecoder extends InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        int offset = (instruction & DISP_30_MASK) << 2;
        return factory.newCallInstruction(new PCRelativeAddress(offset));
    }
}
