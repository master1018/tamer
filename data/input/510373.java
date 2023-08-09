package proguard.classfile.instruction;
public abstract class SwitchInstruction extends Instruction
{
    public int   defaultOffset;
    public int[] jumpOffsets;
    public SwitchInstruction() {}
    public SwitchInstruction(byte  opcode,
                             int   defaultOffset,
                             int[] jumpOffsets)
    {
        this.opcode        = opcode;
        this.defaultOffset = defaultOffset;
        this.jumpOffsets   = jumpOffsets;
    }
    public SwitchInstruction copy(SwitchInstruction switchInstruction)
    {
        this.opcode        = switchInstruction.opcode;
        this.defaultOffset = switchInstruction.defaultOffset;
        this.jumpOffsets   = switchInstruction.jumpOffsets;
        return this;
    }
    public String toString(int offset)
    {
        return "["+offset+"] "+toString()+" (target="+(offset+defaultOffset)+")";
    }
    public String toString()
    {
        return getName()+" ("+jumpOffsets.length+" offsets, default="+defaultOffset+")";
    }
}
