package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.instruction.visitor.InstructionVisitor;
public abstract class Instruction
{
    private static final boolean[] IS_CATEGORY2 = new boolean[]
    {
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        false, 
        false, 
        false, 
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        true,  
        true,  
        true,  
        false, 
        false, 
        false, 
        true,  
        false, 
        false, 
        true,  
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        true,  
        false, 
        true,  
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
        false, 
    };
    private static final int[] STACK_POP_COUNTS = new int[]
    {
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        1, 
        3, 
        4, 
        3, 
        4, 
        3, 
        3, 
        3, 
        3, 
        1, 
        2, 
        1, 
        2, 
        3, 
        2, 
        3, 
        4, 
        2, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        1, 
        2, 
        1, 
        2, 
        2, 
        3, 
        2, 
        3, 
        2, 
        3, 
        2, 
        4, 
        2, 
        4, 
        2, 
        4, 
        0, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        4, 
        2, 
        2, 
        4, 
        4, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        2, 
        0, 
        0, 
        0, 
        1, 
        1, 
        1, 
        2, 
        1, 
        2, 
        1, 
        0, 
        0, 
        0, 
        1, 
        1, 
        1, 
        1, 
        0, 
        1, 
        0, 
        0, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        0, 
        0, 
        1, 
        1, 
        0, 
        0, 
    };
    private static final int[] STACK_PUSH_COUNTS = new int[]
    {
        0, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        1, 
        1, 
        1, 
        2, 
        2, 
        1, 
        1, 
        1, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        1, 
        2, 
        2, 
        2, 
        2, 
        1, 
        1, 
        1, 
        1, 
        1, 
        2, 
        1, 
        2, 
        1, 
        1, 
        1, 
        1, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        2, 
        3, 
        4, 
        4, 
        5, 
        6, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        1, 
        2, 
        0, 
        2, 
        1, 
        2, 
        1, 
        1, 
        2, 
        1, 
        2, 
        2, 
        1, 
        2, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        1, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        1, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        0, 
        1, 
        1, 
        1, 
        1, 
        0, 
        1, 
        1, 
        0, 
        0, 
        0, 
        1, 
        0, 
        0, 
        0, 
        1, 
    };
    public byte opcode;
    public byte canonicalOpcode()
    {
        return opcode;
    }
    public abstract Instruction shrink();
    public final void write(CodeAttribute codeAttribute, int offset)
    {
        write(codeAttribute.code, offset);
    }
    public void write(byte[] code, int offset)
    {
        if (isWide())
        {
            code[offset++] = InstructionConstants.OP_WIDE;
        }
        code[offset++] = opcode;
        writeInfo(code, offset);
    }
    protected boolean isWide()
    {
        return false;
    }
    protected abstract void readInfo(byte[] code, int offset);
    protected abstract void writeInfo(byte[] code, int offset);
    public abstract int length(int offset);
    public abstract void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor);
    public String toString(int offset)
    {
        return "["+offset+"] "+ this.toString();
    }
    public String getName()
    {
        return InstructionConstants.NAMES[opcode & 0xff];
    }
    public boolean isCategory2()
    {
        return IS_CATEGORY2[opcode & 0xff];
    }
    public int stackPopCount(Clazz clazz)
    {
        return STACK_POP_COUNTS[opcode & 0xff];
    }
    public int stackPushCount(Clazz clazz)
    {
        return STACK_PUSH_COUNTS[opcode & 0xff];
    }
    protected static int readByte(byte[] code, int offset)
    {
        return code[offset] & 0xff;
    }
    protected static int readShort(byte[] code, int offset)
    {
        return ((code[offset++] & 0xff) << 8) |
               ( code[offset  ] & 0xff      );
    }
    protected static int readInt(byte[] code, int offset)
    {
        return ( code[offset++]         << 24) |
               ((code[offset++] & 0xff) << 16) |
               ((code[offset++] & 0xff) <<  8) |
               ( code[offset  ] & 0xff       );
    }
    protected static int readValue(byte[] code, int offset, int valueSize)
    {
        switch (valueSize)
        {
            case 0: return 0;
            case 1: return readByte( code, offset);
            case 2: return readShort(code, offset);
            case 4: return readInt(  code, offset);
            default: throw new IllegalArgumentException("Unsupported value size ["+valueSize+"]");
        }
    }
    protected static int readSignedByte(byte[] code, int offset)
    {
        return code[offset];
    }
    protected static int readSignedShort(byte[] code, int offset)
    {
        return (code[offset++] <<   8) |
               (code[offset  ] & 0xff);
    }
    protected static int readSignedValue(byte[] code, int offset, int valueSize)
    {
        switch (valueSize)
        {
            case 0: return 0;
            case 1: return readSignedByte( code, offset);
            case 2: return readSignedShort(code, offset);
            case 4: return readInt(        code, offset);
            default: throw new IllegalArgumentException("Unsupported value size ["+valueSize+"]");
        }
    }
    protected static void writeByte(byte[] code, int offset, int value)
    {
        if (value > 0xff)
        {
            throw new IllegalArgumentException("Unsigned byte value larger than 0xff ["+value+"]");
        }
        code[offset] = (byte)value;
    }
    protected static void writeShort(byte[] code, int offset, int value)
    {
        if (value > 0xffff)
        {
            throw new IllegalArgumentException("Unsigned short value larger than 0xffff ["+value+"]");
        }
        code[offset++] = (byte)(value >> 8);
        code[offset  ] = (byte)(value     );
    }
    protected static void writeInt(byte[] code, int offset, int value)
    {
        code[offset++] = (byte)(value >> 24);
        code[offset++] = (byte)(value >> 16);
        code[offset++] = (byte)(value >>  8);
        code[offset  ] = (byte)(value      );
    }
    protected static void writeValue(byte[] code, int offset, int value, int valueSize)
    {
        switch (valueSize)
        {
            case 0:                                  break;
            case 1: writeByte( code, offset, value); break;
            case 2: writeShort(code, offset, value); break;
            case 4: writeInt(  code, offset, value); break;
            default: throw new IllegalArgumentException("Unsupported value size ["+valueSize+"]");
        }
    }
    protected static void writeSignedByte(byte[] code, int offset, int value)
    {
        if (value << 24 >> 24 != value)
        {
            throw new IllegalArgumentException("Signed byte value out of range ["+value+"]");
        }
        code[offset] = (byte)value;
    }
    protected static void writeSignedShort(byte[] code, int offset, int value)
    {
        if (value << 16 >> 16 != value)
        {
            throw new IllegalArgumentException("Signed short value out of range ["+value+"]");
        }
        code[offset++] = (byte)(value >> 8);
        code[offset  ] = (byte)(value     );
    }
    protected static void writeSignedValue(byte[] code, int offset, int value, int valueSize)
    {
        switch (valueSize)
        {
            case 0:                                        break;
            case 1: writeSignedByte( code, offset, value); break;
            case 2: writeSignedShort(code, offset, value); break;
            case 4: writeInt(        code, offset, value); break;
            default: throw new IllegalArgumentException("Unsupported value size ["+valueSize+"]");
        }
    }
}
