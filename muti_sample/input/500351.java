package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class TableSwitchInstruction extends SwitchInstruction
{
    public int lowCase;
    public int highCase;
    public TableSwitchInstruction() {}
    public TableSwitchInstruction(byte  opcode,
                                  int   defaultOffset,
                                  int   lowCase,
                                  int   highCase,
                                  int[] jumpOffsets)
    {
        this.opcode        = opcode;
        this.defaultOffset = defaultOffset;
        this.lowCase       = lowCase;
        this.highCase      = highCase;
        this.jumpOffsets   = jumpOffsets;
    }
    public TableSwitchInstruction copy(TableSwitchInstruction tableSwitchInstruction)
    {
        this.opcode        = tableSwitchInstruction.opcode;
        this.defaultOffset = tableSwitchInstruction.defaultOffset;
        this.lowCase       = tableSwitchInstruction.lowCase;
        this.highCase      = tableSwitchInstruction.highCase;
        this.jumpOffsets   = tableSwitchInstruction.jumpOffsets;
        return this;
    }
    public Instruction shrink()
    {
        return this;
    }
    protected void readInfo(byte[] code, int offset)
    {
        offset += -offset & 3;
        defaultOffset = readInt(code, offset); offset += 4;
        lowCase       = readInt(code, offset); offset += 4;
        highCase      = readInt(code, offset); offset += 4;
        jumpOffsets = new int[highCase - lowCase + 1];
        for (int index = 0; index < jumpOffsets.length; index++)
        {
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
        writeInt(code, offset, lowCase);       offset += 4;
        writeInt(code, offset, highCase);      offset += 4;
        int length = highCase - lowCase + 1;
        for (int index = 0; index < length; index++)
        {
            writeInt(code, offset, jumpOffsets[index]); offset += 4;
        }
    }
    public int length(int offset)
    {
        return 1 + (-(offset+1) & 3) + 12 + (highCase - lowCase + 1) * 4;
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitTableSwitchInstruction(clazz, method, codeAttribute, offset, this);
    }
}
