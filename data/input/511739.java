import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.RefConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
public class ExceptionInstructionChecker
extends      SimplifiedVisitor
implements   InstructionVisitor
{
    private boolean mayThrowExceptions;
    public boolean mayThrowExceptions(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction)
    {
        mayThrowExceptions = false;
        instruction.accept(clazz, method,  codeAttribute, offset, this);
        return mayThrowExceptions;
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitSimpleInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SimpleInstruction simpleInstruction)
    {
        byte opcode = simpleInstruction.opcode;
        if (opcode == InstructionConstants.OP_IDIV         ||
            opcode == InstructionConstants.OP_LDIV         ||
            opcode == InstructionConstants.OP_IREM         ||
            opcode == InstructionConstants.OP_LREM         ||
            opcode == InstructionConstants.OP_IALOAD       ||
            opcode == InstructionConstants.OP_LALOAD       ||
            opcode == InstructionConstants.OP_FALOAD       ||
            opcode == InstructionConstants.OP_DALOAD       ||
            opcode == InstructionConstants.OP_AALOAD       ||
            opcode == InstructionConstants.OP_BALOAD       ||
            opcode == InstructionConstants.OP_CALOAD       ||
            opcode == InstructionConstants.OP_SALOAD       ||
            opcode == InstructionConstants.OP_IASTORE      ||
            opcode == InstructionConstants.OP_LASTORE      ||
            opcode == InstructionConstants.OP_FASTORE      ||
            opcode == InstructionConstants.OP_DASTORE      ||
            opcode == InstructionConstants.OP_AASTORE      ||
            opcode == InstructionConstants.OP_BASTORE      ||
            opcode == InstructionConstants.OP_CASTORE      ||
            opcode == InstructionConstants.OP_SASTORE      ||
            opcode == InstructionConstants.OP_NEWARRAY     ||
            opcode == InstructionConstants.OP_ARRAYLENGTH  ||
            opcode == InstructionConstants.OP_ATHROW       ||
            opcode == InstructionConstants.OP_MONITORENTER ||
            opcode == InstructionConstants.OP_MONITOREXIT)
        {
            mayThrowExceptions = true;
        }
    }
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        byte opcode = constantInstruction.opcode;
        if (opcode == InstructionConstants.OP_GETSTATIC       ||
            opcode == InstructionConstants.OP_PUTSTATIC       ||
            opcode == InstructionConstants.OP_GETFIELD        ||
            opcode == InstructionConstants.OP_PUTFIELD        ||
            opcode == InstructionConstants.OP_INVOKEVIRTUAL   ||
            opcode == InstructionConstants.OP_INVOKESPECIAL   ||
            opcode == InstructionConstants.OP_INVOKESTATIC    ||
            opcode == InstructionConstants.OP_INVOKEINTERFACE ||
            opcode == InstructionConstants.OP_NEW             ||
            opcode == InstructionConstants.OP_ANEWARRAY       ||
            opcode == InstructionConstants.OP_CHECKCAST       ||
            opcode == InstructionConstants.OP_MULTIANEWARRAY)
        {
            mayThrowExceptions = true;
        }
    }
}
