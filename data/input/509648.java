package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.preverification.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class CodeAttributeComposer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor,
             StackMapFrameVisitor,
             VerificationTypeVisitor,
             LineNumberInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor
{
    private static final boolean DEBUG = false;
    public CodeAttributeComposer()
    {
        this(false);
    }
    public CodeAttributeComposer(boolean allowExternalExceptionHandlers)
    {
        this.allowExternalExceptionHandlers = allowExternalExceptionHandlers;
    }
    public void reset()
    {
        maximumCodeLength    = 0;
        codeLength           = 0;
        exceptionTableLength = 0;
        level                = -1;
    }
    public void beginCodeFragment(int maximumCodeFragmentLength)
    {
        level++;
        if (level >= MAXIMUM_LEVELS)
        {
            throw new IllegalArgumentException("Maximum number of code fragment levels exceeded ["+level+"]");
        }
        maximumCodeLength += maximumCodeFragmentLength;
        ensureCodeLength(maximumCodeLength);
        if (instructionOffsetMap[level].length <= maximumCodeFragmentLength)
        {
            instructionOffsetMap[level] = new int[maximumCodeFragmentLength + 1];
        }
        for (int index = 0; index <= maximumCodeFragmentLength; index++)
        {
            instructionOffsetMap[level][index] = INVALID;
        }
        codeFragmentOffsets[level] = codeLength;
        codeFragmentLengths[level] = maximumCodeFragmentLength;
    }
    public void appendInstruction(int         oldInstructionOffset,
                                  Instruction instruction)
    {
        if (DEBUG)
        {
            println("["+codeLength+"] <- ", instruction.toString(oldInstructionOffset));
        }
        int newCodeLength = codeLength + instruction.length(codeLength);
        ensureCodeLength(newCodeLength);
        oldInstructionOffsets[codeLength] = oldInstructionOffset;
        instruction.write(code, codeLength);
        instructionOffsetMap[level][oldInstructionOffset] = codeLength;
        codeLength = newCodeLength;
    }
    public void appendLabel(int oldInstructionOffset)
    {
        if (DEBUG)
        {
            println("["+codeLength+"] <- ", "[" + oldInstructionOffset + "] (label)");
        }
        instructionOffsetMap[level][oldInstructionOffset] = codeLength;
    }
    public void appendException(ExceptionInfo exceptionInfo)
    {
        if (DEBUG)
        {
            print("         ", "Exception ["+exceptionInfo.u2startPC+" -> "+exceptionInfo.u2endPC+": "+exceptionInfo.u2handlerPC+"]");
        }
        visitExceptionInfo(null, null, null, exceptionInfo);
        if (DEBUG)
        {
            System.out.println(" -> ["+exceptionInfo.u2startPC+" -> "+exceptionInfo.u2endPC+": "+exceptionInfo.u2handlerPC+"]");
        }
        if (exceptionInfo.u2startPC == exceptionInfo.u2endPC)
        {
            if (DEBUG)
            {
                println("         ", "  (not added because of empty instruction range)");
            }
            return;
        }
        if (exceptionTable.length <= exceptionTableLength)
        {
            ExceptionInfo[] newExceptionTable = new ExceptionInfo[exceptionTableLength+1];
            System.arraycopy(exceptionTable, 0, newExceptionTable, 0, exceptionTableLength);
            exceptionTable = newExceptionTable;
        }
        exceptionTable[exceptionTableLength++] = exceptionInfo;
    }
    public void endCodeFragment()
    {
        if (level < 0)
        {
            throw new IllegalArgumentException("Code fragment not begun ["+level+"]");
        }
        int instructionOffset = codeFragmentOffsets[level];
        while (instructionOffset < codeLength)
        {
            Instruction instruction = InstructionFactory.create(code, instructionOffset);
            if (oldInstructionOffsets[instructionOffset] >= 0)
            {
                instruction.accept(null, null, null, instructionOffset, this);
                instruction.write(code, instructionOffset);
                oldInstructionOffsets[instructionOffset] = -1;
            }
            instructionOffset += instruction.length(instructionOffset);
        }
        maximumCodeLength += codeLength - codeFragmentOffsets[level] -
                             codeFragmentLengths[level];
        if (allowExternalExceptionHandlers)
        {
            for (int index = 0; index < exceptionTableLength; index++)
            {
                ExceptionInfo exceptionInfo = exceptionTable[index];
                int handlerPC = -exceptionInfo.u2handlerPC;
                if (handlerPC > 0)
                {
                    if (remappableInstructionOffset(handlerPC))
                    {
                        exceptionInfo.u2handlerPC = remapInstructionOffset(handlerPC);
                    }
                    else if (level == 0)
                    {
                        throw new IllegalStateException("Couldn't remap exception handler offset ["+handlerPC+"]");
                    }
                }
            }
        }
        level--;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println("CodeAttributeComposer: putting results in ["+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz)+"]");
        }
        if (level != -1)
        {
            throw new IllegalArgumentException("Code fragment not ended ["+level+"]");
        }
        level++;
        if (codeAttribute.u4codeLength < codeLength)
        {
            codeAttribute.code = new byte[codeLength];
        }
        codeAttribute.u4codeLength = codeLength;
        System.arraycopy(code, 0, codeAttribute.code, 0, codeLength);
        if (codeAttribute.exceptionTable.length < exceptionTableLength)
        {
            codeAttribute.exceptionTable = new ExceptionInfo[exceptionTableLength];
        }
        codeAttribute.u2exceptionTableLength = exceptionTableLength;
        System.arraycopy(exceptionTable, 0, codeAttribute.exceptionTable, 0, exceptionTableLength);
        stackSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        variableSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.attributesAccept(clazz, method, this);
        level--;
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        expectedStackMapFrameOffset = -1;
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        expectedStackMapFrameOffset = 0;
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        lineNumberTableAttribute.lineNumbersAccept(clazz, method, codeAttribute, this);
        lineNumberTableAttribute.u2lineNumberTableLength =
           removeEmptyLineNumbers(lineNumberTableAttribute.lineNumberTable,
                                  lineNumberTableAttribute.u2lineNumberTableLength,
                                  codeAttribute.u4codeLength);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        localVariableTableAttribute.u2localVariableTableLength =
            removeEmptyLocalVariables(localVariableTableAttribute.localVariableTable,
                                      localVariableTableAttribute.u2localVariableTableLength,
                                      codeAttribute.u2maxLocals);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        localVariableTypeTableAttribute.u2localVariableTypeTableLength =
            removeEmptyLocalVariableTypes(localVariableTypeTableAttribute.localVariableTypeTable,
                                          localVariableTypeTableAttribute.u2localVariableTypeTableLength,
                                          codeAttribute.u2maxLocals);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        branchInstruction.branchOffset = remapBranchOffset(offset,
                                                           branchInstruction.branchOffset);
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        switchInstruction.defaultOffset = remapBranchOffset(offset,
                                                            switchInstruction.defaultOffset);
        remapJumpOffsets(offset,
                         switchInstruction.jumpOffsets);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        exceptionInfo.u2startPC = remapInstructionOffset(exceptionInfo.u2startPC);
        exceptionInfo.u2endPC   = remapInstructionOffset(exceptionInfo.u2endPC);
        int handlerPC = exceptionInfo.u2handlerPC;
        exceptionInfo.u2handlerPC =
            !allowExternalExceptionHandlers ||
            remappableInstructionOffset(handlerPC) ?
                remapInstructionOffset(handlerPC) :
                -handlerPC;
    }
    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame)
    {
        int stackMapFrameOffset = remapInstructionOffset(offset);
        int offsetDelta = stackMapFrameOffset;
        if (expectedStackMapFrameOffset >= 0)
        {
            offsetDelta -= expectedStackMapFrameOffset;
            expectedStackMapFrameOffset = stackMapFrameOffset + 1;
        }
        stackMapFrame.u2offsetDelta = offsetDelta;
    }
    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, sameOneFrame);
        sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, moreZeroFrame);
        moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        visitAnyStackMapFrame(clazz, method, codeAttribute, offset, fullFrame);
        fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
        fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType) {}
    public void visitUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType uninitializedType)
    {
        uninitializedType.u2newInstructionOffset = remapInstructionOffset(uninitializedType.u2newInstructionOffset);
    }
    public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo)
    {
        lineNumberInfo.u2startPC = remapInstructionOffset(lineNumberInfo.u2startPC);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        int startPC = remapInstructionOffset(localVariableInfo.u2startPC);
        int endPC   = remapInstructionOffset(localVariableInfo.u2startPC + localVariableInfo.u2length);
        localVariableInfo.u2startPC = startPC;
        localVariableInfo.u2length  = endPC - startPC;
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        int startPC = remapInstructionOffset(localVariableTypeInfo.u2startPC);
        int endPC   = remapInstructionOffset(localVariableTypeInfo.u2startPC + localVariableTypeInfo.u2length);
        localVariableTypeInfo.u2startPC = startPC;
        localVariableTypeInfo.u2length  = endPC - startPC;
    }
    private void ensureCodeLength(int newCodeLength)
    {
        if (code.length < newCodeLength)
        {
            newCodeLength = newCodeLength * 6 / 5;
            byte[] newCode = new byte[newCodeLength];
            System.arraycopy(code, 0, newCode, 0, codeLength);
            code = newCode;
            int[] newOldInstructionOffsets = new int[newCodeLength];
            System.arraycopy(oldInstructionOffsets, 0, newOldInstructionOffsets, 0, codeLength);
            oldInstructionOffsets = newOldInstructionOffsets;
        }
    }
    private void remapJumpOffsets(int offset, int[] jumpOffsets)
    {
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            jumpOffsets[index] = remapBranchOffset(offset, jumpOffsets[index]);
        }
    }
    private int remapBranchOffset(int newInstructionOffset, int branchOffset)
    {
        if (newInstructionOffset < 0 ||
            newInstructionOffset > codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+newInstructionOffset +"] in code with length ["+codeLength+"]");
        }
        int oldInstructionOffset = oldInstructionOffsets[newInstructionOffset];
        return remapInstructionOffset(oldInstructionOffset + branchOffset) -
               remapInstructionOffset(oldInstructionOffset);
    }
    private int remapInstructionOffset(int oldInstructionOffset)
    {
        if (oldInstructionOffset < 0 ||
            oldInstructionOffset > codeFragmentLengths[level])
        {
            throw new IllegalArgumentException("Instruction offset ["+oldInstructionOffset +"] out of range in code fragment with length ["+codeFragmentLengths[level]+"] at level "+level);
        }
        int newInstructionOffset = instructionOffsetMap[level][oldInstructionOffset];
        if (newInstructionOffset == INVALID)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+oldInstructionOffset +"] in code fragment at level "+level);
        }
        return newInstructionOffset;
    }
    private boolean remappableInstructionOffset(int oldInstructionOffset)
    {
        return
            oldInstructionOffset <= codeFragmentLengths[level] &&
            instructionOffsetMap[level][oldInstructionOffset] > INVALID;
    }
    private int removeEmptyExceptions(ExceptionInfo[] exceptionInfos,
                                      int             exceptionInfoCount)
    {
        int newIndex = 0;
        for (int index = 0; index < exceptionInfoCount; index++)
        {
            ExceptionInfo exceptionInfo = exceptionInfos[index];
            if (exceptionInfo.u2startPC < exceptionInfo.u2endPC)
            {
                exceptionInfos[newIndex++] = exceptionInfo;
            }
        }
        for (int index = newIndex; index < exceptionInfoCount; index++)
        {
            exceptionInfos[index] = null;
        }
        return newIndex;
    }
    private int removeEmptyLineNumbers(LineNumberInfo[] lineNumberInfos,
                                       int              lineNumberInfoCount,
                                       int              codeLength)
    {
        int newIndex = 0;
        for (int index = 0; index < lineNumberInfoCount; index++)
        {
            LineNumberInfo lineNumberInfo = lineNumberInfos[index];
            int startPC = lineNumberInfo.u2startPC;
            if (startPC < codeLength &&
                (index == 0 || startPC > lineNumberInfos[index-1].u2startPC))
            {
                lineNumberInfos[newIndex++] = lineNumberInfo;
            }
        }
        for (int index = newIndex; index < lineNumberInfoCount; index++)
        {
            lineNumberInfos[index] = null;
        }
        return newIndex;
    }
    private int removeEmptyLocalVariables(LocalVariableInfo[] localVariableInfos,
                                          int                 localVariableInfoCount,
                                          int                 maxLocals)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableInfoCount; index++)
        {
            LocalVariableInfo localVariableInfo = localVariableInfos[index];
            if (localVariableInfo.u2length > 0 &&
                localVariableInfo.u2index < maxLocals)
            {
                localVariableInfos[newIndex++] = localVariableInfo;
            }
        }
        for (int index = newIndex; index < localVariableInfoCount; index++)
        {
            localVariableInfos[index] = null;
        }
        return newIndex;
    }
    private int removeEmptyLocalVariableTypes(LocalVariableTypeInfo[] localVariableTypeInfos,
                                              int                     localVariableTypeInfoCount,
                                              int                     maxLocals)
    {
        int newIndex = 0;
        for (int index = 0; index < localVariableTypeInfoCount; index++)
        {
            LocalVariableTypeInfo localVariableTypeInfo = localVariableTypeInfos[index];
            if (localVariableTypeInfo.u2length > 0 &&
                localVariableTypeInfo.u2index < maxLocals)
            {
                localVariableTypeInfos[newIndex++] = localVariableTypeInfo;
            }
        }
        for (int index = newIndex; index < localVariableTypeInfoCount; index++)
        {
            localVariableTypeInfos[index] = null;
        }
        return newIndex;
    }
    private void println(String string1, String string2)
    {
        print(string1, string2);
        System.out.println();
    }
    private void print(String string1, String string2)
    {
        System.out.print(string1);
        for (int index = 0; index < level; index++)
        {
            System.out.print("  ");
        }
        System.out.print(string2);
    }
    public static void main(String[] args)
    {
        CodeAttributeComposer composer = new CodeAttributeComposer();
        composer.beginCodeFragment(4);
        composer.appendInstruction(0, new SimpleInstruction(InstructionConstants.OP_ICONST_0));
        composer.appendInstruction(1, new VariableInstruction(InstructionConstants.OP_ISTORE, 0));
        composer.appendInstruction(2, new BranchInstruction(InstructionConstants.OP_GOTO, 1));
        composer.beginCodeFragment(4);
        composer.appendInstruction(0, new VariableInstruction(InstructionConstants.OP_IINC, 0, 1));
        composer.appendInstruction(1, new VariableInstruction(InstructionConstants.OP_ILOAD, 0));
        composer.appendInstruction(2, new SimpleInstruction(InstructionConstants.OP_ICONST_5));
        composer.appendInstruction(3, new BranchInstruction(InstructionConstants.OP_IFICMPLT, -3));
        composer.endCodeFragment();
        composer.appendInstruction(3, new SimpleInstruction(InstructionConstants.OP_RETURN));
        composer.endCodeFragment();
    }
}
