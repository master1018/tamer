package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class VariableInstruction extends Instruction
{
    public boolean wide;
    public int     variableIndex;
    public int     constant;
    public VariableInstruction() {}
    public VariableInstruction(boolean wide)
    {
        this.wide = wide;
    }
    public VariableInstruction(byte opcode)
    {
        this(opcode, embeddedVariable(opcode), 0);
    }
    public VariableInstruction(byte opcode,
                               int  variableIndex)
    {
        this(opcode, variableIndex, 0);
    }
    public VariableInstruction(byte opcode,
                               int  variableIndex,
                               int  constant)
    {
        this.opcode        = opcode;
        this.variableIndex = variableIndex;
        this.constant      = constant;
        this.wide          = requiredVariableIndexSize() > 1 ||
                             requiredConstantSize()      > 1;
    }
    public VariableInstruction copy(VariableInstruction variableInstruction)
    {
        this.opcode        = variableInstruction.opcode;
        this.variableIndex = variableInstruction.variableIndex;
        this.constant      = variableInstruction.constant;
        this.wide          = variableInstruction.wide;
        return this;
    }
    private static int embeddedVariable(byte opcode)
    {
        switch (opcode)
        {
            case InstructionConstants.OP_ILOAD_1:
            case InstructionConstants.OP_LLOAD_1:
            case InstructionConstants.OP_FLOAD_1:
            case InstructionConstants.OP_DLOAD_1:
            case InstructionConstants.OP_ALOAD_1:
            case InstructionConstants.OP_ISTORE_1:
            case InstructionConstants.OP_LSTORE_1:
            case InstructionConstants.OP_FSTORE_1:
            case InstructionConstants.OP_DSTORE_1:
            case InstructionConstants.OP_ASTORE_1: return 1;
            case InstructionConstants.OP_ILOAD_2:
            case InstructionConstants.OP_LLOAD_2:
            case InstructionConstants.OP_FLOAD_2:
            case InstructionConstants.OP_DLOAD_2:
            case InstructionConstants.OP_ALOAD_2:
            case InstructionConstants.OP_ISTORE_2:
            case InstructionConstants.OP_LSTORE_2:
            case InstructionConstants.OP_FSTORE_2:
            case InstructionConstants.OP_DSTORE_2:
            case InstructionConstants.OP_ASTORE_2: return 2;
            case InstructionConstants.OP_ILOAD_3:
            case InstructionConstants.OP_LLOAD_3:
            case InstructionConstants.OP_FLOAD_3:
            case InstructionConstants.OP_DLOAD_3:
            case InstructionConstants.OP_ALOAD_3:
            case InstructionConstants.OP_ISTORE_3:
            case InstructionConstants.OP_LSTORE_3:
            case InstructionConstants.OP_FSTORE_3:
            case InstructionConstants.OP_DSTORE_3:
            case InstructionConstants.OP_ASTORE_3: return 3;
            default: return 0;
        }
    }
    public boolean isStore()
    {
        return opcode >= InstructionConstants.OP_ISTORE ||
               opcode == InstructionConstants.OP_IINC;
    }
    public boolean isLoad()
    {
        return opcode < InstructionConstants.OP_ISTORE;
    }
    public byte canonicalOpcode()
    {
        switch (opcode)
        {
            case InstructionConstants.OP_ILOAD_0:
            case InstructionConstants.OP_ILOAD_1:
            case InstructionConstants.OP_ILOAD_2:
            case InstructionConstants.OP_ILOAD_3: return InstructionConstants.OP_ILOAD;
            case InstructionConstants.OP_LLOAD_0:
            case InstructionConstants.OP_LLOAD_1:
            case InstructionConstants.OP_LLOAD_2:
            case InstructionConstants.OP_LLOAD_3: return InstructionConstants.OP_LLOAD;
            case InstructionConstants.OP_FLOAD_0:
            case InstructionConstants.OP_FLOAD_1:
            case InstructionConstants.OP_FLOAD_2:
            case InstructionConstants.OP_FLOAD_3: return InstructionConstants.OP_FLOAD;
            case InstructionConstants.OP_DLOAD_0:
            case InstructionConstants.OP_DLOAD_1:
            case InstructionConstants.OP_DLOAD_2:
            case InstructionConstants.OP_DLOAD_3: return InstructionConstants.OP_DLOAD;
            case InstructionConstants.OP_ALOAD_0:
            case InstructionConstants.OP_ALOAD_1:
            case InstructionConstants.OP_ALOAD_2:
            case InstructionConstants.OP_ALOAD_3: return InstructionConstants.OP_ALOAD;
            case InstructionConstants.OP_ISTORE_0:
            case InstructionConstants.OP_ISTORE_1:
            case InstructionConstants.OP_ISTORE_2:
            case InstructionConstants.OP_ISTORE_3: return InstructionConstants.OP_ISTORE;
            case InstructionConstants.OP_LSTORE_0:
            case InstructionConstants.OP_LSTORE_1:
            case InstructionConstants.OP_LSTORE_2:
            case InstructionConstants.OP_LSTORE_3: return InstructionConstants.OP_LSTORE;
            case InstructionConstants.OP_FSTORE_0:
            case InstructionConstants.OP_FSTORE_1:
            case InstructionConstants.OP_FSTORE_2:
            case InstructionConstants.OP_FSTORE_3: return InstructionConstants.OP_FSTORE;
            case InstructionConstants.OP_DSTORE_0:
            case InstructionConstants.OP_DSTORE_1:
            case InstructionConstants.OP_DSTORE_2:
            case InstructionConstants.OP_DSTORE_3: return InstructionConstants.OP_DSTORE;
            case InstructionConstants.OP_ASTORE_0:
            case InstructionConstants.OP_ASTORE_1:
            case InstructionConstants.OP_ASTORE_2:
            case InstructionConstants.OP_ASTORE_3: return InstructionConstants.OP_ASTORE;
            default: return opcode;
        }
    }
    public Instruction shrink()
    {
        opcode = canonicalOpcode();
        if (variableIndex <= 3)
        {
            switch (opcode)
            {
                case InstructionConstants.OP_ILOAD: opcode = (byte)(InstructionConstants.OP_ILOAD_0 + variableIndex); break;
                case InstructionConstants.OP_LLOAD: opcode = (byte)(InstructionConstants.OP_LLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_FLOAD: opcode = (byte)(InstructionConstants.OP_FLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_DLOAD: opcode = (byte)(InstructionConstants.OP_DLOAD_0 + variableIndex); break;
                case InstructionConstants.OP_ALOAD: opcode = (byte)(InstructionConstants.OP_ALOAD_0 + variableIndex); break;
                case InstructionConstants.OP_ISTORE: opcode = (byte)(InstructionConstants.OP_ISTORE_0 + variableIndex); break;
                case InstructionConstants.OP_LSTORE: opcode = (byte)(InstructionConstants.OP_LSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_FSTORE: opcode = (byte)(InstructionConstants.OP_FSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_DSTORE: opcode = (byte)(InstructionConstants.OP_DSTORE_0 + variableIndex); break;
                case InstructionConstants.OP_ASTORE: opcode = (byte)(InstructionConstants.OP_ASTORE_0 + variableIndex); break;
            }
        }
        wide = requiredVariableIndexSize() > 1 ||
               requiredConstantSize()      > 1;
        return this;
    }
    protected boolean isWide()
    {
        return wide;
    }
    protected void readInfo(byte[] code, int offset)
    {
        int variableIndexSize = variableIndexSize();
        int constantSize      = constantSize();
        if (variableIndexSize == 0)
        {
            variableIndex = opcode < InstructionConstants.OP_ISTORE_0 ?
                (opcode - InstructionConstants.OP_ILOAD_0 ) & 3 :
                (opcode - InstructionConstants.OP_ISTORE_0) & 3;
        }
        else
        {
            variableIndex = readValue(code, offset, variableIndexSize); offset += variableIndexSize;
        }
        constant = readSignedValue(code, offset, constantSize);
    }
    protected void writeInfo(byte[] code, int offset)
    {
        int variableIndexSize = variableIndexSize();
        int constantSize      = constantSize();
        if (requiredVariableIndexSize() > variableIndexSize)
        {
            throw new IllegalArgumentException("Instruction has invalid variable index size ("+this.toString(offset)+")");
        }
        if (requiredConstantSize() > constantSize)
        {
            throw new IllegalArgumentException("Instruction has invalid constant size ("+this.toString(offset)+")");
        }
        writeValue(code, offset, variableIndex, variableIndexSize); offset += variableIndexSize;
        writeSignedValue(code, offset, constant, constantSize);
    }
    public int length(int offset)
    {
        return (wide ? 2 : 1) + variableIndexSize() + constantSize();
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitVariableInstruction(clazz, method, codeAttribute, offset, this);
    }
    public String toString()
    {
        return getName() +
               (wide ? "_w" : "") +
               " v"+variableIndex +
               (constantSize() > 0 ? ", "+constant : "");
    }
    private int variableIndexSize()
    {
        return (opcode >= InstructionConstants.OP_ILOAD_0 &&
                opcode <= InstructionConstants.OP_ALOAD_3) ||
               (opcode >= InstructionConstants.OP_ISTORE_0 &&
                opcode <= InstructionConstants.OP_ASTORE_3) ? 0 :
               wide                                         ? 2 :
                                                              1;
    }
    private int requiredVariableIndexSize()
    {
        return (variableIndex &    0x3) == variableIndex ? 0 :
               (variableIndex &   0xff) == variableIndex ? 1 :
               (variableIndex & 0xffff) == variableIndex ? 2 :
                                                           4;
    }
    private int constantSize()
    {
        return opcode != InstructionConstants.OP_IINC ? 0 :
               wide                                   ? 2 :
                                                        1;
    }
    private int requiredConstantSize()
    {
        return opcode != InstructionConstants.OP_IINC ? 0 :
               constant << 24 >> 24 == constant       ? 1 :
               constant << 16 >> 16 == constant       ? 2 :
                                                        4;
    }
}
