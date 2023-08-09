class IntegerBranchDecoder extends BranchDecoder {
    String getConditionName(int conditionCode, boolean isAnnuled) {
        return isAnnuled ? integerAnnuledConditionNames[conditionCode]
                         : integerConditionNames[conditionCode];
    }
}
