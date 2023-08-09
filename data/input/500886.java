package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class ClassReferenceFixer
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             MemberVisitor,
             AttributeVisitor,
             InnerClassesInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final boolean ensureUniqueMemberNames;
    public ClassReferenceFixer(boolean ensureUniqueMemberNames)
    {
        this.ensureUniqueMemberNames = ensureUniqueMemberNames;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldsAccept(this);
        libraryClass.methodsAccept(this);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        String descriptor    = programField.getDescriptor(programClass);
        String newDescriptor = newDescriptor(descriptor,
                                             programField.referencedClass);
        if (!descriptor.equals(newDescriptor))
        {
            ConstantPoolEditor constantPoolEditor =
                new ConstantPoolEditor(programClass);
            programField.u2descriptorIndex =
                constantPoolEditor.addUtf8Constant(newDescriptor);
            if (ensureUniqueMemberNames)
            {
                String name    = programField.getName(programClass);
                String newName = newUniqueMemberName(name, descriptor);
                programField.u2nameIndex =
                    constantPoolEditor.addUtf8Constant(newName);
            }
        }
        programField.attributesAccept(programClass, this);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        String descriptor    = programMethod.getDescriptor(programClass);
        String newDescriptor = newDescriptor(descriptor,
                                             programMethod.referencedClasses);
        if (!descriptor.equals(newDescriptor))
        {
            ConstantPoolEditor constantPoolEditor =
                new ConstantPoolEditor(programClass);
            programMethod.u2descriptorIndex =
                constantPoolEditor.addUtf8Constant(newDescriptor);
            if (ensureUniqueMemberNames)
            {
                String name    = programMethod.getName(programClass);
                String newName = newUniqueMemberName(name, descriptor);
                programMethod.u2nameIndex =
                    constantPoolEditor.addUtf8Constant(newName);
            }
        }
        programMethod.attributesAccept(programClass, this);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        String descriptor    = libraryField.getDescriptor(libraryClass);
        String newDescriptor = newDescriptor(descriptor,
                                             libraryField.referencedClass);
        libraryField.descriptor = newDescriptor;
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        String descriptor    = libraryMethod.getDescriptor(libraryClass);
        String newDescriptor = newDescriptor(descriptor,
                                             libraryMethod.referencedClasses);
        libraryMethod.descriptor = newDescriptor;
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        Clazz  referencedClass  = stringConstant.referencedClass;
        Member referencedMember = stringConstant.referencedMember;
        if (referencedClass  != null &&
            referencedMember == null)
        {
            String externalClassName    = stringConstant.getString(clazz);
            String internalClassName    = ClassUtil.internalClassName(externalClassName);
            String newInternalClassName = newClassName(internalClassName,
                                                       referencedClass);
            if (!newInternalClassName.equals(internalClassName))
            {
                String newExternalClassName = ClassUtil.externalClassName(newInternalClassName);
                stringConstant.u2stringIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newExternalClassName);
            }
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        Clazz referencedClass = classConstant.referencedClass;
        if (referencedClass != null)
        {
            String className    = classConstant.getName(clazz);
            String newClassName = newClassName(className, referencedClass);
            if (!className.equals(newClassName))
            {
                classConstant.u2nameIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newClassName);
            }
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        String signature    = clazz.getString(signatureAttribute.u2signatureIndex);
        String newSignature = newDescriptor(signature,
                                            signatureAttribute.referencedClasses);
        if (!signature.equals(newSignature))
        {
            signatureAttribute.u2signatureIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSignature);
        }
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
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        int innerClassIndex = innerClassesInfo.u2innerClassIndex;
        int innerNameIndex  = innerClassesInfo.u2innerNameIndex;
        if (innerClassIndex != 0 &&
            innerNameIndex  != 0)
        {
            String newInnerName = clazz.getClassName(innerClassIndex);
            int index = newInnerName.lastIndexOf(ClassConstants.INTERNAL_INNER_CLASS_SEPARATOR);
            if (index >= 0)
            {
                innerClassesInfo.u2innerNameIndex =
                    new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newInnerName.substring(index + 1));
            }
        }
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        String descriptor    = clazz.getString(localVariableInfo.u2descriptorIndex);
        String newDescriptor = newDescriptor(descriptor,
                                             localVariableInfo.referencedClass);
        if (!descriptor.equals(newDescriptor))
        {
            localVariableInfo.u2descriptorIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newDescriptor);
        }
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        String signature    = clazz.getString(localVariableTypeInfo.u2signatureIndex);
        String newSignature = newDescriptor(signature,
                                            localVariableTypeInfo.referencedClasses);
        if (!signature.equals(newSignature))
        {
            localVariableTypeInfo.u2signatureIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newSignature);
        }
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        String typeName    = clazz.getString(annotation.u2typeIndex);
        String newTypeName = newDescriptor(typeName,
                                           annotation.referencedClasses);
        if (!typeName.equals(newTypeName))
        {
            annotation.u2typeIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newTypeName);
        }
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        String typeName    = clazz.getString(enumConstantElementValue.u2typeNameIndex);
        String newTypeName = newDescriptor(typeName,
                                           enumConstantElementValue.referencedClasses);
        if (!typeName.equals(newTypeName))
        {
            enumConstantElementValue.u2typeNameIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newTypeName);
        }
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        String className    = clazz.getString(classElementValue.u2classInfoIndex);
        String newClassName = newDescriptor(className,
                                            classElementValue.referencedClasses);
        if (!className.equals(newClassName))
        {
            classElementValue.u2classInfoIndex =
                new ConstantPoolEditor((ProgramClass)clazz).addUtf8Constant(newClassName);
        }
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private static String newDescriptor(String descriptor,
                                        Clazz  referencedClass)
    {
        if (referencedClass == null)
        {
            return descriptor;
        }
        DescriptorClassEnumeration descriptorClassEnumeration =
            new DescriptorClassEnumeration(descriptor);
        StringBuffer newDescriptorBuffer = new StringBuffer(descriptor.length());
        newDescriptorBuffer.append(descriptorClassEnumeration.nextFluff());
        if (descriptorClassEnumeration.hasMoreClassNames())
        {
            String className = descriptorClassEnumeration.nextClassName();
            String fluff     = descriptorClassEnumeration.nextFluff();
            String newClassName = newClassName(className,
                                               referencedClass);
            newDescriptorBuffer.append(newClassName);
            newDescriptorBuffer.append(fluff);
        }
        return newDescriptorBuffer.toString();
    }
    private static String newDescriptor(String  descriptor,
                                        Clazz[] referencedClasses)
    {
        if (referencedClasses == null ||
            referencedClasses.length == 0)
        {
            return descriptor;
        }
        DescriptorClassEnumeration descriptorClassEnumeration =
            new DescriptorClassEnumeration(descriptor);
        StringBuffer newDescriptorBuffer = new StringBuffer(descriptor.length());
        newDescriptorBuffer.append(descriptorClassEnumeration.nextFluff());
        int index = 0;
        while (descriptorClassEnumeration.hasMoreClassNames())
        {
            String  className        = descriptorClassEnumeration.nextClassName();
            boolean isInnerClassName = descriptorClassEnumeration.isInnerClassName();
            String  fluff            = descriptorClassEnumeration.nextFluff();
            String newClassName = newClassName(className,
                                               referencedClasses[index++]);
            if (isInnerClassName)
            {
                newClassName =
                    newClassName.substring(newClassName.lastIndexOf(ClassConstants.INTERNAL_INNER_CLASS_SEPARATOR)+1);
            }
            newDescriptorBuffer.append(newClassName);
            newDescriptorBuffer.append(fluff);
        }
        return newDescriptorBuffer.toString();
    }
    private String newUniqueMemberName(String name, String descriptor)
    {
        return name.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ?
            ClassConstants.INTERNAL_METHOD_NAME_INIT :
            name + ClassConstants.SPECIAL_MEMBER_SEPARATOR + Long.toHexString(Math.abs((descriptor).hashCode()));
    }
    private static String newClassName(String className,
                                       Clazz  referencedClass)
    {
        if (referencedClass == null)
        {
            return className;
        }
        String newClassName = referencedClass.getName();
        if (className.charAt(0) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            newClassName =
                 className.substring(0, className.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_START)+1) +
                 newClassName +
                 ClassConstants.INTERNAL_TYPE_CLASS_END;
        }
        return newClassName;
    }
}
