import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class BranchTargetFinder
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor,
             ConstantVisitor
{
    private static final boolean DEBUG = false;
    public boolean isInstruction(int offset)
    {
        return (instructionMarks[offset] & INSTRUCTION) != 0;
    }
    public boolean isTarget(int offset)
    {
        return offset == 0 ||
               (instructionMarks[offset] & (BRANCH_TARGET   |
                                            EXCEPTION_START |
                                            EXCEPTION_END   |
                                            EXCEPTION_HANDLER)) != 0;
    }
    public boolean isBranchOrigin(int offset)
    {
        return (instructionMarks[offset] & BRANCH_ORIGIN) != 0;
    }
    public boolean isBranchTarget(int offset)
    {
        return (instructionMarks[offset] & BRANCH_TARGET) != 0;
    }
    public boolean isAfterBranch(int offset)
    {
        return (instructionMarks[offset] & AFTER_BRANCH) != 0;
    }
    public boolean isExceptionStart(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_START) != 0;
    }
    public boolean isExceptionEnd(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_END) != 0;
    }
    public boolean isExceptionHandler(int offset)
    {
        return (instructionMarks[offset] & EXCEPTION_HANDLER) != 0;
    }
    public boolean isSubroutineInvocation(int offset)
    {
        return (instructionMarks[offset] & SUBROUTINE_INVOCATION) != 0;
    }
    public boolean isSubroutineStart(int offset)
    {
        return subroutineStarts[offset] == offset;
    }
    public boolean isSubroutine(int offset)
    {
        return subroutineStarts[offset] != NONE;
    }
    public boolean isSubroutineReturning(int offset)
    {
        return (instructionMarks[offset] & SUBROUTINE_RETURNING) != 0;
    }
    public int subroutineStart(int offset)
    {
        return subroutineStarts[offset];
    }
    public int subroutineEnd(int offset)
    {
        return subroutineEnds[offset];
    }
    public boolean isNew(int offset)
    {
        return initializationOffsets[offset] != NONE;
    }
    public int initializationOffset(int offset)
    {
        return initializationOffsets[offset];
    }
    public boolean isInitializer()
    {
        return superInitializationOffset != NONE;
    }
    public int superInitializationOffset()
    {
        return superInitializationOffset;
    }
    public boolean isInitializer(int offset)
    {
        return creationOffsets[offset] != NONE;
    }
    public int creationOffset(int offset)
    {
        return creationOffsets[offset];
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;
        if (subroutineStarts.length < codeLength)
        {
            instructionMarks      = new short[codeLength + 1];
            subroutineStarts      = new int[codeLength];
            subroutineEnds        = new int[codeLength];
            creationOffsets       = new int[codeLength];
            initializationOffsets = new int[codeLength];
            for (int index = 0; index < codeLength; index++)
            {
                subroutineStarts[index]      = NONE;
                subroutineEnds[index]        = NONE;
                creationOffsets[index]       = NONE;
                initializationOffsets[index] = NONE;
            }
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                instructionMarks[index]      = 0;
                subroutineStarts[index]      = NONE;
                subroutineEnds[index]        = NONE;
                creationOffsets[index]       = NONE;
                initializationOffsets[index] = NONE;
            }
            instructionMarks[codeLength] = 0;
        }
        superInitializationOffset = NONE;
        currentSubroutineStart = NONE;
        currentSubroutineEnd   = NONE;
        recentCreationOffsetIndex = 0;
        if (method.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            recentCreationOffsets[recentCreationOffsetIndex++] = AT_METHOD_ENTRY;
        }
        instructionMarks[codeLength] = BRANCH_TARGET;
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.exceptionsAccept(clazz, method, this);
        int     subroutineStart     = NONE;
        int     subroutineEnd       = codeLength;
        boolean subroutineReturning = false;
        for (int index = codeLength - 1; index >= 0; index--)
        {
            if (isInstruction(index))
            {
                if (subroutineStarts[index] != NONE)
                {
                    subroutineStart = subroutineStarts[index];
                }
                else if (subroutineStart != NONE)
                {
                    subroutineStarts[index] = subroutineStart;
                }
                if (isSubroutineStart(index))
                {
                    subroutineStart = NONE;
                }
                if (isSubroutine(index))
                {
                    subroutineEnds[index] = subroutineEnd;
                    if (isSubroutineReturning(index))
                    {
                        subroutineReturning = true;
                    }
                    else if (subroutineReturning)
                    {
                        instructionMarks[index] |= SUBROUTINE_RETURNING;
                    }
                }
                else
                {
                    subroutineEnd       = index;
                    subroutineReturning = false;
                }
            }
        }
        if (DEBUG)
        {
            System.out.println();
            System.out.println("Branch targets: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
            for (int index = 0; index < codeLength; index++)
            {
                if (isInstruction(index))
                {
                    System.out.println("" +
                                       (isBranchOrigin(index)         ? 'B' : '-') +
                                       (isAfterBranch(index)          ? 'b' : '-') +
                                       (isBranchTarget(index)         ? 'T' : '-') +
                                       (isExceptionStart(index)       ? 'E' : '-') +
                                       (isExceptionEnd(index)         ? 'e' : '-') +
                                       (isExceptionHandler(index)     ? 'H' : '-') +
                                       (isSubroutineInvocation(index) ? 'J' : '-') +
                                       (isSubroutineStart(index)      ? 'S' : '-') +
                                       (isSubroutineReturning(index)  ? 'r' : '-') +
                                       (isSubroutine(index)           ? " ["+subroutineStart(index)+" -> "+subroutineEnd(index)+"]" : "") +
                                       (isNew(index)                  ? " ["+initializationOffset(index)+"] " : " ---- ") +
                                       InstructionFactory.create(codeAttribute.code, index).toString(index));
                }
            }
        }
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        instructionMarks[offset] |= INSTRUCTION;
        checkSubroutine(offset);
        byte opcode = simpleInstruction.opcode;
        if (opcode == InstructionConstants.OP_IRETURN ||
            opcode == InstructionConstants.OP_LRETURN ||
            opcode == InstructionConstants.OP_FRETURN ||
            opcode == InstructionConstants.OP_DRETURN ||
            opcode == InstructionConstants.OP_ARETURN ||
            opcode == InstructionConstants.OP_ATHROW)
        {
            markBranchOrigin(offset);
            markAfterBranchOrigin(offset + simpleInstruction.length(offset));
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        instructionMarks[offset] |= INSTRUCTION;
        checkSubroutine(offset);
        if (constantInstruction.opcode == InstructionConstants.OP_NEW)
        {
            recentCreationOffsets[recentCreationOffsetIndex++] = offset;
        }
        else
        {
            isInitializer = false;
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
            if (isInitializer)
            {
                int recentCreationOffset = recentCreationOffsets[--recentCreationOffsetIndex];
                creationOffsets[offset] = recentCreationOffset;
                if (recentCreationOffset == AT_METHOD_ENTRY)
                {
                    superInitializationOffset = offset;
                }
                else
                {
                    initializationOffsets[recentCreationOffset] = offset;
                }
            }
        }
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        instructionMarks[offset] |= INSTRUCTION;
        checkSubroutine(offset);
        if (variableInstruction.opcode == InstructionConstants.OP_RET)
        {
            markBranchOrigin(offset);
            instructionMarks[offset] |= SUBROUTINE_RETURNING;
            markAfterBranchOrigin(offset + variableInstruction.length(offset));
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        markBranchOrigin(offset);
        checkSubroutine(offset);
        markBranchTarget(offset, branchInstruction.branchOffset);
        byte opcode = branchInstruction.opcode;
        if (opcode == InstructionConstants.OP_JSR ||
            opcode == InstructionConstants.OP_JSR_W)
        {
            instructionMarks[offset] |= SUBROUTINE_INVOCATION;
            int targetOffset = offset + branchInstruction.branchOffset;
            subroutineStarts[targetOffset] = targetOffset;
        }
        else if (opcode == InstructionConstants.OP_GOTO ||
                 opcode == InstructionConstants.OP_GOTO_W)
        {
            markAfterBranchOrigin(offset + branchInstruction.length(offset));
        }
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        markBranchOrigin(offset);
        checkSubroutine(offset);
        markBranchTarget(offset, switchInstruction.defaultOffset);
        markBranchTargets(offset,
                          switchInstruction.jumpOffsets);
        markAfterBranchOrigin(offset + switchInstruction.length(offset));
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        isInitializer = methodrefConstant.getName(clazz).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        instructionMarks[exceptionInfo.u2startPC]   |= EXCEPTION_START;
        instructionMarks[exceptionInfo.u2endPC]     |= EXCEPTION_END;
        instructionMarks[exceptionInfo.u2handlerPC] |= EXCEPTION_HANDLER;
    }
    private void markBranchTargets(int offset, int[] jumpOffsets)
    {
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            markBranchTarget(offset, jumpOffsets[index]);
        }
    }
    private void markBranchOrigin(int offset)
    {
        instructionMarks[offset] |= INSTRUCTION | BRANCH_ORIGIN;
    }
    private void markBranchTarget(int offset, int jumpOffset)
    {
        int targetOffset = offset + jumpOffset;
        instructionMarks[targetOffset] |= BRANCH_TARGET;
        if (isSubroutine(offset))
        {
            subroutineStarts[targetOffset] = currentSubroutineStart;
            if (currentSubroutineEnd < targetOffset)
            {
                currentSubroutineEnd = targetOffset;
            }
        }
    }
    private void markAfterBranchOrigin(int nextOffset)
    {
        instructionMarks[nextOffset] |= AFTER_BRANCH;
        if (currentSubroutineEnd <= nextOffset)
        {
            currentSubroutineStart = NONE;
        }
    }
    private void checkSubroutine(int offset)
    {
        if (isSubroutine(offset))
        {
            currentSubroutineStart = subroutineStarts[offset];
        }
        else
        {
            subroutineStarts[offset] = currentSubroutineStart;
        }
    }
}
