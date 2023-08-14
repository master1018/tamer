package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class MethodInvocationFixer
extends      SimplifiedVisitor
implements   AttributeVisitor,
             InstructionVisitor,
             ConstantVisitor,
             ClassVisitor,
             MemberVisitor
{
    private static final boolean DEBUG = false;
    private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
    private Clazz  referencedClass;
    private Clazz  referencedMethodClass;
    private Member referencedMethod;
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        int constantIndex = constantInstruction.constantIndex;
        referencedMethod = null;
        clazz.constantPoolEntryAccept(constantIndex, this);
        if (referencedMethod != null)
        {
            byte opcode = constantInstruction.opcode;
            if ((referencedMethod.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) != 0)
            {
                if (opcode != InstructionConstants.OP_INVOKESTATIC)
                {
                    Instruction replacementInstruction =
                        new ConstantInstruction(InstructionConstants.OP_INVOKESTATIC,
                                                constantIndex).shrink();
                    codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
                    if (DEBUG)
                    {
                        debug(clazz, method, offset, constantInstruction, replacementInstruction);
                    }
                }
            }
            else if ((referencedMethod.getAccessFlags() & ClassConstants.INTERNAL_ACC_PRIVATE) != 0 ||
                     referencedMethod.getName(referencedMethodClass).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
            {
                if (opcode != InstructionConstants.OP_INVOKESPECIAL)
                {
                    Instruction replacementInstruction =
                        new ConstantInstruction(InstructionConstants.OP_INVOKESPECIAL,
                                                constantIndex).shrink();
                    codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
                    if (DEBUG)
                    {
                        debug(clazz, method, offset, constantInstruction, replacementInstruction);
                    }
                }
            }
            else if ((referencedClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) != 0)
            {
                int invokeinterfaceConstant =
                    (ClassUtil.internalMethodParameterSize(referencedMethod.getDescriptor(referencedMethodClass), false)) << 8;
                if (opcode != InstructionConstants.OP_INVOKEINTERFACE ||
                    constantInstruction.constant != invokeinterfaceConstant)
                {
                    Instruction replacementInstruction =
                        new ConstantInstruction(InstructionConstants.OP_INVOKEINTERFACE,
                                                constantIndex,
                                                invokeinterfaceConstant).shrink();
                    codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
                    if (DEBUG)
                    {
                        debug(clazz, method, offset, constantInstruction, replacementInstruction);
                    }
                }
            }
            else
            {
                if (opcode != InstructionConstants.OP_INVOKEVIRTUAL &&
                    (opcode != InstructionConstants.OP_INVOKESPECIAL ||
                     !clazz.extends_(referencedClass)))
                {
                    Instruction replacementInstruction =
                        new ConstantInstruction(InstructionConstants.OP_INVOKEVIRTUAL,
                                                constantIndex).shrink();
                    codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
                    if (DEBUG)
                    {
                        debug(clazz, method, offset, constantInstruction, replacementInstruction);
                    }
                }
            }
        }
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitAnyMethodrefConstant(Clazz clazz, RefConstant refConstant)
    {
        clazz.constantPoolEntryAccept(refConstant.u2classIndex, this);
        refConstant.referencedMemberAccept(this);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
       classConstant.referencedClassAccept(this);
    }
    public void visitAnyClass(Clazz clazz)
    {
        referencedClass = clazz;
    }
    public void visitAnyMember(Clazz clazz, Member member)
    {
        referencedMethodClass = clazz;
        referencedMethod      = member;
    }
    private void debug(Clazz               clazz,
                       Method              method,
                       int                 offset,
                       ConstantInstruction constantInstruction,
                       Instruction         replacementInstruction)
    {
        System.out.println("MethodInvocationFixer:");
        System.out.println("  Class       = "+clazz.getName());
        System.out.println("  Method      = "+method.getName(clazz)+method.getDescriptor(clazz));
        System.out.println("  Instruction = "+constantInstruction.toString(offset));
        System.out.println("  -> Class    = "+referencedClass);
        System.out.println("     Method   = "+referencedMethod);
        if ((referencedClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) != 0)
        {
            System.out.println("     Parameter size   = "+(ClassUtil.internalMethodParameterSize(referencedMethod.getDescriptor(referencedMethodClass), false)));
        }
        System.out.println("  Replacement instruction = "+replacementInstruction.toString(offset));
    }
}
