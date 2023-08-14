class TrapDecoder extends InstructionDecoder {
    private static final String trapConditionNames[] = {
        "tn", "te", "tle", "tl", "tleu", "tcs", "tneg", "tvs",
        "ta", "tne", "tg", "tge", "tgu" , "tcc", "tpos", "tvc"
    };
    static String getTrapConditionName(int index) {
        return trapConditionNames[index];
    }
    Instruction decode(int instruction, SPARCInstructionFactory factory) {
        int conditionCode = getConditionCode(instruction);
        return factory.newTrapInstruction(getTrapConditionName(conditionCode),
                                                     conditionCode);
    }
}
