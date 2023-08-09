import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.editor.CodeAttributeComposer;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
import proguard.optimize.peephole.BranchTargetFinder;
public class CodeSubroutineInliner
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor
{
    private static final boolean DEBUG = false;
    private boolean containsSubroutines(CodeAttribute codeAttribute)
    {
        for (int offset = 0; offset < codeAttribute.u4codeLength; offset++)
        {
            if (branchTargetFinder.isSubroutineInvocation(offset))
            {
                return true;
            }
        }
        return false;
    }
    private void inlineSubroutine(Clazz         clazz,
                                  Method        method,
                                  CodeAttribute codeAttribute,
                                  int           subroutineInvocationOffset,
                                  int           subroutineStart)
    {
        int subroutineEnd = branchTargetFinder.subroutineEnd(subroutineStart);
        if (DEBUG)
        {
            System.out.println("  Inlining subroutine ["+subroutineStart+" -> "+subroutineEnd+"] at ["+subroutineInvocationOffset+"]");
        }
        ExceptionInfoVisitor oldSubroutineExceptionInliner = subroutineExceptionInliner;
        int                  oldClipStart                  = clipStart;
        int                  oldClipEnd                    = clipEnd;
        subroutineExceptionInliner =
            new ExceptionExcludedOffsetFilter(subroutineInvocationOffset,
                                              subroutineExceptionInliner);
        clipStart = subroutineStart;
        clipEnd   = subroutineEnd;
        codeAttributeComposer.beginCodeFragment(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz,
                                         method,
                                         subroutineStart,
                                         subroutineEnd,
                                         this);
        if (DEBUG)
        {
            System.out.println("    Appending label after inlined subroutine at ["+subroutineEnd+"]");
        }
        codeAttributeComposer.appendLabel(subroutineEnd);
        codeAttribute.exceptionsAccept(clazz,
                                       method,
                                       subroutineStart,
                                       subroutineEnd,
                                       subroutineExceptionInliner);
        subroutineExceptionInliner = oldSubroutineExceptionInliner;
        clipStart                  = oldClipStart;
        clipEnd                    = oldClipEnd;
        codeAttributeComposer.endCodeFragment();
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        codeAttributeComposer.appendInstruction(offset, instruction.shrink());
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        byte opcode = variableInstruction.opcode;
        if (opcode == InstructionConstants.OP_RET)
        {
            if (branchTargetFinder.subroutineEnd(offset) == offset + variableInstruction.length(offset))
            {
                if (DEBUG)
                {
                    System.out.println("    Replacing subroutine return at ["+offset+"] by a label");
                }
                codeAttributeComposer.appendLabel(offset);
            }
            else
            {
                if (DEBUG)
                {
                    System.out.println("    Replacing subroutine return at ["+offset+"] by a simple branch");
                }
                Instruction replacementInstruction =
                    new BranchInstruction(InstructionConstants.OP_GOTO,
                                          branchTargetFinder.subroutineEnd(offset) - offset).shrink();
                codeAttributeComposer.appendInstruction(offset, replacementInstruction);
            }
        }
        else if (branchTargetFinder.isSubroutineStart(offset))
        {
            if (DEBUG)
            {
                System.out.println("    Replacing first subroutine instruction at ["+offset+"] by a label");
            }
            codeAttributeComposer.appendLabel(offset);
        }
        else
        {
            codeAttributeComposer.appendInstruction(offset, variableInstruction);
        }
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;
        if (opcode == InstructionConstants.OP_JSR ||
            opcode == InstructionConstants.OP_JSR_W)
        {
            int branchOffset = branchInstruction.branchOffset;
            int branchTarget = offset + branchOffset;
            if (branchTargetFinder.isSubroutineReturning(branchTarget))
            {
                codeAttributeComposer.appendLabel(offset);
                inlineSubroutine(clazz,
                                 method,
                                 codeAttribute,
                                 offset,
                                 branchTarget);
            }
            else
            {
                if (DEBUG)
                {
                    System.out.println("Replacing subroutine invocation at ["+offset+"] by a simple branch");
                }
                Instruction replacementInstruction =
                    new BranchInstruction(InstructionConstants.OP_GOTO,
                                          branchOffset).shrink();
                codeAttributeComposer.appendInstruction(offset, replacementInstruction);
            }
        }
        else
        {
            codeAttributeComposer.appendInstruction(offset, branchInstruction);
        }
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        int startPC   = Math.max(exceptionInfo.u2startPC, clipStart);
        int endPC     = Math.min(exceptionInfo.u2endPC,   clipEnd);
        int handlerPC = exceptionInfo.u2handlerPC;
        int catchType = exceptionInfo.u2catchType;
        for (int offset = startPC; offset < endPC; offset++)
        {
            if (branchTargetFinder.isSubroutineInvocation(offset))
            {
                Instruction instruction = InstructionFactory.create(codeAttribute.code, offset);
                int instructionLength = instruction.length(offset);
                if (!exceptionInfo.isApplicable(offset + ((BranchInstruction)instruction).branchOffset))
                {
                    if (DEBUG)
                    {
                        System.out.println("  Appending extra exception ["+startPC+" -> "+offset+"] -> "+handlerPC);
                    }
                    codeAttributeComposer.appendException(new ExceptionInfo(startPC,
                                                                            offset,
                                                                            handlerPC,
                                                                            catchType));
                    startPC = offset + instructionLength;
                }
            }
        }
        if (DEBUG)
        {
            if (startPC == exceptionInfo.u2startPC &&
                endPC   == exceptionInfo.u2endPC)
            {
                System.out.println("  Appending exception ["+startPC+" -> "+endPC+"] -> "+handlerPC);
            }
            else
            {
                System.out.println("  Appending clipped exception ["+exceptionInfo.u2startPC+" -> "+exceptionInfo.u2endPC+"] ~> ["+startPC+" -> "+endPC+"] -> "+handlerPC);
            }
        }
        codeAttributeComposer.appendException(new ExceptionInfo(startPC,
                                                                endPC,
                                                                handlerPC,
                                                                catchType));
    }
}
