import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.MemberVisitor;
public class ReadWriteFieldMarker
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor,
             MemberVisitor
{
    private boolean reading = true;
    private boolean writing = true;
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        byte opcode = constantInstruction.opcode;
        switch (opcode)
        {
            case InstructionConstants.OP_LDC:
            case InstructionConstants.OP_LDC_W:
                reading = true;
                writing = true;
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                break;
            case InstructionConstants.OP_GETSTATIC:
            case InstructionConstants.OP_GETFIELD:
                reading = true;
                writing = false;
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                break;
            case InstructionConstants.OP_PUTSTATIC:
            case InstructionConstants.OP_PUTFIELD:
                reading = false;
                writing = true;
                clazz.constantPoolEntryAccept(constantInstruction.constantIndex, this);
                break;
        }
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.referencedMemberAccept(this);
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        fieldrefConstant.referencedMemberAccept(this);
    }
    public void visitAnyMember(Clazz Clazz, Member member) {}
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (reading)
        {
            markAsRead(programField);
        }
        if (writing)
        {
            markAsWritten(programField);
        }
    }
    private static void markAsRead(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        if (info != null)
        {
            info.setRead();
        }
    }
    public static boolean isRead(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        return info == null ||
               info.isRead();
    }
    private static void markAsWritten(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        if (info != null)
        {
            info.setWritten();
        }
    }
    public static boolean isWritten(Field field)
    {
        FieldOptimizationInfo info = FieldOptimizationInfo.getFieldOptimizationInfo(field);
        return info == null ||
               info.isWritten();
    }
}
