public class SPARCV9BranchInstruction extends SPARCBranchInstruction
                    implements SPARCV9Instruction {
    final private boolean predictTaken;
    final private int conditionFlag; 
    public SPARCV9BranchInstruction(String name, PCRelativeAddress addr,
              boolean isAnnuled, int conditionCode, boolean predictTaken, int conditionFlag) {
        super((name += (predictTaken)? ",pt" : ",pn"), addr, isAnnuled, conditionCode);
        this.predictTaken = predictTaken;
        this.conditionFlag = conditionFlag;
    }
    public boolean getPredictTaken() {
        return predictTaken;
    }
    public String getConditionFlagName() {
        return SPARCV9ConditionFlags.getFlagName(conditionFlag);
    }
    public int getConditionFlag() {
        return conditionFlag;
    }
    public String asString(long currentPc, SymbolFinder symFinder) {
        long address = addr.getDisplacement() + currentPc;
        StringBuffer buf = new StringBuffer();
        buf.append(getName());
        buf.append(spaces);
        buf.append(getConditionFlagName());
        buf.append(comma);
        buf.append(symFinder.getSymbolFor(address));
        return buf.toString();
    }
}
