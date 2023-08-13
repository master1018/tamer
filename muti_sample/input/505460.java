package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.visitor.ClassPrinter;
import proguard.classfile.attribute.*;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class StackSizeComputer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ExceptionInfoVisitor
{
    private static final boolean DEBUG = false;
    public boolean isReachable(int instructionOffset)
    {
        return evaluated[instructionOffset];
    }
    public int getStackSize(int instructionOffset)
    {
        if (!evaluated[instructionOffset])
        {
            throw new IllegalArgumentException("Unknown stack size at unreachable instruction offset ["+instructionOffset+"]");
        }
        return stackSizes[instructionOffset];
    }
    public int getMaxStackSize()
    {
        return maxStackSize;
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
            System.err.println("Unexpected error while computing stack sizes:");
            System.err.println("  Class       = ["+clazz.getName()+"]");
            System.err.println("  Method      = ["+method.getName(clazz)+method.getDescriptor(clazz)+"]");
            System.err.println("  Exception   = ["+ex.getClass().getName()+"] ("+ex.getMessage()+")");
            if (DEBUG)
            {
                method.accept(clazz, new ClassPrinter());
            }
            throw ex;
        }
    }
    public void visitCodeAttribute0(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (DEBUG)
        {
            System.out.println("StackSizeComputer: "+clazz.getName()+"."+method.getName(clazz)+method.getDescriptor(clazz));
        }
        int codeLength = codeAttribute.u4codeLength;
        if (evaluated.length < codeLength)
        {
            evaluated  = new boolean[codeLength];
            stackSizes = new int[codeLength];
        }
        else
        {
            for (int index = 0; index < codeLength; index++)
            {
                evaluated[index] = false;
            }
        }
        stackSize    = 0;
        maxStackSize = 0;
        evaluateInstructionBlock(clazz, method, codeAttribute, 0);
        codeAttribute.exceptionsAccept(clazz, method, this);
    }
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        byte opcode = simpleInstruction.opcode;
        exitInstructionBlock =
            opcode == InstructionConstants.OP_IRETURN ||
            opcode == InstructionConstants.OP_LRETURN ||
            opcode == InstructionConstants.OP_FRETURN ||
            opcode == InstructionConstants.OP_DRETURN ||
            opcode == InstructionConstants.OP_ARETURN ||
            opcode == InstructionConstants.OP_RETURN  ||
            opcode == InstructionConstants.OP_ATHROW;
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        exitInstructionBlock = false;
    }
    public void visitVariableInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VariableInstruction variableInstruction)
    {
        byte opcode = variableInstruction.opcode;
        exitInstructionBlock =
            opcode == InstructionConstants.OP_RET;
    }
    public void visitBranchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, BranchInstruction branchInstruction)
    {
        byte opcode = branchInstruction.opcode;
        evaluateInstructionBlock(clazz,
                                 method,
                                 codeAttribute,
                                 offset +
                                 branchInstruction.branchOffset);
        if (opcode == InstructionConstants.OP_JSR ||
            opcode == InstructionConstants.OP_JSR_W)
        {
            stackSize -= 1;
            evaluateInstructionBlock(clazz,
                                     method,
                                     codeAttribute,
                                     offset + branchInstruction.length(offset));
        }
        exitInstructionBlock =
            opcode == InstructionConstants.OP_GOTO   ||
            opcode == InstructionConstants.OP_GOTO_W ||
            opcode == InstructionConstants.OP_JSR    ||
            opcode == InstructionConstants.OP_JSR_W;
    }
    public void visitAnySwitchInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SwitchInstruction switchInstruction)
    {
        int[] jumpOffsets = switchInstruction.jumpOffsets;
        for (int index = 0; index < jumpOffsets.length; index++)
        {
            evaluateInstructionBlock(clazz,
                                     method,
                                     codeAttribute,
                                     offset + jumpOffsets[index]);
        }
        evaluateInstructionBlock(clazz,
                                 method,
                                 codeAttribute,
                                 offset + switchInstruction.defaultOffset);
        exitInstructionBlock = true;
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        if (DEBUG)
        {
            System.out.println("Exception:");
        }
        stackSize = 1;
        evaluateInstructionBlock(clazz,
                                 method,
                                 codeAttribute,
                                 exceptionInfo.u2handlerPC);
    }
    private void evaluateInstructionBlock(Clazz         clazz,
                                          Method        method,
                                          CodeAttribute codeAttribute,
                                          int           instructionOffset)
    {
        if (DEBUG)
        {
            if (evaluated[instructionOffset])
            {
                System.out.println("-- (instruction block at "+instructionOffset+" already evaluated)");
            }
            else
            {
                System.out.println("-- instruction block:");
            }
        }
        int initialStackSize = stackSize;
        if (maxStackSize < stackSize)
        {
            maxStackSize = stackSize;
        }
        while (!evaluated[instructionOffset])
        {
            evaluated[instructionOffset] = true;
            Instruction instruction = InstructionFactory.create(codeAttribute.code,
                                                                instructionOffset);
            if (DEBUG)
            {
                int stackPushCount = instruction.stackPushCount(clazz);
                int stackPopCount  = instruction.stackPopCount(clazz);
                System.out.println("["+instructionOffset+"]: "+
                                   stackSize+" - "+
                                   stackPopCount+" + "+
                                   stackPushCount+" = "+
                                   (stackSize+stackPushCount-stackPopCount)+": "+
                                   instruction.toString(instructionOffset));
            }
            stackSize -= instruction.stackPopCount(clazz);
            if (stackSize < 0)
            {
                throw new IllegalArgumentException("Stack size becomes negative after instruction "+
                                                   instruction.toString(instructionOffset)+" in ["+
                                                   clazz.getName()+"."+
                                                   method.getName(clazz)+
                                                   method.getDescriptor(clazz)+"]");
            }
            stackSizes[instructionOffset] =
            stackSize += instruction.stackPushCount(clazz);
            if (maxStackSize < stackSize)
            {
                maxStackSize = stackSize;
            }
            int nextInstructionOffset = instructionOffset +
                                        instruction.length(instructionOffset);
            instruction.accept(clazz, method, codeAttribute, instructionOffset, this);
            if (exitInstructionBlock)
            {
                break;
            }
            instructionOffset = nextInstructionOffset;
            if (DEBUG)
            {
                if (evaluated[instructionOffset])
                {
                    System.out.println("-- (instruction at "+instructionOffset+" already evaluated)");
                }
            }
        }
        this.stackSize = initialStackSize;
    }
}
