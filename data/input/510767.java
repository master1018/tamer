package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public class SimpleInstruction extends Instruction
{
    public int constant;
    public SimpleInstruction() {}
    public SimpleInstruction(byte opcode)
    {
        this(opcode, embeddedConstant(opcode));
    }
    public SimpleInstruction(byte opcode, int constant)
    {
        this.opcode   = opcode;
        this.constant = constant;
    }
    public SimpleInstruction copy(SimpleInstruction simpleInstruction)
    {
        this.opcode   = simpleInstruction.opcode;
        this.constant = simpleInstruction.constant;
        return this;
    }
    private static int embeddedConstant(byte opcode)
    {
        switch (opcode)
        {
            case InstructionConstants.OP_ICONST_M1: return -1;
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_LCONST_1:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_DCONST_1: return 1;
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_FCONST_2: return 2;
            case InstructionConstants.OP_ICONST_3: return 3;
            case InstructionConstants.OP_ICONST_4: return 4;
            case InstructionConstants.OP_ICONST_5: return 5;
            default: return 0;
        }
    }
    public byte canonicalOpcode()
    {
        switch (opcode)
        {
            case InstructionConstants.OP_ICONST_M1:
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_ICONST_3:
            case InstructionConstants.OP_ICONST_4:
            case InstructionConstants.OP_ICONST_5:
            case InstructionConstants.OP_BIPUSH:
            case InstructionConstants.OP_SIPUSH:   return InstructionConstants.OP_ICONST_0;
            case InstructionConstants.OP_LCONST_0:
            case InstructionConstants.OP_LCONST_1: return InstructionConstants.OP_LCONST_0;
            case InstructionConstants.OP_FCONST_0:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_FCONST_2: return InstructionConstants.OP_FCONST_0;
            case InstructionConstants.OP_DCONST_0:
            case InstructionConstants.OP_DCONST_1: return InstructionConstants.OP_DCONST_0;
            default: return opcode;
        }
    }
    public Instruction shrink()
    {
        switch (opcode)
        {
            case InstructionConstants.OP_ICONST_M1:
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
            case InstructionConstants.OP_ICONST_2:
            case InstructionConstants.OP_ICONST_3:
            case InstructionConstants.OP_ICONST_4:
            case InstructionConstants.OP_ICONST_5:
            case InstructionConstants.OP_BIPUSH:
            case InstructionConstants.OP_SIPUSH:
                switch (requiredConstantSize())
                {
                    case 0:
                        opcode = (byte)(InstructionConstants.OP_ICONST_0 + constant);
                        break;
                    case 1:
                        opcode = InstructionConstants.OP_BIPUSH;
                        break;
                    case 2:
                        opcode = InstructionConstants.OP_SIPUSH;
                        break;
                }
                break;
            case InstructionConstants.OP_LCONST_0:
            case InstructionConstants.OP_LCONST_1:
                opcode = (byte)(InstructionConstants.OP_LCONST_0 + constant);
                break;
            case InstructionConstants.OP_FCONST_0:
            case InstructionConstants.OP_FCONST_1:
            case InstructionConstants.OP_FCONST_2:
                opcode = (byte)(InstructionConstants.OP_FCONST_0 + constant);
                break;
            case InstructionConstants.OP_DCONST_0:
            case InstructionConstants.OP_DCONST_1:
                opcode = (byte)(InstructionConstants.OP_DCONST_0 + constant);
                break;
        }
        return this;
    }
    protected void readInfo(byte[] code, int offset)
    {
        int constantSize = constantSize();
        constant = constantSize == 0 ?
            embeddedConstant(opcode) :
            readSignedValue(code, offset, constantSize);
    }
    protected void writeInfo(byte[] code, int offset)
    {
        int constantSize = constantSize();
        if (requiredConstantSize() > constantSize)
        {
            throw new IllegalArgumentException("Instruction has invalid constant size ("+this.toString(offset)+")");
        }
        writeSignedValue(code, offset, constant, constantSize);
    }
    public int length(int offset)
    {
        return 1 + constantSize();
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitSimpleInstruction(clazz, method, codeAttribute, offset, this);
    }
    public String toString()
    {
        return getName() +
               (constantSize() > 0 ? " "+constant : "");
    }
    private int constantSize()
    {
        return opcode == InstructionConstants.OP_BIPUSH ||
               opcode == InstructionConstants.OP_NEWARRAY ? 1 :
               opcode == InstructionConstants.OP_SIPUSH   ? 2 :
                                                            0;
    }
    private int requiredConstantSize()
    {
        return constant >= -1 && constant <= 5  ? 0 :
               constant << 24 >> 24 == constant ? 1 :
               constant << 16 >> 16 == constant ? 2 :
                                                  4;
    }
}
