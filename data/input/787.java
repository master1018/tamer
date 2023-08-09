class FPArithmeticDecoder extends FloatDecoder {
    private final int rtlOperation;
    FPArithmeticDecoder(int opf, String name, int rtlOperation,
                        int src1Type, int src2Type, int resultType) {
       super(opf, name, src1Type, src2Type, resultType);
       this.rtlOperation = rtlOperation;
    }
    Instruction decodeFloatInstruction(int instruction,
                       SPARCRegister rs1, SPARCRegister rs2,
                       SPARCRegister rd,
                       SPARCInstructionFactory factory) {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(rs1.isFloat() && rs2.isFloat() && rd.isFloat(), "rs1, rs2 and rd must be floats");
        return factory.newFPArithmeticInstruction(name, opf, rtlOperation,
                                                 (SPARCFloatRegister)rs1,
                                                 (SPARCFloatRegister)rs2,
                                                 (SPARCFloatRegister)rd);
    }
}
