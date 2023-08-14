import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.editor.CodeAttributeEditor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class GotoCommonCodeReplacer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor
{
    private static final boolean DEBUG = false;
    public GotoCommonCodeReplacer(InstructionVisitor  extraInstructionVisitor)
    {
        this.extraInstructionVisitor = extraInstructionVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        branchTargetFinder.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;
        if ((opcode == InstructionConstants.OP_GOTO ||
             opcode == InstructionConstants.OP_GOTO_W) &&
            !branchTargetFinder.isBranchTarget(offset))
        {
            int branchOffset = branchInstruction.branchOffset;
            int targetOffset = offset + branchOffset;
            int commonCount = commonByteCodeCount(codeAttribute, offset, targetOffset);
            if (commonCount > 0 &&
                !exceptionBoundary(codeAttribute, offset, targetOffset))
            {
                if (DEBUG)
                {
                    System.out.println("GotoCommonCodeReplacer: "+clazz.getName()+"."+method.getName(clazz)+" (["+(offset-commonCount)+"] - "+branchInstruction.toString(offset)+" -> "+targetOffset+")");
                }
                for (int delta = 0; delta <= commonCount; delta++)
                {
                    int deleteOffset = offset - delta;
                    if (branchTargetFinder.isInstruction(deleteOffset))
                    {
                        codeAttributeEditor.replaceInstruction(     deleteOffset, (Instruction)null);
                        codeAttributeEditor.insertBeforeInstruction(deleteOffset, (Instruction)null);
                        codeAttributeEditor.insertAfterInstruction( deleteOffset, (Instruction)null);
                        codeAttributeEditor.deleteInstruction(deleteOffset);
                    }
                }
                int newBranchOffset = branchOffset - commonCount;
                if (newBranchOffset != branchInstruction.length(offset))
                {
                    Instruction newGotoInstruction =
                         new BranchInstruction(opcode, newBranchOffset);
                    codeAttributeEditor.replaceInstruction(offset,
                                                           newGotoInstruction);
                }
                if (extraInstructionVisitor != null)
                {
                    extraInstructionVisitor.visitBranchInstruction(clazz, method, codeAttribute, offset, branchInstruction);
                }
            }
        }
    }
    private int commonByteCodeCount(CodeAttribute codeAttribute, int offset1, int offset2)
    {
        byte[] code = codeAttribute.code;
        int successfulDelta = 0;
        for (int delta = 1;
             delta <= offset1 &&
             delta <= offset2 &&
             offset2 - delta != offset1;
             delta++)
        {
            int newOffset1 = offset1 - delta;
            int newOffset2 = offset2 - delta;
            if (code[newOffset1] != code[newOffset2])
            {
                break;
            }
            if (branchTargetFinder.isInstruction(newOffset1) ^
                branchTargetFinder.isInstruction(newOffset2))
            {
                break;
            }
            if (branchTargetFinder.isInstruction(newOffset1) &&
                branchTargetFinder.isInstruction(newOffset2))
            {
                if (branchTargetFinder.isBranchOrigin(newOffset1)   ||
                    branchTargetFinder.isBranchTarget(newOffset1)   ||
                    branchTargetFinder.isExceptionStart(newOffset1) ||
                    branchTargetFinder.isExceptionEnd(newOffset1)   ||
                    branchTargetFinder.isInitializer(newOffset1)    ||
                    branchTargetFinder.isExceptionStart(newOffset2) ||
                    branchTargetFinder.isExceptionEnd(newOffset2)   ||
                    isPop(code[newOffset1]))
                {
                    break;
                }
                if (branchTargetFinder.isBranchTarget(newOffset2))
                {
                    successfulDelta = delta;
                }
                if (branchTargetFinder.isBranchTarget(newOffset1))
                {
                    break;
                }
            }
        }
        return successfulDelta;
    }
    private boolean isPop(byte opcode)
    {
        return opcode == InstructionConstants.OP_POP  ||
               opcode == InstructionConstants.OP_POP2 ||
               opcode == InstructionConstants.OP_ARRAYLENGTH;
    }
    private boolean exceptionBoundary(CodeAttribute codeAttribute, int offset1, int offset2)
    {
        if (offset2 < offset1)
        {
            int offset = offset1;
            offset1 = offset2;
            offset2 = offset;
        }
        for (int offset = offset1; offset <= offset2; offset++)
        {
            if (branchTargetFinder.isExceptionStart(offset) ||
                branchTargetFinder.isExceptionEnd(offset))
            {
                return true;
            }
        }
        return false;
    }
}
