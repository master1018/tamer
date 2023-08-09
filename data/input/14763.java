class V9FloatBranchDecoder extends V9CCBranchDecoder {
    String getConditionName(int conditionCode, boolean isAnnuled) {
        return isAnnuled ? floatAnnuledConditionNames[conditionCode]
                         : floatConditionNames[conditionCode];
    }
    int getConditionFlag(int instruction) {
        return (FBPfcc_CC_MASK & instruction) >>> FBPfcc_CC_START_BIT;
    }
}
