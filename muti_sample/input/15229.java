class ArithmeticDecoder extends Format3ADecoder {
    ArithmeticDecoder(int op3, String name, int rtlOperation) {
        super(op3, name, rtlOperation);
    }
    Instruction decodeFormat3AInstruction(int instruction,
                                       SPARCRegister rs1,
                                       ImmediateOrRegister operand2,
                                       SPARCRegister rd,
                                       SPARCInstructionFactory factory) {
        return factory.newArithmeticInstruction(name, op3, rtlOperation, rs1, operand2, rd);
    }
}
