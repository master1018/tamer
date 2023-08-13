package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class MemberReferenceFixer
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             MemberVisitor,
             AttributeVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private static final boolean DEBUG = false;
    private final StackSizeUpdater stackSizeUpdater = new StackSizeUpdater();
    private int constantIndex;
    private boolean isInterfaceMethod;
    private boolean stackSizesMayHaveChanged;
    public void visitProgramClass(ProgramClass programClass)
    {
        stackSizesMayHaveChanged = false;
        for (int index = 1; index < programClass.u2constantPoolCount; index++)
        {
            Constant constant = programClass.constantPool[index];
            if (constant != null)
            {
                this.constantIndex = index;
                constant.accept(programClass, this);
            }
        }
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        Member referencedMember = stringConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = stringConstant.referencedClass;
            String newName = referencedMember.getName(referencedClass);
            if (!stringConstant.getString(clazz).equals(newName))
            {
                if (DEBUG)
                {
                    debug(clazz, stringConstant, referencedClass, referencedMember);
                }
                stringConstant.u2stringIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newName);
            }
        }
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        Member referencedMember = fieldrefConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = fieldrefConstant.referencedClass;
            String newName = referencedMember.getName(referencedClass);
            String newType = referencedMember.getDescriptor(referencedClass);
            if (!fieldrefConstant.getName(clazz).equals(newName) ||
                !fieldrefConstant.getType(clazz).equals(newType))
            {
                if (DEBUG)
                {
                    debug(clazz, fieldrefConstant, referencedClass, referencedMember);
                }
                fieldrefConstant.u2nameAndTypeIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addNameAndTypeConstant(newName, newType);
            }
        }
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        Member referencedMember = interfaceMethodrefConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = interfaceMethodrefConstant.referencedClass;
            String newName = referencedMember.getName(referencedClass);
            String newType = referencedMember.getDescriptor(referencedClass);
            if (!interfaceMethodrefConstant.getName(clazz).equals(newName) ||
                !interfaceMethodrefConstant.getType(clazz).equals(newType))
            {
                if (DEBUG)
                {
                    debug(clazz, interfaceMethodrefConstant, referencedClass, referencedMember);
                }
                interfaceMethodrefConstant.u2nameAndTypeIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addNameAndTypeConstant(newName, newType);
                stackSizesMayHaveChanged = true;
            }
            isInterfaceMethod = true;
            clazz.constantPoolEntryAccept(interfaceMethodrefConstant.u2classIndex, this);
            if (!isInterfaceMethod)
            {
                if (DEBUG)
                {
                    System.out.println("MemberReferenceFixer:");
                    System.out.println("  Class file     = "+clazz.getName());
                    System.out.println("  Ref class      = "+referencedClass.getName());
                    System.out.println("  Ref method     = "+interfaceMethodrefConstant.getName(clazz)+interfaceMethodrefConstant.getType(clazz));
                    System.out.println("    -> ordinary method");
                }
                ((ProgramClass)clazz).constantPool[this.constantIndex] =
                    new MethodrefConstant(interfaceMethodrefConstant.u2classIndex,
                                          interfaceMethodrefConstant.u2nameAndTypeIndex,
                                          referencedClass,
                                          referencedMember);
            }
        }
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        Member referencedMember = methodrefConstant.referencedMember;
        if (referencedMember != null)
        {
            Clazz referencedClass = methodrefConstant.referencedClass;
            String newName = referencedMember.getName(referencedClass);
            String newType = referencedMember.getDescriptor(referencedClass);
            if (!methodrefConstant.getName(clazz).equals(newName) ||
                !methodrefConstant.getType(clazz).equals(newType))
            {
                if (DEBUG)
                {
                    debug(clazz, methodrefConstant, referencedClass, referencedMember);
                }
                methodrefConstant.u2nameAndTypeIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addNameAndTypeConstant(newName, newType);
                stackSizesMayHaveChanged = true;
            }
            isInterfaceMethod = false;
            clazz.constantPoolEntryAccept(methodrefConstant.u2classIndex, this);
            if (isInterfaceMethod)
            {
                if (DEBUG)
                {
                    System.out.println("MemberReferenceFixer:");
                    System.out.println("  Class file     = "+clazz.getName());
                    System.out.println("  Ref class      = "+referencedClass.getName());
                    System.out.println("  Ref method     = "+methodrefConstant.getName(clazz)+methodrefConstant.getType(clazz));
                    System.out.println("    -> interface method");
                }
                ((ProgramClass)clazz).constantPool[this.constantIndex] =
                    new InterfaceMethodrefConstant(methodrefConstant.u2classIndex,
                                                   methodrefConstant.u2nameAndTypeIndex,
                                                   referencedClass,
                                                   referencedMember);
            }
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        if (ClassUtil.isInternalArrayType(classConstant.getName(clazz)))
        {
            isInterfaceMethod = false;
        }
        else
        {
            Clazz referencedClass = classConstant.referencedClass;
            if (referencedClass != null)
            {
                isInterfaceMethod = (referencedClass.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) != 0;
            }
        }
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.attributesAccept(programClass, this);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        Member referencedMember = enclosingMethodAttribute.referencedMethod;
        if (referencedMember != null)
        {
            Clazz referencedClass = enclosingMethodAttribute.referencedClass;
            if (!enclosingMethodAttribute.getClassName(clazz).equals(referencedClass.getName()))
            {
                enclosingMethodAttribute.u2classIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addClassConstant(referencedClass);
            }
            if (!enclosingMethodAttribute.getName(clazz).equals(referencedMember.getName(referencedClass)) ||
                !enclosingMethodAttribute.getType(clazz).equals(referencedMember.getDescriptor(referencedClass)))
            {
                enclosingMethodAttribute.u2nameAndTypeIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addNameAndTypeConstant(referencedMember.getName(referencedClass),
                                                                                       referencedMember.getDescriptor(referencedClass));
            }
        }
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (stackSizesMayHaveChanged)
        {
                stackSizeUpdater.visitCodeAttribute(clazz, method, codeAttribute);
        }
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute.annotationsAccept(clazz, this);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        fixElementValue(clazz, annotation, constantElementValue);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        fixElementValue(clazz, annotation, enumConstantElementValue);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        fixElementValue(clazz, annotation, classElementValue);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        fixElementValue(clazz, annotation, annotationElementValue);
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        fixElementValue(clazz, annotation, arrayElementValue);
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private void fixElementValue(Clazz        clazz,
                                 Annotation   annotation,
                                 ElementValue elementValue)
    {
        Member referencedMember = elementValue.referencedMethod;
        if (referencedMember != null)
        {
            String methodName    = elementValue.getMethodName(clazz);
            String newMethodName = referencedMember.getName(elementValue.referencedClass);
            if (!methodName.equals(newMethodName))
            {
                elementValue.u2elementNameIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newMethodName);
            }
        }
    }
    private void debug(Clazz          clazz,
                       StringConstant stringConstant,
                       Clazz          referencedClass,
                       Member         referencedMember)
    {
        System.out.println("MemberReferenceFixer:");
        System.out.println("  Class file      = "+clazz.getName());
        System.out.println("  Ref class       = "+referencedClass.getName());
        System.out.println("  Ref member name = "+stringConstant.getString(clazz));
        System.out.println("                 -> "+referencedMember.getName(referencedClass));
    }
    private void debug(Clazz       clazz,
                       RefConstant refConstant,
                       Clazz       referencedClass,
                       Member      referencedMember)
    {
        System.out.println("MemberReferenceFixer:");
        System.out.println("  Class file      = "+clazz.getName());
        System.out.println("  Ref class       = "+referencedClass.getName());
        System.out.println("  Ref member name = "+refConstant.getName(clazz));
        System.out.println("                 -> "+referencedMember.getName(referencedClass));
        System.out.println("  Ref descriptor  = "+refConstant.getType(clazz));
        System.out.println("                 -> "+referencedMember.getDescriptor(referencedClass));
    }
}
