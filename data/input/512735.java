import proguard.classfile.Clazz;
import proguard.classfile.attribute.CodeAttribute;
import proguard.evaluation.value.InstructionOffsetValue;
public class BasicBranchUnit
implements   BranchUnit
{
    private boolean                wasCalled;
    private InstructionOffsetValue traceBranchTargets;
    public void resetCalled()
    {
        wasCalled = false;
    }
    protected void setCalled()
    {
        wasCalled = true;
    }
    public boolean wasCalled()
    {
        return wasCalled;
    }
    public void setTraceBranchTargets(InstructionOffsetValue branchTargets)
    {
        this.traceBranchTargets = branchTargets;
    }
    public InstructionOffsetValue getTraceBranchTargets()
    {
        return traceBranchTargets;
    }
    public void branch(Clazz         clazz,
                       CodeAttribute codeAttribute,
                       int           offset,
                       int           branchTarget)
    {
        traceBranchTargets = new InstructionOffsetValue(branchTarget);
        wasCalled = true;
    }
    public void branchConditionally(Clazz         clazz,
                                    CodeAttribute codeAttribute,
                                    int           offset,
                                    int           branchTarget,
                                    int           conditional)
    {
        traceBranchTargets =
            traceBranchTargets.generalize(new InstructionOffsetValue(branchTarget)).instructionOffsetValue();
        wasCalled = true;
    }
    public void returnFromMethod()
    {
        traceBranchTargets = InstructionOffsetValue.EMPTY_VALUE;
        wasCalled = true;
    }
    public void throwException()
    {
        traceBranchTargets = InstructionOffsetValue.EMPTY_VALUE;
        wasCalled = true;
    }
}
