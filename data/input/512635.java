package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.CodeAttribute;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class DotClassClassVisitor
extends      SimplifiedVisitor
implements   InstructionVisitor,
             ConstantVisitor
{
    private final ClassVisitor classVisitor;
    public DotClassClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        byte opcode = constantInstruction.opcode;
        if (opcode == InstructionConstants.OP_LDC ||
            opcode == InstructionConstants.OP_LDC_W)
        {
            clazz.constantPoolEntryAccept(constantInstruction.constantIndex,
                                          this);
        }
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClassAccept(classVisitor);
    }
}
