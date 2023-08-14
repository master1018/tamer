class V9IntRegisterBranchDecoder extends V9RegisterBranchDecoder {
    static final String integerRegisterConditionNames[] = {
        null, "brz", "brlez", "brlz", null, "brnz", "brgz", "brgez"
    };
    String getRegisterConditionName(int index) {
        return integerRegisterConditionNames[index];
    }
}
