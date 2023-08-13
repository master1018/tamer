class FP2RegisterDecoder extends FloatDecoder {
    FP2RegisterDecoder(int opf, String name, int srcType, int resultType) {
        super(opf, name, srcType, resultType);
    }
    Instruction decodeFloatInstruction(int instruction,
                                       SPARCRegister rs1, SPARCRegister rs2,
                                       SPARCRegister rd,
                                       SPARCInstructionFactory factory) {
        if (Assert.ASSERTS_ENABLED)
            Assert.that(rs2.isFloat() && rd.isFloat(), "rs2, rd have to be float registers");
        return factory.newFP2RegisterInstruction(name, opf, (SPARCFloatRegister)rs2, (SPARCFloatRegister)rd);
    }
}
