class SethiDecoder extends InstructionDecoder {
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        Instruction instr = null;
        int rdNum = getDestinationRegister(instruction);
        SPARCRegister rd = SPARCRegisters.getRegister(rdNum);
        int imm22 = (instruction & DISP_22_MASK);
        if (imm22 == 0 && rd == SPARCRegisters.G0) {
            instr = factory.newNoopInstruction();
        } else {
            instr = factory.newSethiInstruction(imm22, rd);
        }
        return instr;
    }
}
