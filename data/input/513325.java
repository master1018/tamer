package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class BranchInstruction extends Instruction
{
    public int branchOffset;
    public BranchInstruction() {}
    public BranchInstruction(byte opcode, int branchOffset)
    {
        this.opcode       = opcode;
        this.branchOffset = branchOffset;
    }
    public BranchInstruction copy(BranchInstruction branchInstruction)
    {
        this.opcode       = branchInstruction.opcode;
        this.branchOffset = branchInstruction.branchOffset;
        return this;
    }
    public byte canonicalOpcode()
    {
        switch (opcode)
        {
            case InstructionConstants.OP_GOTO_W: return InstructionConstants.OP_GOTO;
            case InstructionConstants.OP_JSR_W: return InstructionConstants.OP_JSR;
            default: return opcode;
        }
    }
    public Instruction shrink()
    {
        if (requiredBranchOffsetSize() == 2)
        {
            if      (opcode == InstructionConstants.OP_GOTO_W)
            {
                opcode = InstructionConstants.OP_GOTO;
            }
            else if (opcode == InstructionConstants.OP_JSR_W)
            {
                opcode = InstructionConstants.OP_JSR;
            }
        }
        else
        {
            if      (opcode == InstructionConstants.OP_GOTO)
            {
                opcode = InstructionConstants.OP_GOTO_W;
            }
            else if (opcode == InstructionConstants.OP_JSR)
            {
                opcode = InstructionConstants.OP_JSR_W;
            }
            else
            {
                throw new IllegalArgumentException("Branch instruction can't be widened ("+this.toString()+")");
            }
        }
        return this;
    }
    protected void readInfo(byte[] code, int offset)
    {
        branchOffset = readSignedValue(code, offset, branchOffsetSize());
    }
    protected void writeInfo(byte[] code, int offset)
    {
        if (requiredBranchOffsetSize() > branchOffsetSize())
        {
            throw new IllegalArgumentException("Instruction has invalid branch offset size ("+this.toString(offset)+")");
        }
        writeSignedValue(code, offset, branchOffset, branchOffsetSize());
    }
    public int length(int offset)
    {
        return 1 + branchOffsetSize();
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitBranchInstruction(clazz, method, codeAttribute, offset, this);
    }
    public String toString(int offset)
    {
        return "["+offset+"] "+toString()+" (target="+(offset+branchOffset)+")";
    }
    public String toString()
    {
        return getName()+" "+(branchOffset >= 0 ? "+" : "")+branchOffset;
    }
    private int branchOffsetSize()
    {
        return opcode == InstructionConstants.OP_GOTO_W ||
               opcode == InstructionConstants.OP_JSR_W  ? 4 :
                                                          2;
    }
    private int requiredBranchOffsetSize()
    {
        return branchOffset << 16 >> 16 == branchOffset ? 2 :
                                                          4;
    }
}
