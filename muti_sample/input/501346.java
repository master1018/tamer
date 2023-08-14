import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.evaluation.*;
import proguard.evaluation.value.*;
import proguard.optimize.info.*;
public class EvaluationSimplifier
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor
{
    private static final boolean DEBUG = false;
    public EvaluationSimplifier()
    {
        this(new PartialEvaluator(), null);
    }
    public EvaluationSimplifier(PartialEvaluator   partialEvaluator,
                                InstructionVisitor extraInstructionVisitor)
    {
        this.partialEvaluator        = partialEvaluator;
        this.extraInstructionVisitor = extraInstructionVisitor;
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
            System.err.println("Unexpected error while simplifying instructions after partial evaluation:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            System.err.println("Not optimizing this method");
            if (DEBUG)
            {
                method.accept(clazz, new ClassPrinter());
                throw ex;
            }
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println();
            System.out.println("Class "+ClassUtil.externalClassName(clazz.getName()));
            System.out.println("Method "+ClassUtil.externalFullMethodDescription(clazz.getName(),
                                                                                 0,
                                                                                 method.getName(clazz),
                                                                                 method.getDescriptor(clazz)));
        }
        partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute);
        int codeLength = codeAttribute.u4codeLength;
        codeAttributeEditor.reset(codeLength);
        for (int offset = 0; offset < codeLength; offset++)
        {
            if (partialEvaluator.isTraced(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                    offset);
                instruction.accept(clazz, method, codeAttribute, offset, this);
            }
        }
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        switch (simpleInstruction.opcode)
        {
            case InstructionConstants.OP_IALOAD:
            case InstructionConstants.OP_BALOAD:
            case InstructionConstants.OP_CALOAD:
            case InstructionConstants.OP_SALOAD:
            case InstructionConstants.OP_IADD:
            case InstructionConstants.OP_ISUB:
            case InstructionConstants.OP_IMUL:
            case InstructionConstants.OP_IDIV:
            case InstructionConstants.OP_IREM:
            case InstructionConstants.OP_INEG:
            case InstructionConstants.OP_ISHL:
            case InstructionConstants.OP_ISHR:
            case InstructionConstants.OP_IUSHR:
            case InstructionConstants.OP_IAND:
            case InstructionConstants.OP_IOR:
            case InstructionConstants.OP_IXOR:
            case InstructionConstants.OP_L2I:
            case InstructionConstants.OP_F2I:
            case InstructionConstants.OP_D2I:
            case InstructionConstants.OP_I2B:
            case InstructionConstants.OP_I2C:
            case InstructionConstants.OP_I2S:
                replaceIntegerPushInstruction(clazz, offset, simpleInstruction);
                break;
            case InstructionConstants.OP_LALOAD:
            case InstructionConstants.OP_LADD:
            case InstructionConstants.OP_LSUB:
            case InstructionConstants.OP_LMUL:
            case InstructionConstants.OP_LDIV:
            case InstructionConstants.OP_LREM:
            case InstructionConstants.OP_LNEG:
            case InstructionConstants.OP_LSHL:
            case InstructionConstants.OP_LSHR:
            case InstructionConstants.OP_LUSHR:
            case InstructionConstants.OP_LAND:
            case InstructionConstants.OP_LOR:
            case InstructionConstants.OP_LXOR:
            case InstructionConstants.OP_I2L:
            case InstructionConstants.OP_F2L:
            case InstructionConstants.OP_D2L:
                replaceLongPushInstruction(clazz, offset, simpleInstruction);
                break;
            case InstructionConstants.OP_FALOAD:
            case InstructionConstants.OP_FADD:
            case InstructionConstants.OP_FSUB:
            case InstructionConstants.OP_FMUL:
            case InstructionConstants.OP_FDIV:
            case InstructionConstants.OP_FREM:
            case InstructionConstants.OP_FNEG:
            case InstructionConstants.OP_I2F:
            case InstructionConstants.OP_L2F:
            case InstructionConstants.OP_D2F:
                replaceFloatPushInstruction(clazz, offset, simpleInstruction);
                break;
            case InstructionConstants.OP_DALOAD:
            case InstructionConstants.OP_DADD:
            case InstructionConstants.OP_DSUB:
            case InstructionConstants.OP_DMUL:
            case InstructionConstants.OP_DDIV:
            case InstructionConstants.OP_DREM:
            case InstructionConstants.OP_DNEG:
            case InstructionConstants.OP_I2D:
            case InstructionConstants.OP_L2D:
            case InstructionConstants.OP_F2D:
                replaceDoublePushInstruction(clazz, offset, simpleInstruction);
                break;
            case InstructionConstants.OP_AALOAD:
                replaceReferencePushInstruction(clazz, offset, simpleInstruction);
                break;
        }
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        int variableIndex = variableInstruction.variableIndex;
        switch (variableInstruction.opcode)
        {
            case InstructionConstants.OP_ILOAD:
            case InstructionConstants.OP_ILOAD_0:
            case InstructionConstants.OP_ILOAD_1:
            case InstructionConstants.OP_ILOAD_2:
            case InstructionConstants.OP_ILOAD_3:
                replaceIntegerPushInstruction(clazz, offset, variableInstruction, variableIndex);
                break;
            case InstructionConstants.OP_LLOAD:
            case InstructionConstants.OP_LLOAD_0:
            case InstructionConstants.OP_LLOAD_1:
            case InstructionConstants.OP_LLOAD_2:
            case InstructionConstants.OP_LLOAD_3:
                replaceLongPushInstruction(clazz, offset, variableInstruction, variableIndex);
                break;
            case InstructionConstants.OP_FLOAD:
            case InstructionConstants.OP_FLOAD_0:
            case InstructionConstants.OP_FLOAD_1:
            case InstructionConstants.OP_FLOAD_2:
            case InstructionConstants.OP_FLOAD_3:
                replaceFloatPushInstruction(clazz, offset, variableInstruction, variableIndex);
                break;
            case InstructionConstants.OP_DLOAD:
            case InstructionConstants.OP_DLOAD_0:
            case InstructionConstants.OP_DLOAD_1:
            case InstructionConstants.OP_DLOAD_2:
            case InstructionConstants.OP_DLOAD_3:
                replaceDoublePushInstruction(clazz, offset, variableInstruction, variableIndex);
                break;
            case InstructionConstants.OP_ALOAD:
            case InstructionConstants.OP_ALOAD_0:
            case InstructionConstants.OP_ALOAD_1:
            case InstructionConstants.OP_ALOAD_2:
            case InstructionConstants.OP_ALOAD_3:
                replaceReferencePushInstruction(clazz, offset, variableInstruction);
                break;
            case InstructionConstants.OP_ASTORE:
            case InstructionConstants.OP_ASTORE_0:
            case InstructionConstants.OP_ASTORE_1:
            case InstructionConstants.OP_ASTORE_2:
            case InstructionConstants.OP_ASTORE_3:
                deleteReferencePopInstruction(clazz, offset, variableInstruction);
                break;
            case InstructionConstants.OP_RET:
                replaceBranchInstruction(clazz, offset, variableInstruction);
                break;
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        switch (constantInstruction.opcode)
        {
            case InstructionConstants.OP_GETSTATIC:
            case InstructionConstants.OP_GETFIELD:
                replaceAnyPushInstruction(clazz, offset, constantInstruction);
                break;
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                if (constantInstruction.stackPushCount(clazz) > 0 &&
                    !sideEffectInstructionChecker.hasSideEffects(clazz,
                                                                 method,
                                                                 codeAttribute,
                                                                 offset,
                                                                 constantInstruction))
                {
                    replaceAnyPushInstruction(clazz, offset, constantInstruction);
                }
                break;
            case InstructionConstants.OP_CHECKCAST:
                replaceReferencePushInstruction(clazz, offset, constantInstruction);
                break;
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        switch (branchInstruction.opcode)
        {
            case InstructionConstants.OP_GOTO:
            case InstructionConstants.OP_GOTO_W:
                break;
            case InstructionConstants.OP_JSR:
            case InstructionConstants.OP_JSR_W:
                replaceJsrInstruction(clazz, offset, branchInstruction);
                break;
            default:
                replaceBranchInstruction(clazz, offset, branchInstruction);
                break;
        }
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        replaceBranchInstruction(clazz, offset, switchInstruction);
        if (!codeAttributeEditor.isModified(offset))
        {
            replaceSwitchInstruction(clazz, offset, switchInstruction);
        }
    }
    private void replaceAnyPushInstruction(Clazz       clazz,
                                           int         offset,
                                           Instruction instruction)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            switch (pushedValue.computationalType())
            {
                case Value.TYPE_INTEGER:
                    replaceIntegerPushInstruction(clazz, offset, instruction);
                    break;
                case Value.TYPE_LONG:
                    replaceLongPushInstruction(clazz, offset, instruction);
                    break;
                case Value.TYPE_FLOAT:
                    replaceFloatPushInstruction(clazz, offset, instruction);
                    break;
                case Value.TYPE_DOUBLE:
                    replaceDoublePushInstruction(clazz, offset, instruction);
                    break;
                case Value.TYPE_REFERENCE:
                    replaceReferencePushInstruction(clazz, offset, instruction);
                    break;
            }
        }
    }
    private void replaceIntegerPushInstruction(Clazz       clazz,
                                               int         offset,
                                               Instruction instruction)
    {
        replaceIntegerPushInstruction(clazz,
                                      offset,
                                      instruction,
                                      partialEvaluator.getVariablesBefore(offset).size());
    }
    private void replaceIntegerPushInstruction(Clazz       clazz,
                                               int         offset,
                                               Instruction instruction,
                                               int         maxVariableIndex)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            int value = pushedValue.integerValue().value();
            if (value << 16 >> 16 == value)
            {
                replaceConstantPushInstruction(clazz,
                                               offset,
                                               instruction,
                                               InstructionConstants.OP_SIPUSH,
                                               value);
            }
            else
            {
                ConstantPoolEditor constantPoolEditor =
                    new ConstantPoolEditor((ProgramClass)clazz);
                Instruction replacementInstruction =
                    new ConstantInstruction(InstructionConstants.OP_LDC,
                                            constantPoolEditor.addIntegerConstant(value)).shrink();
                replaceInstruction(clazz, offset, instruction, replacementInstruction);
            }
        }
        else if (pushedValue.isSpecific())
        {
            TracedVariables variables = partialEvaluator.getVariablesBefore(offset);
            for (int variableIndex = 0; variableIndex < maxVariableIndex; variableIndex++)
            {
                if (pushedValue.equals(variables.load(variableIndex)))
                {
                    replaceVariablePushInstruction(clazz,
                                                   offset,
                                                   instruction,
                                                   InstructionConstants.OP_ILOAD,
                                                   variableIndex);
                }
            }
        }
    }
    private void replaceLongPushInstruction(Clazz       clazz,
                                            int         offset,
                                            Instruction instruction)
    {
        replaceLongPushInstruction(clazz,
                                   offset,
                                   instruction,
                                   partialEvaluator.getVariablesBefore(offset).size());
    }
    private void replaceLongPushInstruction(Clazz       clazz,
                                            int         offset,
                                            Instruction instruction,
                                            int         maxVariableIndex)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            long value = pushedValue.longValue().value();
            if (value == 0L ||
                value == 1L)
            {
                replaceConstantPushInstruction(clazz,
                                       offset,
                                       instruction,
                                       InstructionConstants.OP_LCONST_0,
                                       (int)value);
            }
            else
            {
                ConstantPoolEditor constantPoolEditor =
                    new ConstantPoolEditor((ProgramClass)clazz);
                Instruction replacementInstruction =
                    new ConstantInstruction(InstructionConstants.OP_LDC2_W,
                                            constantPoolEditor.addLongConstant(value)).shrink();
                replaceInstruction(clazz, offset, instruction, replacementInstruction);
            }
        }
        else if (pushedValue.isSpecific())
        {
            TracedVariables variables = partialEvaluator.getVariablesBefore(offset);
            for (int variableIndex = 0; variableIndex < maxVariableIndex; variableIndex++)
            {
                if (pushedValue.equals(variables.load(variableIndex)))
                {
                    replaceVariablePushInstruction(clazz,
                                                   offset,
                                                   instruction,
                                                   InstructionConstants.OP_LLOAD,
                                                   variableIndex);
                }
            }
        }
    }
    private void replaceFloatPushInstruction(Clazz       clazz,
                                             int         offset,
                                             Instruction instruction)
    {
        replaceFloatPushInstruction(clazz,
                                    offset,
                                    instruction,
                                    partialEvaluator.getVariablesBefore(offset).size());
    }
    private void replaceFloatPushInstruction(Clazz       clazz,
                                             int         offset,
                                             Instruction instruction,
                                             int         maxVariableIndex)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            float value = pushedValue.floatValue().value();
            if (value == 0f ||
                value == 1f ||
                value == 2f)
            {
                replaceConstantPushInstruction(clazz,
                                               offset,
                                               instruction,
                                               InstructionConstants.OP_FCONST_0,
                                               (int)value);
            }
            else
            {
                ConstantPoolEditor constantPoolEditor =
                    new ConstantPoolEditor((ProgramClass)clazz);
                Instruction replacementInstruction =
                    new ConstantInstruction(InstructionConstants.OP_LDC,
                                            constantPoolEditor.addFloatConstant(value)).shrink();
                replaceInstruction(clazz, offset, instruction, replacementInstruction);
            }
        }
        else if (pushedValue.isSpecific())
        {
            TracedVariables variables = partialEvaluator.getVariablesBefore(offset);
            for (int variableIndex = 0; variableIndex < maxVariableIndex; variableIndex++)
            {
                if (pushedValue.equals(variables.load(variableIndex)))
                {
                    replaceVariablePushInstruction(clazz,
                                                   offset,
                                                   instruction,
                                                   InstructionConstants.OP_FLOAD,
                                                   variableIndex);
                }
            }
        }
    }
    private void replaceDoublePushInstruction(Clazz       clazz,
                                              int         offset,
                                              Instruction instruction)
    {
        replaceDoublePushInstruction(clazz,
                                     offset,
                                     instruction,
                                     partialEvaluator.getVariablesBefore(offset).size());
    }
    private void replaceDoublePushInstruction(Clazz       clazz,
                                              int         offset,
                                              Instruction instruction,
                                              int         maxVariableIndex)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            double value = pushedValue.doubleValue().value();
            if (value == 0.0 ||
                value == 1.0)
            {
                replaceConstantPushInstruction(clazz,
                                               offset,
                                               instruction,
                                               InstructionConstants.OP_DCONST_0,
                                               (int)value);
            }
            else
            {
                ConstantPoolEditor constantPoolEditor =
                    new ConstantPoolEditor((ProgramClass)clazz);
                Instruction replacementInstruction =
                    new ConstantInstruction(InstructionConstants.OP_LDC2_W,
                                            constantPoolEditor.addDoubleConstant(value)).shrink();
                replaceInstruction(clazz, offset, instruction, replacementInstruction);
            }
        }
        else if (pushedValue.isSpecific())
        {
            TracedVariables variables = partialEvaluator.getVariablesBefore(offset);
            for (int variableIndex = 0; variableIndex < maxVariableIndex; variableIndex++)
            {
                if (pushedValue.equals(variables.load(variableIndex)))
                {
                    replaceVariablePushInstruction(clazz,
                                                   offset,
                                                   instruction,
                                                   InstructionConstants.OP_DLOAD,
                                                   variableIndex);
                }
            }
        }
    }
    private void replaceReferencePushInstruction(Clazz       clazz,
                                                 int         offset,
                                                 Instruction instruction)
    {
        Value pushedValue = partialEvaluator.getStackAfter(offset).getTop(0);
        if (pushedValue.isParticular())
        {
            replaceConstantPushInstruction(clazz,
                                           offset,
                                           instruction,
                                           InstructionConstants.OP_ACONST_NULL,
                                           0);
        }
    }
    private void replaceConstantPushInstruction(Clazz       clazz,
                                                int         offset,
                                                Instruction instruction,
                                                byte        replacementOpcode,
                                                int         value)
    {
        Instruction replacementInstruction =
            new SimpleInstruction(replacementOpcode, value).shrink();
        replaceInstruction(clazz, offset, instruction, replacementInstruction);
    }
    private void replaceVariablePushInstruction(Clazz       clazz,
                                                int         offset,
                                                Instruction instruction,
                                                byte        replacementOpcode,
                                                int         variableIndex)
    {
        Instruction replacementInstruction =
            new VariableInstruction(replacementOpcode, variableIndex).shrink();
        replaceInstruction(clazz, offset, instruction, replacementInstruction);
    }
    private void replaceJsrInstruction(Clazz             clazz,
                                       int               offset,
                                       BranchInstruction branchInstruction)
    {
        int subroutineStart = offset + branchInstruction.branchOffset;
        if (!partialEvaluator.isSubroutineReturning(subroutineStart) ||
            partialEvaluator.branchOrigins(subroutineStart).instructionOffsetCount() == 1)
        {
            replaceBranchInstruction(clazz, offset, branchInstruction);
        }
        else if (!partialEvaluator.isTraced(offset + branchInstruction.length(offset)))
        {
            replaceByInfiniteLoop(clazz, offset + branchInstruction.length(offset), branchInstruction);
        }
    }
    private void deleteReferencePopInstruction(Clazz       clazz,
                                               int         offset,
                                               Instruction instruction)
    {
        if (partialEvaluator.isSubroutineStart(offset) &&
            (!partialEvaluator.isSubroutineReturning(offset) ||
             partialEvaluator.branchOrigins(offset).instructionOffsetCount() == 1))
        {
            if (DEBUG) System.out.println("  Deleting store of subroutine return address "+instruction.toString(offset));
            codeAttributeEditor.deleteInstruction(offset);
        }
    }
    private void replaceBranchInstruction(Clazz       clazz,
                                          int         offset,
                                          Instruction instruction)
    {
        InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);
        if (branchTargets != null &&
            branchTargets.instructionOffsetCount() == 1)
        {
            int branchOffset = branchTargets.instructionOffset(0) - offset;
            if (branchOffset == instruction.length(offset))
            {
                if (DEBUG) System.out.println("  Ignoring zero branch instruction at ["+offset+"]");
            }
            else
            {
                Instruction replacementInstruction =
                    new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                          branchOffset).shrink();
                replaceInstruction(clazz, offset, instruction, replacementInstruction);
            }
        }
    }
    private void replaceSwitchInstruction(Clazz             clazz,
                                          int               offset,
                                          SwitchInstruction switchInstruction)
    {
        InstructionOffsetValue branchTargets = partialEvaluator.branchTargets(offset);
        int defaultOffset =
            branchTargets.instructionOffset(branchTargets.instructionOffsetCount()-1) -
            offset;
        Instruction replacementInstruction = null;
        int[] jumpOffsets = switchInstruction.jumpOffsets;
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            if (!branchTargets.contains(offset + jumpOffsets[index]))
            {
                jumpOffsets[index] = defaultOffset;
                replacementInstruction = switchInstruction;
            }
        }
        if (!branchTargets.contains(offset + switchInstruction.defaultOffset))
        {
            switchInstruction.defaultOffset = defaultOffset;
            replacementInstruction = switchInstruction;
        }
        if (replacementInstruction != null)
        {
            replaceInstruction(clazz, offset, switchInstruction, replacementInstruction);
        }
    }
    private void replaceByInfiniteLoop(Clazz       clazz,
                                       int         offset,
                                       Instruction instruction)
    {
        Instruction replacementInstruction =
            new BranchInstruction(InstructionConstants.OP_GOTO, 0);
        if (DEBUG) System.out.println("  Replacing unreachable instruction by infinite loop "+replacementInstruction.toString(offset));
        codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        if (extraInstructionVisitor != null)
        {
            instruction.accept(clazz, null, null, offset, extraInstructionVisitor);
        }
    }
    private void replaceInstruction(Clazz       clazz,
                                    int         offset,
                                    Instruction instruction,
                                    Instruction replacementInstruction)
    {
        int popCount =
            instruction.stackPopCount(clazz) -
            replacementInstruction.stackPopCount(clazz);
        insertPopInstructions(offset, popCount);
        if (DEBUG) System.out.println("  Replacing instruction "+instruction.toString(offset)+" -> "+replacementInstruction.toString()+(popCount == 0 ? "" : " ("+popCount+" pops)"));
        codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        if (extraInstructionVisitor != null)
        {
            instruction.accept(clazz, null, null, offset, extraInstructionVisitor);
        }
    }
    private void insertPopInstructions(int offset, int popCount)
    {
        switch (popCount)
        {
            case 0:
            {
                break;
            }
            case 1:
            {
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP);
                codeAttributeEditor.insertBeforeInstruction(offset,
                                                            popInstruction);
                break;
            }
            case 2:
            {
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP2);
                codeAttributeEditor.insertBeforeInstruction(offset,
                                                            popInstruction);
                break;
            }
            default:
            {
                Instruction[] popInstructions =
                    new Instruction[popCount / 2 + popCount % 2];
                Instruction popInstruction =
                    new SimpleInstruction(InstructionConstants.OP_POP2);
                for (int index = 0; index < popCount / 2; index++)
                {
                      popInstructions[index] = popInstruction;
                }
                if (popCount % 2 == 1)
                {
                    popInstruction =
                        new SimpleInstruction(InstructionConstants.OP_POP);
                    popInstructions[popCount / 2] = popInstruction;
                }
                codeAttributeEditor.insertBeforeInstruction(offset,
                                                            popInstructions);
                break;
            }
        }
    }
}
