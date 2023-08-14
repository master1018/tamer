import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.evaluation.value.*;
public class LivenessAnalyzer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor
{
    private static final boolean DEBUG = false;
    public LivenessAnalyzer()
    {
        this(new PartialEvaluator());
    }
    public LivenessAnalyzer(PartialEvaluator partialEvaluator)
    {
        this.partialEvaluator = partialEvaluator;
    }
    public boolean isAliveBefore(int instructionOffset, int variableIndex)
    {
        return variableIndex >= MAX_VARIABLES_SIZE ||
               (isAliveBefore[instructionOffset] & (1L << variableIndex)) != 0;
    }
    public void setAliveBefore(int instructionOffset, int variableIndex, boolean alive)
    {
        if (variableIndex < MAX_VARIABLES_SIZE)
        {
            if (alive)
            {
                isAliveBefore[instructionOffset] |= 1L << variableIndex;
            }
            else
            {
                isAliveBefore[instructionOffset] &= ~(1L << variableIndex);
            }
        }
    }
    public boolean isAliveAfter(int instructionOffset, int variableIndex)
    {
        return variableIndex >= MAX_VARIABLES_SIZE ||
               (isAliveAfter[instructionOffset] & (1L << variableIndex)) != 0;
    }
    public void setAliveAfter(int instructionOffset, int variableIndex, boolean alive)
    {
        if (variableIndex < MAX_VARIABLES_SIZE)
        {
            if (alive)
            {
                isAliveAfter[instructionOffset] |= 1L << variableIndex;
            }
            else
            {
                isAliveAfter[instructionOffset] &= ~(1L << variableIndex);
            }
        }
    }
    public boolean isCategory2(int instructionOffset, int variableIndex)
    {
        return variableIndex < MAX_VARIABLES_SIZE &&
               (isCategory2[instructionOffset] & (1L << variableIndex)) != 0;
    }
    public void setCategory2(int instructionOffset, int variableIndex, boolean category2)
    {
        if (variableIndex < MAX_VARIABLES_SIZE)
        {
            if (category2)
            {
                isCategory2[instructionOffset] |= 1L << variableIndex;
            }
            else
            {
                isCategory2[instructionOffset] &= ~(1L << variableIndex);
            }
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println();
            System.out.println("Liveness analysis: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
        }
        initializeArrays(codeAttribute);
        partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
        int codeLength    = codeAttribute.u4codeLength;
        int variablesSize = codeAttribute.u2maxLocals;
        if (variablesSize > MAX_VARIABLES_SIZE)
        {
            variablesSize = MAX_VARIABLES_SIZE;
        }
        do
        {
            checkAgain = false;
            alive      = 0L;
            for (int offset = codeLength - 1; offset >= 0; offset--)
            {
                if (partialEvaluator.isTraced(offset))
                {
                    InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);
                    if (branchTargets != null)
                    {
                        alive = combinedLiveness(branchTargets);
                    }
                    alive |= isAliveAfter[offset];
                    isAliveAfter[offset] = alive;
                    codeAttribute.instructionAccept(clazz, method, offset, this);
                    alive |= isAliveBefore[offset];
                    if ((~isAliveBefore[offset] & alive) != 0L)
                    {
                        isAliveBefore[offset] = alive;
                        checkAgain |= offset < maxOffset(partialEvaluator.branchOrigins(offset));
                    }
                }
            }
            codeAttribute.exceptionsAccept(clazz, method, this);
        }
        while (checkAgain);
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset))
            {
                for (int variableIndex = 0; variableIndex < variablesSize; variableIndex++)
                {
                    if (isAliveBefore(offset, variableIndex))
                    {
                        Value value = partialEvaluator.getVariablesBefore(offset).getValue(variableIndex);
                        if (value != null && value.isCategory2())
                        {
                            setCategory2(offset, variableIndex, true);
                            setAliveBefore(offset, variableIndex + 1, true);
                            setCategory2(  offset, variableIndex + 1, true);
                        }
                    }
                    if (isAliveAfter(offset, variableIndex))
                    {
                        Value value = partialEvaluator.getVariablesAfter(offset).getValue(variableIndex);
                        if (value != null && value.isCategory2())
                        {
                            setCategory2(offset, variableIndex, true);
                            setAliveAfter(offset, variableIndex + 1, true);
                            setCategory2( offset, variableIndex + 1, true);
                        }
                    }
                }
            }
        }
        if (DEBUG)
        {
            for (int offset = 0; offset < codeLength; offset++)
            {
                if (partialEvaluator.isTraced(offset))
                {
                    long aliveBefore = isAliveBefore[offset];
                    long aliveAfter  = isAliveAfter[offset];
                    long category2   = isCategory2[offset];
                    for (int variableIndex = 0; variableIndex < variablesSize; variableIndex++)
                    {
                        long variableMask = (1L << variableIndex);
                        System.out.print((aliveBefore & variableMask) == 0L ? '.' :
                                         (category2   & variableMask) == 0L ? 'x' :
                                                                              '*');
                    }
                    System.out.println(" "+ InstructionFactory.create(codeAttribute.code, offset).toString(offset));
                    for (int variableIndex = 0; variableIndex < variablesSize; variableIndex++)
                    {
                        long variableMask = (1L << variableIndex);
                        System.out.print((aliveAfter & variableMask) == 0L ? '.' :
                                         (category2  & variableMask) == 0L ? 'x' :
                                                                             '=');
                    }
                    System.out.println();
                }
            }
        }
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        int variableIndex = variableInstruction.variableIndex;
        if (variableIndex < MAX_VARIABLES_SIZE)
        {
            long livenessMask = 1L << variableIndex;
            if (variableInstruction.isLoad())
            {
                alive |= livenessMask;
            }
            else
            {
                alive &= ~livenessMask;
                isAliveAfter[offset] |= livenessMask;
            }
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
         if (offset == partialEvaluator.superInitializationOffset())
        {
            alive |= 1L;
        }
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        long alive = isAliveBefore[exceptionInfo.u2handlerPC];
        if (alive != 0L)
        {
            int startOffset = exceptionInfo.u2startPC;
            int endOffset   = exceptionInfo.u2endPC;
            for (int offset = startOffset; offset < endOffset; offset++)
            {
                if (partialEvaluator.isTraced(offset))
                {
                    if ((~(isAliveBefore[offset] & isAliveAfter[offset]) & alive) != 0L)
                    {
                        isAliveBefore[offset] |= alive;
                        isAliveAfter[offset]  |= alive;
                        checkAgain = true;
                    }
                }
            }
        }
    }
    private void initializeArrays(CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;
        if (isAliveBefore.length < codeLength)
        {
            isAliveBefore = new long[codeLength];
            isAliveAfter  = new long[codeLength];
            isCategory2   = new long[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                isAliveBefore[index] = 0L;
                isAliveAfter[index]  = 0L;
                isCategory2[index]   = 0L;
            }
        }
    }
    private long combinedLiveness(InstructionOffsetValue instructionOffsetValue)
    {
        long alive = 0L;
        int count = instructionOffsetValue.instructionOffsetCount();
        for (int index = 0; index < count; index++)
        {
            alive |= isAliveBefore[instructionOffsetValue.instructionOffset(index)];
        }
        return alive;
    }
    private int minOffset(Value instructionOffsets)
    {
        return minOffset(instructionOffsets, Integer.MAX_VALUE);
    }
    private int minOffset(Value instructionOffsets, int minOffset)
    {
        if (instructionOffsets != null)
        {
            InstructionOffsetValue instructionOffsetValue =
                instructionOffsets.instructionOffsetValue();
            int count = instructionOffsetValue.instructionOffsetCount();
            for (int index = 0; index < count; index++)
            {
                int offset = instructionOffsetValue.instructionOffset(index);
                if (minOffset > offset)
                {
                    minOffset = offset;
                }
            }
        }
        return minOffset;
    }
    private int maxOffset(Value instructionOffsets)
    {
        return maxOffset(instructionOffsets, Integer.MIN_VALUE);
    }
    private int maxOffset(Value instructionOffsets, int maxOffset)
    {
        if (instructionOffsets != null)
        {
            InstructionOffsetValue instructionOffsetValue =
                instructionOffsets.instructionOffsetValue();
            int count = instructionOffsetValue.instructionOffsetCount();
            for (int index = 0; index < count; index++)
            {
                int offset = instructionOffsetValue.instructionOffset(index);
                if (maxOffset < offset)
                {
                    maxOffset = offset;
                }
            }
        }
        return maxOffset;
    }
}
