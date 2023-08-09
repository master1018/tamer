package proguard.classfile.instruction;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.ClassUtil;
public class ConstantInstruction extends Instruction
implements   ConstantVisitor
{
    public int constantIndex;
    public int constant;
    private int parameterStackDelta;
    private int typeStackDelta;
    public ConstantInstruction() {}
    public ConstantInstruction(byte opcode, int constantIndex)
    {
        this(opcode, constantIndex, 0);
    }
    public ConstantInstruction(byte opcode, int constantIndex, int constant)
    {
        this.opcode        = opcode;
        this.constantIndex = constantIndex;
        this.constant      = constant;
    }
    public ConstantInstruction copy(ConstantInstruction constantInstruction)
    {
        this.opcode        = constantInstruction.opcode;
        this.constantIndex = constantInstruction.constantIndex;
        this.constant      = constantInstruction.constant;
        return this;
    }
    public byte canonicalOpcode()
    {
        switch (opcode)
        {
            case InstructionConstants.OP_LDC_W:
            case InstructionConstants.OP_LDC2_W: return InstructionConstants.OP_LDC;
            default: return opcode;
        }
    }
    public Instruction shrink()
    {
        if (requiredConstantIndexSize() == 1)
        {
            if (opcode == InstructionConstants.OP_LDC_W)
            {
                opcode = InstructionConstants.OP_LDC;
            }
        }
        else
        {
            if (opcode == InstructionConstants.OP_LDC)
            {
                opcode = InstructionConstants.OP_LDC_W;
            }
        }
        return this;
    }
    protected void readInfo(byte[] code, int offset)
    {
        int constantIndexSize = constantIndexSize();
        int constantSize      = constantSize();
        constantIndex = readValue(code, offset, constantIndexSize);  offset += constantIndexSize;
        constant      = readValue(code, offset, constantSize);
    }
    protected void writeInfo(byte[] code, int offset)
    {
        int constantIndexSize = constantIndexSize();
        int constantSize      = constantSize();
        if (requiredConstantIndexSize() > constantIndexSize)
        {
            throw new IllegalArgumentException("Instruction has invalid constant index size ("+this.toString(offset)+")");
        }
        writeValue(code, offset, constantIndex, constantIndexSize); offset += constantIndexSize;
        writeValue(code, offset, constant,      constantSize);
    }
    public int length(int offset)
    {
        return 1 + constantIndexSize() + constantSize();
    }
    public void accept(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, InstructionVisitor instructionVisitor)
    {
        instructionVisitor.visitConstantInstruction(clazz, method, codeAttribute, offset, this);
    }
    public int stackPopCount(Clazz clazz)
    {
        int stackPopCount = super.stackPopCount(clazz);
        switch (opcode)
        {
            case InstructionConstants.OP_MULTIANEWARRAY:
                stackPopCount += constant;
                break;
            case InstructionConstants.OP_PUTSTATIC:
            case InstructionConstants.OP_PUTFIELD:
                clazz.constantPoolEntryAccept(constantIndex, this);
                stackPopCount += typeStackDelta;
                break;
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                clazz.constantPoolEntryAccept(constantIndex, this);
                stackPopCount += parameterStackDelta;
                break;
        }
        return stackPopCount;
    }
    public int stackPushCount(Clazz clazz)
    {
        int stackPushCount = super.stackPushCount(clazz);
        switch (opcode)
        {
            case InstructionConstants.OP_GETSTATIC:
            case InstructionConstants.OP_GETFIELD:
            case InstructionConstants.OP_INVOKEVIRTUAL:
            case InstructionConstants.OP_INVOKESPECIAL:
            case InstructionConstants.OP_INVOKESTATIC:
            case InstructionConstants.OP_INVOKEINTERFACE:
                clazz.constantPoolEntryAccept(constantIndex, this);
                stackPushCount += typeStackDelta;
                break;
        }
        return stackPushCount;
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant) {}
    public void visitLongConstant(Clazz clazz, LongConstant longConstant) {}
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant) {}
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant) {}
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant) {}
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant) {}
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant) {}
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        String type = fieldrefConstant.getType(clazz);
        typeStackDelta = ClassUtil.internalTypeSize(ClassUtil.internalMethodReturnType(type));
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        visitRefConstant(clazz, interfaceMethodrefConstant);
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        visitRefConstant(clazz, methodrefConstant);
    }
    private void visitRefConstant(Clazz clazz, RefConstant methodrefConstant)
    {
        String type = methodrefConstant.getType(clazz);
        parameterStackDelta = ClassUtil.internalMethodParameterSize(type);
        typeStackDelta      = ClassUtil.internalTypeSize(ClassUtil.internalMethodReturnType(type));
    }
    public String toString()
    {
        return getName()+" #"+constantIndex;
    }
    private int constantIndexSize()
    {
        return opcode == InstructionConstants.OP_LDC ? 1 :
                                                       2;
    }
    private int constantSize()
    {
        return opcode == InstructionConstants.OP_MULTIANEWARRAY  ? 1 :
               opcode == InstructionConstants.OP_INVOKEINTERFACE ? 2 :
                                                                   0;
    }
    private int requiredConstantIndexSize()
    {
        return (constantIndex &   0xff) == constantIndex ? 1 :
               (constantIndex & 0xffff) == constantIndex ? 2 :
                                                           4;
    }
}
