abstract class FPopDecoder extends InstructionDecoder {
    abstract InstructionDecoder getOpfDecoder(int opf);
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        int opf = getOpf(instruction);
        InstructionDecoder decoder = getOpfDecoder(opf);
        return (decoder == null) ? factory.newIllegalInstruction(instruction)
                                 : decoder.decode(instruction, factory);
    }
}
