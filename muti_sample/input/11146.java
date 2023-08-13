class V9IntegerBranchDecoder extends V9CCBranchDecoder {
    String getConditionName(int conditionCode, boolean isAnnuled) {
        return isAnnuled ? integerAnnuledConditionNames[conditionCode]
                         : integerConditionNames[conditionCode];
    }
    int getConditionFlag(int instruction) {
        return ((BPcc_CC_MASK & instruction) >>> BPcc_CC_START_BIT) + icc;
    }
}
