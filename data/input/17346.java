class FloatBranchDecoder extends BranchDecoder {
    String getConditionName(int conditionCode, boolean isAnnuled) {
        return isAnnuled ? floatAnnuledConditionNames[conditionCode]
                         : floatConditionNames[conditionCode];
    }
}
