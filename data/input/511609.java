package proguard.classfile.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class ReferencedClassVisitor
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor,
             AttributeVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final ClassVisitor classVisitor;
    public ReferencedClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
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
        libraryClass.superClassAccept(classVisitor);
        libraryClass.interfacesAccept(classVisitor);
        libraryClass.fieldsAccept(this);
        libraryClass.methodsAccept(this);
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.referencedClassesAccept(classVisitor);
        programMember.attributesAccept(programClass, this);
    }
    public void visitLibraryMember(LibraryClass programClass, LibraryMember libraryMember)
    {
        libraryMember.referencedClassesAccept(classVisitor);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.referencedClassAccept(classVisitor);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        refConstant.referencedClassAccept(classVisitor);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClassAccept(classVisitor);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        enclosingMethodAttribute.referencedClassAccept(classVisitor);
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
        signatureAttribute.referencedClassesAccept(classVisitor);
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
        localVariableInfo.referencedClassAccept(classVisitor);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.referencedClassesAccept(classVisitor);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.referencedClassesAccept(classVisitor);
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue) {}
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        enumConstantElementValue.referencedClassesAccept(classVisitor);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        classElementValue.referencedClassesAccept(classVisitor);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
}
