abstract class Format3ADecoder extends InstructionDecoder
                           implements  RTLOperations {
    final int op3;
    final String name;
    final int rtlOperation;
    Format3ADecoder(int op3, String name, int rtlOperation) {
        this.op3 = op3;
        this.name = name;
        this.rtlOperation = rtlOperation;
    }
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        SPARCRegister rs1 = SPARCRegisters.getRegister(getSourceRegister1(instruction));
        SPARCRegister rd = SPARCRegisters.getRegister(getDestinationRegister(instruction));
        ImmediateOrRegister operand2 = getOperand2(instruction);
        return decodeFormat3AInstruction(instruction, rs1, operand2, rd, factory);
    }
    abstract Instruction decodeFormat3AInstruction(int instruction,
                                       SPARCRegister rs1,
                                       ImmediateOrRegister operand2,
                                       SPARCRegister rd,
                                       SPARCInstructionFactory factory);
}
