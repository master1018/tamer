package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.preverification.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassPrinter;
public class CodeAttributeEditor
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
    public Instruction[]    preInsertions  = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    public Instruction[]    replacements   = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    public Instruction[]    postInsertions = new Instruction[ClassConstants.TYPICAL_CODE_LENGTH];
    public boolean[]        deleted        = new boolean[ClassConstants.TYPICAL_CODE_LENGTH];
    private int[]   instructionOffsetMap = new int[ClassConstants.TYPICAL_CODE_LENGTH];
    private int     newOffset;
    private boolean lengthIncreased;
    private int expectedStackMapFrameOffset;
    private final StackSizeUpdater    stackSizeUpdater    = new StackSizeUpdater();
    private final VariableSizeUpdater variableSizeUpdater = new VariableSizeUpdater();
    private final InstructionWriter   instructionWriter   = new InstructionWriter();
    public CodeAttributeEditor()
    {
        this(true);
    }
    public CodeAttributeEditor(boolean updateFrameSizes)
    {
        this.updateFrameSizes = updateFrameSizes;
    }
    public void reset(int codeLength)
    {
        this.codeLength = codeLength;
        if (preInsertions.length < codeLength)
        {
            preInsertions  = new Instruction[codeLength];
            replacements   = new Instruction[codeLength];
            postInsertions = new Instruction[codeLength];
            deleted        = new boolean[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                preInsertions[index]  = null;
                replacements[index]   = null;
                postInsertions[index] = null;
                deleted[index]        = false;
            }
        }
        modified = false;
        simple   = true;
    }
    public void insertBeforeInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        preInsertions[instructionOffset] = instruction;
        modified = true;
        simple   = false;
    }
    public void insertBeforeInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        preInsertions[instructionOffset] = new CompositeInstruction(instructions);
        modified = true;
        simple   = false;
    }
    public void replaceInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        replacements[instructionOffset] = instruction;
        modified = true;
    }
    public void replaceInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        replacements[instructionOffset] = new CompositeInstruction(instructions);
        modified = true;
    }
    public void insertAfterInstruction(int instructionOffset, Instruction instruction)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        postInsertions[instructionOffset] = instruction;
        modified = true;
        simple   = false;
    }
    public void insertAfterInstruction(int instructionOffset, Instruction[] instructions)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        postInsertions[instructionOffset] = new CompositeInstruction(instructions);
        modified = true;
        simple   = false;
    }
    public void deleteInstruction(int instructionOffset)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        deleted[instructionOffset] = true;
        modified = true;
        simple   = false;
    }
    public void undeleteInstruction(int instructionOffset)
    {
        if (instructionOffset < 0 ||
            instructionOffset >= codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+instructionOffset+"] in code with length ["+codeLength+"]");
        }
        deleted[instructionOffset] = false;
    }
    public boolean isModified(int instructionOffset)
    {
        return preInsertions[instructionOffset]  != null ||
               replacements[instructionOffset]   != null ||
               postInsertions[instructionOffset] != null ||
               deleted[instructionOffset];
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        try
        {
            visitCodeAttribute0(clazz, method, codeAttribute);
        }
        catch (RuntimeException ex)
        {
            System.err.println("Unexpected error while editing code:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            throw ex;
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println("CodeAttributeEditor: ["+clazz.getName()+"."+method.getName(clazz)+"]");
        }
        if (!modified)
        {
            return;
        }
        if (canPerformSimpleReplacements(codeAttribute))
        {
            performSimpleReplacements(codeAttribute);
            updateFrameSizes(clazz, method, codeAttribute);
        }
        else
        {
            codeAttribute.u4codeLength =
                updateInstructions(clazz, method, codeAttribute);
            codeAttribute.exceptionsAccept(clazz, method, this);
            codeAttribute.u2exceptionTableLength =
                removeEmptyExceptions(codeAttribute.exceptionTable,
                                      codeAttribute.u2exceptionTableLength);
            updateFrameSizes(clazz, method, codeAttribute);
            codeAttribute.attributesAccept(clazz, method, this);
            instructionWriter.visitCodeAttribute(clazz, method, codeAttribute);
        }
    }
    private void updateFrameSizes(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (updateFrameSizes)
        {
            stackSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
            variableSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        }
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
    private boolean canPerformSimpleReplacements(CodeAttribute codeAttribute)
    {
        if (!simple)
        {
            return false;
        }
        byte[] code       = codeAttribute.code;
        int    codeLength = codeAttribute.u4codeLength;
        for (int offset = 0; offset < codeLength; offset++)
        {
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null &&
                replacementInstruction.length(offset) !=
                    InstructionFactory.create(code, offset).length(offset))
            {
                return false;
            }
        }
        return true;
    }
    private void performSimpleReplacements(CodeAttribute codeAttribute)
    {
        int codeLength = codeAttribute.u4codeLength;
        for (int offset = 0; offset < codeLength; offset++)
        {
            Instruction replacementInstruction = replacements[offset];
            if (replacementInstruction != null)
            {
                replacementInstruction.write(codeAttribute, offset);
                if (DEBUG)
                {
                    System.out.println("  Replaced "+replacementInstruction.toString(newOffset));
                }
            }
        }
    }
    private int updateInstructions(Clazz         clazz,
                                   Method        method,
                                   CodeAttribute codeAttribute)
    {
        byte[] oldCode   = codeAttribute.code;
        int    oldLength = codeAttribute.u4codeLength;
        if (instructionOffsetMap.length < oldLength + 1)
        {
            instructionOffsetMap = new int[oldLength + 1];
        }
        int newLength = mapInstructions(oldCode,
                                        oldLength);
        if (lengthIncreased)
        {
            codeAttribute.code = new byte[newLength];
        }
        instructionWriter.reset(newLength);
        moveInstructions(clazz,
                         method,
                         codeAttribute,
                         oldCode,
                         oldLength);
        return newLength;
    }
    private int mapInstructions(byte[] oldCode, int oldLength)
    {
        newOffset       = 0;
        lengthIncreased = false;
        int oldOffset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);
            mapInstruction(oldOffset, instruction);
            oldOffset += instruction.length(oldOffset);
            if (newOffset > oldOffset)
            {
                lengthIncreased = true;
            }
        }
        while (oldOffset < oldLength);
        instructionOffsetMap[oldOffset] = newOffset;
        return newOffset;
    }
    private void mapInstruction(int         oldOffset,
                                Instruction instruction)
    {
        instructionOffsetMap[oldOffset] = newOffset;
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            newOffset += preInstruction.length(newOffset);
        }
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            newOffset += replacementInstruction.length(newOffset);
        }
        else if (!deleted[oldOffset])
        {
            newOffset += instruction.length(newOffset);
        }
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            newOffset += postInstruction.length(newOffset);
        }
    }
    private void moveInstructions(Clazz         clazz,
                                  Method        method,
                                  CodeAttribute codeAttribute,
                                  byte[]        oldCode,
                                  int           oldLength)
    {
        newOffset = 0;
        int oldOffset = 0;
        do
        {
            Instruction instruction = InstructionFactory.create(oldCode, oldOffset);
            moveInstruction(clazz,
                            method,
                            codeAttribute,
                            oldOffset,
                            instruction);
            oldOffset += instruction.length(oldOffset);
        }
        while (oldOffset < oldLength);
    }
    private void moveInstruction(Clazz         clazz,
                                 Method        method,
                                 CodeAttribute codeAttribute,
                                 int           oldOffset,
                                 Instruction   instruction)
    {
        Instruction preInstruction = preInsertions[oldOffset];
        if (preInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Pre-inserted  "+preInstruction.toString(newOffset));
            }
            preInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
        Instruction replacementInstruction = replacements[oldOffset];
        if (replacementInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Replaced      "+replacementInstruction.toString(newOffset));
            }
            replacementInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
        else if (!deleted[oldOffset])
        {
            if (DEBUG)
            {
                System.out.println("  Copied        "+instruction.toString(newOffset));
            }
            instruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
        Instruction postInstruction = postInsertions[oldOffset];
        if (postInstruction != null)
        {
            if (DEBUG)
            {
                System.out.println("  Post-inserted "+postInstruction.toString(newOffset));
            }
            postInstruction.accept(clazz, method, codeAttribute, oldOffset, this);
        }
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        instructionWriter.visitSimpleInstruction(clazz,
                                                 method,
                                                 codeAttribute,
                                                 newOffset,
                                                 simpleInstruction);
        newOffset += simpleInstruction.length(newOffset);
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        instructionWriter.visitConstantInstruction(clazz,
                                                   method,
                                                   codeAttribute,
                                                   newOffset,
                                                   constantInstruction);
        newOffset += constantInstruction.length(newOffset);
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        instructionWriter.visitVariableInstruction(clazz,
                                                   method,
                                                   codeAttribute,
                                                   newOffset,
                                                   variableInstruction);
        newOffset += variableInstruction.length(newOffset);
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        branchInstruction.branchOffset = remapBranchOffset(offset,
                                                           branchInstruction.branchOffset);
        instructionWriter.visitBranchInstruction(clazz,
                                                 method,
                                                 codeAttribute,
                                                 newOffset,
                                                 branchInstruction);
        newOffset += branchInstruction.length(newOffset);
    }
    public void visitTableSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        tableSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                 tableSwitchInstruction.defaultOffset);
        remapJumpOffsets(offset,
                         tableSwitchInstruction.jumpOffsets);
        instructionWriter.visitTableSwitchInstruction(clazz,
                                                      method,
                                                      codeAttribute,
                                                      newOffset,
                                                      tableSwitchInstruction);
        newOffset += tableSwitchInstruction.length(newOffset);
    }
    public void visitLookUpSwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        lookUpSwitchInstruction.defaultOffset = remapBranchOffset(offset,
                                                                  lookUpSwitchInstruction.defaultOffset);
        remapJumpOffsets(offset,
                         lookUpSwitchInstruction.jumpOffsets);
        instructionWriter.visitLookUpSwitchInstruction(clazz,
                                                       method,
                                                       codeAttribute,
                                                       newOffset,
                                                       lookUpSwitchInstruction);
        newOffset += lookUpSwitchInstruction.length(newOffset);
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        exceptionInfo.u2startPC   = remapInstructionOffset(exceptionInfo.u2startPC);
        exceptionInfo.u2endPC     = remapInstructionOffset(exceptionInfo.u2endPC);
        exceptionInfo.u2handlerPC = remapInstructionOffset(exceptionInfo.u2handlerPC);
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
        localVariableInfo.u2length  = remapBranchOffset(localVariableInfo.u2startPC,
                                                        localVariableInfo.u2length);
        localVariableInfo.u2startPC = remapInstructionOffset(localVariableInfo.u2startPC);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2length  = remapBranchOffset(localVariableTypeInfo.u2startPC,
                                                            localVariableTypeInfo.u2length);
        localVariableTypeInfo.u2startPC = remapInstructionOffset(localVariableTypeInfo.u2startPC);
    }
    private void remapJumpOffsets(int offset, int[] jumpOffsets)
    {
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            jumpOffsets[index] = remapBranchOffset(offset, jumpOffsets[index]);
        }
    }
    private int remapBranchOffset(int offset, int branchOffset)
    {
        return remapInstructionOffset(offset + branchOffset) - newOffset;
    }
    private int remapInstructionOffset(int offset)
    {
        if (offset < 0 ||
            offset > codeLength)
        {
            throw new IllegalArgumentException("Invalid instruction offset ["+offset+"] in code with length ["+codeLength+"]");
        }
        return instructionOffsetMap[offset];
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
        return newIndex;
    }
    private class CompositeInstruction
    extends       Instruction
    {
        private Instruction[] instructions;
        private CompositeInstruction(Instruction[] instructions)
        {
            this.instructions = instructions;
        }
        public Instruction shrink()
        {
            for (int index = 0; index < instructions.length; index++)
            {
                instructions[index] = instructions[index].shrink();
            }
            return this;
        }
        public void write(byte[] code, int offset)
        {
            for (int index = 0; index < instructions.length; index++)
            {
                Instruction instruction = instructions[index];
                instruction.write(code, offset);
                offset += instruction.length(offset);
            }
        }
        protected void readInfo(byte[] code, int offset)
        {
            throw new UnsupportedOperationException("Can't read composite instruction");
        }
        protected void writeInfo(byte[] code, int offset)
        {
            throw new UnsupportedOperationException("Can't write composite instruction");
        }
        public int length(int offset)
        {
            int newOffset = offset;
            for (int index = 0; index < instructions.length; index++)
            {
                newOffset += instructions[index].length(newOffset);
            }
            return newOffset - offset;
        }
        public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
        {
            if (instructionVisitor != CodeAttributeEditor.this)
            {
                throw new UnsupportedOperationException("Unexpected visitor ["+instructionVisitor+"]");
            }
            for (int index = 0; index < instructions.length; index++)
            {
                Instruction instruction = instructions[index];
                instruction.accept(clazz, method, codeAttribute, offset, CodeAttributeEditor.this);
                offset += instruction.length(offset);
            }
        }
        public String toString()
        {
            StringBuffer stringBuffer = new StringBuffer();
            for (int index = 0; index < instructions.length; index++)
            {
                stringBuffer.append(instructions[index].toString()).append("; ");
            }
            return stringBuffer.toString();
        }
    }
}
