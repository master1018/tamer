package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class InstructionWriter
extends      SimplifiedVisitor
implements   InstructionVisitor,
             AttributeVisitor
{
    private int codeLength;
    private CodeAttributeEditor codeAttributeEditor;
    public void reset(int codeLength)
    {
        this.codeLength = codeLength;
        if (codeAttributeEditor != null)
        {
            codeAttributeEditor.reset(codeLength);
        }
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        simpleInstruction.write(codeAttribute, offset);
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        try
        {
            constantInstruction.write(codeAttribute, offset);
        }
        catch (IllegalArgumentException exception)
        {
            Instruction replacementInstruction =
                new ConstantInstruction(constantInstruction.opcode,
                                        constantInstruction.constantIndex,
                                        constantInstruction.constant).shrink();
            replaceInstruction(offset, replacementInstruction);
            constantInstruction.constantIndex = 0;
            constantInstruction.constant      = 0;
            constantInstruction.write(codeAttribute, offset);
        }
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        try
        {
            variableInstruction.write(codeAttribute, offset);
        }
        catch (IllegalArgumentException exception)
        {
            Instruction replacementInstruction =
                new VariableInstruction(variableInstruction.opcode,
                                        variableInstruction.variableIndex,
                                        variableInstruction.constant).shrink();
            replaceInstruction(offset, replacementInstruction);
            variableInstruction.variableIndex = 0;
            variableInstruction.constant      = 0;
            variableInstruction.write(codeAttribute, offset);
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        try
        {
            branchInstruction.write(codeAttribute, offset);
        }
        catch (IllegalArgumentException exception)
        {
            Instruction replacementInstruction =
                new BranchInstruction(InstructionConstants.OP_GOTO_W,
                                      branchInstruction.branchOffset);
            switch (branchInstruction.opcode)
            {
                default:
                {
                    replacementInstruction =
                        new BranchInstruction(branchInstruction.opcode,
                                              branchInstruction.branchOffset).shrink();
                    break;
                }
                case InstructionConstants.OP_IFEQ:
                case InstructionConstants.OP_IFNE:
                case InstructionConstants.OP_IFLT:
                case InstructionConstants.OP_IFGE:
                case InstructionConstants.OP_IFGT:
                case InstructionConstants.OP_IFLE:
                case InstructionConstants.OP_IFICMPEQ:
                case InstructionConstants.OP_IFICMPNE:
                case InstructionConstants.OP_IFICMPLT:
                case InstructionConstants.OP_IFICMPGE:
                case InstructionConstants.OP_IFICMPGT:
                case InstructionConstants.OP_IFICMPLE:
                case InstructionConstants.OP_IFACMPEQ:
                case InstructionConstants.OP_IFACMPNE:
                {
                    Instruction complementaryConditionalBranch =
                        new BranchInstruction((byte)(((branchInstruction.opcode+1) ^ 1) - 1),
                                              (1+2) + (1+4));
                    insertBeforeInstruction(offset, complementaryConditionalBranch);
                    break;
                }
                case InstructionConstants.OP_IFNULL:
                case InstructionConstants.OP_IFNONNULL:
                {
                    Instruction complementaryConditionalBranch =
                        new BranchInstruction((byte)(branchInstruction.opcode ^ 1),
                                              (1+2) + (1+4));
                    insertBeforeInstruction(offset, complementaryConditionalBranch);
                    break;
                }
            }
            replaceInstruction(offset, replacementInstruction);
            branchInstruction.branchOffset = 0;
            branchInstruction.write(codeAttribute, offset);
        }
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        switchInstruction.write(codeAttribute, offset);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (codeAttributeEditor != null)
        {
            codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
            codeAttributeEditor = null;
        }
    }
    private void insertBeforeInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttributeEditor();
        codeAttributeEditor.insertBeforeInstruction(instructionOffset, instruction);
    }
    private void replaceInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttributeEditor();
        codeAttributeEditor.replaceInstruction(instructionOffset, instruction);
    }
    private void insertAfterInstruction(int instructionOffset, Instruction instruction)
    {
        ensureCodeAttributeEditor();
        codeAttributeEditor.insertAfterInstruction(instructionOffset, instruction);
    }
    private void ensureCodeAttributeEditor()
    {
        if (codeAttributeEditor == null)
        {
            codeAttributeEditor = new CodeAttributeEditor();
            codeAttributeEditor.reset(codeLength);
        }
    }
}
