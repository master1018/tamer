abstract class V9CCBranchDecoder extends V9BranchDecoder {
    abstract int getConditionFlag(int instruction);
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        SPARCV9InstructionFactory v9factory = (SPARCV9InstructionFactory) factory;
        int conditionFlag = getConditionFlag(instruction);
        boolean predictTaken = getPredictTaken(instruction);
        int conditionCode = getConditionCode(instruction);
        boolean annuled = getAnnuledBit(instruction);
        String name = getConditionName(conditionCode, annuled);
        PCRelativeAddress addr = new PCRelativeAddress(extractSignedIntFromNBits(instruction, 19) << 2);
        return v9factory.newV9BranchInstruction(name, addr, annuled, conditionCode,
                                              predictTaken, conditionFlag);
    }
}
