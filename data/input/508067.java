import proguard.classfile.*;
import proguard.classfile.editor.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class TargetClassChanger
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             MemberVisitor,
             AttributeVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    public void visitProgramClass(ProgramClass programClass)
    {
        Clazz   superClass       = null;
        Clazz[] interfaceClasses = null;
        programClass.constantPoolEntriesAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
        Clazz targetClass = ClassMerger.getTargetClass(programClass);
        if (targetClass != null)
        {
            programClass.u2thisClass =
                addNewClassConstant(programClass,
                                    programClass.getName(),
                                    programClass);
            programClass.subClasses = null;
        }
        int newInterfacesCount = 0;
        for (int index = 0; index < programClass.u2interfacesCount; index++)
        {
            Clazz interfaceClass = programClass.getInterface(index);
            if (!programClass.equals(interfaceClass))
            {
                programClass.u2interfaces[newInterfacesCount++] =
                    programClass.u2interfaces[index];
            }
        }
        programClass.u2interfacesCount = newInterfacesCount;
        ConstantVisitor subclassAdder =
            new ReferencedClassVisitor(
            new SubclassFilter(programClass,
            new SubclassAdder(programClass)));
        programClass.superClassConstantAccept(subclassAdder);
        programClass.interfaceConstantsAccept(subclassAdder);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldsAccept(this);
        libraryClass.methodsAccept(this);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        programField.referencedClass =
            updateReferencedClass(programField.referencedClass);
        programField.attributesAccept(programClass, this);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        updateReferencedClasses(programMethod.referencedClasses);
        programMethod.attributesAccept(programClass, this);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        libraryField.referencedClass =
            updateReferencedClass(libraryField.referencedClass);
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        updateReferencedClasses(libraryMethod.referencedClasses);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        Clazz referencedClass    = stringConstant.referencedClass;
        Clazz newReferencedClass = updateReferencedClass(referencedClass);
        if (referencedClass != newReferencedClass)
        {
            stringConstant.referencedClass = newReferencedClass;
            stringConstant.referencedMember =
                updateReferencedMember(stringConstant.referencedMember,
                                       stringConstant.getString(clazz),
                                       null,
                                       newReferencedClass);
        }
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        Clazz referencedClass    = refConstant.referencedClass;
        Clazz newReferencedClass = updateReferencedClass(referencedClass);
        if (referencedClass != newReferencedClass)
        {
            refConstant.referencedClass  = newReferencedClass;
            refConstant.referencedMember =
                updateReferencedMember(refConstant.referencedMember,
                                       refConstant.getName(clazz),
                                       refConstant.getType(clazz),
                                       newReferencedClass);
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClass =
            updateReferencedClass(classConstant.referencedClass);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
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
        updateReferencedClasses(signatureAttribute.referencedClasses);
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
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.referencedClass =
            updateReferencedClass(localVariableInfo.referencedClass);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        updateReferencedClasses(localVariableTypeInfo.referencedClasses);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        updateReferencedClasses(annotation.referencedClasses);
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
    {
        Clazz referencedClass    = elementValue.referencedClass;
        Clazz newReferencedClass = updateReferencedClass(referencedClass);
        if (referencedClass != newReferencedClass)
        {
            elementValue.referencedClass  = newReferencedClass;
            elementValue.referencedMethod =
                (Method)updateReferencedMember(elementValue.referencedMethod,
                                               elementValue.getMethodName(clazz),
                                               null,
                                               newReferencedClass);
        }
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        visitAnyElementValue(clazz, annotation, constantElementValue);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        visitAnyElementValue(clazz, annotation, enumConstantElementValue);
        updateReferencedClasses(enumConstantElementValue.referencedClasses);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        visitAnyElementValue(clazz, annotation, classElementValue);
        updateReferencedClasses(classElementValue.referencedClasses);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        visitAnyElementValue(clazz, annotation, annotationElementValue);
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        visitAnyElementValue(clazz, annotation, arrayElementValue);
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private void updateReferencedClasses(Clazz[] referencedClasses)
    {
        if (referencedClasses == null)
        {
            return;
        }
        for (int index = 0; index < referencedClasses.length; index++)
        {
            referencedClasses[index] =
                updateReferencedClass(referencedClasses[index]);
        }
    }
    private Clazz updateReferencedClass(Clazz referencedClass)
    {
        if (referencedClass == null)
        {
            return null;
        }
        Clazz targetClazz = ClassMerger.getTargetClass(referencedClass);
        return targetClazz != null ?
            targetClazz :
            referencedClass;
    }
    private Member updateReferencedMember(Member referencedMember,
                                          String name,
                                          String type,
                                          Clazz  newReferencedClass)
    {
        if (referencedMember == null)
        {
            return null;
        }
        return referencedMember instanceof Field ?
            (Member)newReferencedClass.findField(name, type) :
            (Member)newReferencedClass.findMethod(name, type);
    }
    private int addNewClassConstant(ProgramClass programClass,
                                    String       className,
                                    Clazz        referencedClass)
    {
        ConstantPoolEditor constantPoolEditor =
            new ConstantPoolEditor(programClass);
        int nameIndex =
            constantPoolEditor.addUtf8Constant(className);
        int classConstantIndex =
            constantPoolEditor.addConstant(new ClassConstant(nameIndex,
                                                             referencedClass));
        return classConstantIndex;
    }
}