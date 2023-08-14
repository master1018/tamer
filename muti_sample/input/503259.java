package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class LookUpSwitchInstruction extends SwitchInstruction
{
    public int[] cases;
    public LookUpSwitchInstruction() {}
    public LookUpSwitchInstruction(byte  opcode,
                                   int   defaultOffset,
                                   int[] cases,
                                   int[] jumpOffsets)
    {
        this.opcode        = opcode;
        this.defaultOffset = defaultOffset;
        this.cases         = cases;
        this.jumpOffsets   = jumpOffsets;
    }
    public LookUpSwitchInstruction copy(LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        this.opcode        = lookUpSwitchInstruction.opcode;
        this.defaultOffset = lookUpSwitchInstruction.defaultOffset;
        this.cases         = lookUpSwitchInstruction.cases;
        this.jumpOffsets   = lookUpSwitchInstruction.jumpOffsets;
        return this;
    }
    public Instruction shrink()
    {
        return this;
    }
    protected void readInfo(byte[] code, int offset)
    {
        offset += -offset & 3;
        defaultOffset       = readInt(code, offset); offset += 4;
        int jumpOffsetCount = readInt(code, offset); offset += 4;
        cases       = new int[jumpOffsetCount];
        jumpOffsets = new int[jumpOffsetCount];
        for (int index = 0; index < jumpOffsetCount; index++)
        {
            cases[index]       = readInt(code, offset); offset += 4;
            jumpOffsets[index] = readInt(code, offset); offset += 4;
        }
    }
    protected void writeInfo(byte[] code, int offset)
    {
        while ((offset & 3) != 0)
        {
            writeByte(code, offset++, 0);
        }
        writeInt(code, offset, defaultOffset); offset += 4;
        writeInt(code, offset, cases.length);  offset += 4;
        for (int index = 0; index < cases.length; index++)
        {
            writeInt(code, offset, cases[index]);       offset += 4;
            writeInt(code, offset, jumpOffsets[index]); offset += 4;
        }
    }
    public int length(int offset)
    {
        return 1 + (-(offset+1) & 3) + 8 + cases.length * 8;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitLookUpSwitchInstruction(clazz, method, codeAttribute, offset, this);
    }
}
