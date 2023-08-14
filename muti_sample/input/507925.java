import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class Utf8UsageMarker
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor,
             AttributeVisitor,
             InnerClassesInfoVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private static final Object USED = new Object();
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        markCpUtf8Entry(programClass, programMember.u2nameIndex);
        markCpUtf8Entry(programClass, programMember.u2descriptorIndex);
        programMember.attributesAccept(programClass, this);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        markCpUtf8Entry(clazz, stringConstant.u2stringIndex);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        markCpUtf8Entry(clazz, classConstant.u2nameIndex);
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        markCpUtf8Entry(clazz, nameAndTypeConstant.u2nameIndex);
        markCpUtf8Entry(clazz, nameAndTypeConstant.u2descriptorIndex);
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        markCpUtf8Entry(clazz, unknownAttribute.u2attributeNameIndex);
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        markCpUtf8Entry(clazz, sourceFileAttribute.u2attributeNameIndex);
        markCpUtf8Entry(clazz, sourceFileAttribute.u2sourceFileIndex);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        markCpUtf8Entry(clazz, sourceDirAttribute.u2attributeNameIndex);
        markCpUtf8Entry(clazz, sourceDirAttribute.u2sourceDirIndex);
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        markCpUtf8Entry(clazz, innerClassesAttribute.u2attributeNameIndex);
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        markCpUtf8Entry(clazz, enclosingMethodAttribute.u2attributeNameIndex);
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        markCpUtf8Entry(clazz, deprecatedAttribute.u2attributeNameIndex);
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        markCpUtf8Entry(clazz, syntheticAttribute.u2attributeNameIndex);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        markCpUtf8Entry(clazz, signatureAttribute.u2attributeNameIndex);
        markCpUtf8Entry(clazz, signatureAttribute.u2signatureIndex);
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        markCpUtf8Entry(clazz, constantValueAttribute.u2attributeNameIndex);
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        markCpUtf8Entry(clazz, exceptionsAttribute.u2attributeNameIndex);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        markCpUtf8Entry(clazz, codeAttribute.u2attributeNameIndex);
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        markCpUtf8Entry(clazz, stackMapAttribute.u2attributeNameIndex);
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        markCpUtf8Entry(clazz, stackMapTableAttribute.u2attributeNameIndex);
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        markCpUtf8Entry(clazz, lineNumberTableAttribute.u2attributeNameIndex);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        markCpUtf8Entry(clazz, localVariableTableAttribute.u2attributeNameIndex);
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        markCpUtf8Entry(clazz, localVariableTypeTableAttribute.u2attributeNameIndex);
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        markCpUtf8Entry(clazz, annotationsAttribute.u2attributeNameIndex);
        annotationsAttribute.annotationsAccept(clazz, this);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        markCpUtf8Entry(clazz, parameterAnnotationsAttribute.u2attributeNameIndex);
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        markCpUtf8Entry(clazz, annotationDefaultAttribute.u2attributeNameIndex);
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            markCpUtf8Entry(clazz, innerClassesInfo.u2innerNameIndex);
        }
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        markCpUtf8Entry(clazz, localVariableInfo.u2nameIndex);
        markCpUtf8Entry(clazz, localVariableInfo.u2descriptorIndex);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        markCpUtf8Entry(clazz, localVariableTypeInfo.u2nameIndex);
        markCpUtf8Entry(clazz, localVariableTypeInfo.u2signatureIndex);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        markCpUtf8Entry(clazz, annotation.u2typeIndex);
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        if (constantElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, constantElementValue.u2elementNameIndex);
        }
        if (constantElementValue.u1tag == ClassConstants.ELEMENT_VALUE_STRING_CONSTANT)
        {
            markCpUtf8Entry(clazz, constantElementValue.u2constantValueIndex);
        }
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        if (enumConstantElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, enumConstantElementValue.u2elementNameIndex);
        }
        markCpUtf8Entry(clazz, enumConstantElementValue.u2typeNameIndex);
        markCpUtf8Entry(clazz, enumConstantElementValue.u2constantNameIndex);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        if (classElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, classElementValue.u2elementNameIndex);
        }
        markCpUtf8Entry(clazz, classElementValue.u2classInfoIndex);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        if (annotationElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, annotationElementValue.u2elementNameIndex);
        }
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        if (arrayElementValue.u2elementNameIndex != 0)
        {
            markCpUtf8Entry(clazz, arrayElementValue.u2elementNameIndex);
        }
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private void markCpUtf8Entry(Clazz clazz, int index)
    {
         markAsUsed((Utf8Constant)((ProgramClass)clazz).getConstant(index));
    }
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
