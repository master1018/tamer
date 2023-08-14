public class SPARCTrapInstruction extends SPARCInstruction
    implements BranchInstruction {
    final protected int conditionCode;
    public SPARCTrapInstruction(String name, int conditionCode) {
        super(name);
        this.conditionCode = conditionCode;
    }
    public Address getBranchDestination() {
        return null;
    }
    public int getConditionCode() {
        return conditionCode;
    }
    public boolean isConditional() {
        return conditionCode != CONDITION_TN && conditionCode != CONDITION_TA;
    }
    public boolean isTrap() {
        return true;
    }
}
