package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.preverification.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class ConstantPoolRemapper
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             MemberVisitor,
             AttributeVisitor,
             InstructionVisitor,
             InnerClassesInfoVisitor,
             ExceptionInfoVisitor,
             StackMapFrameVisitor,
             VerificationTypeVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final CodeAttributeEditor codeAttributeEditor = new CodeAttributeEditor();
    private int[] constantIndexMap;
    public void setConstantIndexMap(int[] constantIndexMap)
    {
        this.constantIndexMap = constantIndexMap;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.u2thisClass  = remapConstantIndex(programClass.u2thisClass);
        programClass.u2superClass = remapConstantIndex(programClass.u2superClass);
        remapConstantIndexArray(programClass.u2interfaces,
                                programClass.u2interfacesCount);
        programClass.constantPoolEntriesAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.u2nameIndex =
            remapConstantIndex(classConstant.u2nameIndex);
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
    }
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        fieldrefConstant.u2classIndex =
            remapConstantIndex(fieldrefConstant.u2classIndex);
        fieldrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(fieldrefConstant.u2nameAndTypeIndex);
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        interfaceMethodrefConstant.u2classIndex =
            remapConstantIndex(interfaceMethodrefConstant.u2classIndex);
        interfaceMethodrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(interfaceMethodrefConstant.u2nameAndTypeIndex);
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        methodrefConstant.u2classIndex =
            remapConstantIndex(methodrefConstant.u2classIndex);
        methodrefConstant.u2nameAndTypeIndex =
            remapConstantIndex(methodrefConstant.u2nameAndTypeIndex);
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        nameAndTypeConstant.u2nameIndex =
            remapConstantIndex(nameAndTypeConstant.u2nameIndex);
        nameAndTypeConstant.u2descriptorIndex =
            remapConstantIndex(nameAndTypeConstant.u2descriptorIndex);
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.u2stringIndex =
            remapConstantIndex(stringConstant.u2stringIndex);
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        visitMember(programClass, programField);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        visitMember(programClass, programMethod);
    }
    private void visitMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.u2nameIndex =
            remapConstantIndex(programMember.u2nameIndex);
        programMember.u2descriptorIndex =
            remapConstantIndex(programMember.u2descriptorIndex);
        programMember.attributesAccept(programClass, this);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        unknownAttribute.u2attributeNameIndex =
            remapConstantIndex(unknownAttribute.u2attributeNameIndex);
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        sourceFileAttribute.u2attributeNameIndex =
            remapConstantIndex(sourceFileAttribute.u2attributeNameIndex);
        sourceFileAttribute.u2sourceFileIndex =
            remapConstantIndex(sourceFileAttribute.u2sourceFileIndex);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        sourceDirAttribute.u2attributeNameIndex =
            remapConstantIndex(sourceDirAttribute.u2attributeNameIndex);
        sourceDirAttribute.u2sourceDirIndex       =
            remapConstantIndex(sourceDirAttribute.u2sourceDirIndex);
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.u2attributeNameIndex =
            remapConstantIndex(innerClassesAttribute.u2attributeNameIndex);
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        enclosingMethodAttribute.u2attributeNameIndex =
            remapConstantIndex(enclosingMethodAttribute.u2attributeNameIndex);
        enclosingMethodAttribute.u2classIndex =
            remapConstantIndex(enclosingMethodAttribute.u2classIndex);
        enclosingMethodAttribute.u2nameAndTypeIndex =
            remapConstantIndex(enclosingMethodAttribute.u2nameAndTypeIndex);
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        deprecatedAttribute.u2attributeNameIndex =
            remapConstantIndex(deprecatedAttribute.u2attributeNameIndex);
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        syntheticAttribute.u2attributeNameIndex =
            remapConstantIndex(syntheticAttribute.u2attributeNameIndex);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        signatureAttribute.u2attributeNameIndex =
            remapConstantIndex(signatureAttribute.u2attributeNameIndex);
        signatureAttribute.u2signatureIndex       =
            remapConstantIndex(signatureAttribute.u2signatureIndex);
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        constantValueAttribute.u2attributeNameIndex =
            remapConstantIndex(constantValueAttribute.u2attributeNameIndex);
        constantValueAttribute.u2constantValueIndex =
            remapConstantIndex(constantValueAttribute.u2constantValueIndex);
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        exceptionsAttribute.u2attributeNameIndex =
            remapConstantIndex(exceptionsAttribute.u2attributeNameIndex);
        remapConstantIndexArray(exceptionsAttribute.u2exceptionIndexTable,
                                exceptionsAttribute.u2exceptionIndexTableLength);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.u2attributeNameIndex =
            remapConstantIndex(codeAttribute.u2attributeNameIndex);
        codeAttributeEditor.reset(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttributeEditor.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.exceptionsAccept(clazz, method, this);
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        stackMapAttribute.u2attributeNameIndex =
            remapConstantIndex(stackMapAttribute.u2attributeNameIndex);
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        stackMapTableAttribute.u2attributeNameIndex =
            remapConstantIndex(stackMapTableAttribute.u2attributeNameIndex);
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        lineNumberTableAttribute.u2attributeNameIndex =
            remapConstantIndex(lineNumberTableAttribute.u2attributeNameIndex);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.u2attributeNameIndex =
            remapConstantIndex(localVariableTableAttribute.u2attributeNameIndex);
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.u2attributeNameIndex =
            remapConstantIndex(localVariableTypeTableAttribute.u2attributeNameIndex);
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute.u2attributeNameIndex =
            remapConstantIndex(annotationsAttribute.u2attributeNameIndex);
        annotationsAttribute.annotationsAccept(clazz, this);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        parameterAnnotationsAttribute.u2attributeNameIndex =
            remapConstantIndex(parameterAnnotationsAttribute.u2attributeNameIndex);
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        annotationDefaultAttribute.u2attributeNameIndex =
            remapConstantIndex(annotationDefaultAttribute.u2attributeNameIndex);
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerClassIndex != 0)
        {
            innerClassesInfo.u2innerClassIndex =
                remapConstantIndex(innerClassesInfo.u2innerClassIndex);
        }
        if (innerClassesInfo.u2outerClassIndex != 0)
        {
            innerClassesInfo.u2outerClassIndex =
                remapConstantIndex(innerClassesInfo.u2outerClassIndex);
        }
        if (innerClassesInfo.u2innerNameIndex != 0)
        {
            innerClassesInfo.u2innerNameIndex =
                remapConstantIndex(innerClassesInfo.u2innerNameIndex);
        }
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        if (exceptionInfo.u2catchType != 0)
        {
            exceptionInfo.u2catchType =
                remapConstantIndex(exceptionInfo.u2catchType);
        }
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        int newConstantIndex = remapConstantIndex(constantInstruction.constantIndex);
        if (newConstantIndex != constantInstruction.constantIndex)
        {
            Instruction replacementInstruction =
                new ConstantInstruction(constantInstruction.opcode,
                                        newConstantIndex,
                                        constantInstruction.constant).shrink();
            codeAttributeEditor.replaceInstruction(offset, replacementInstruction);
        }
    }
    public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame) {}
    public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
    {
        sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
    {
        moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
    {
        fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
        fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
    }
    public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType) {}
    public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
    {
        objectType.u2classIndex =
            remapConstantIndex(objectType.u2classIndex);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.u2nameIndex =
            remapConstantIndex(localVariableInfo.u2nameIndex);
        localVariableInfo.u2descriptorIndex =
            remapConstantIndex(localVariableInfo.u2descriptorIndex);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.u2nameIndex =
            remapConstantIndex(localVariableTypeInfo.u2nameIndex);
        localVariableTypeInfo.u2signatureIndex       =
            remapConstantIndex(localVariableTypeInfo.u2signatureIndex);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.u2typeIndex =
            remapConstantIndex(annotation.u2typeIndex);
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        constantElementValue.u2elementNameIndex =
            remapConstantIndex(constantElementValue.u2elementNameIndex);
        constantElementValue.u2constantValueIndex =
            remapConstantIndex(constantElementValue.u2constantValueIndex);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        enumConstantElementValue.u2elementNameIndex =
            remapConstantIndex(enumConstantElementValue.u2elementNameIndex);
        enumConstantElementValue.u2typeNameIndex =
            remapConstantIndex(enumConstantElementValue.u2typeNameIndex);
        enumConstantElementValue.u2constantNameIndex =
            remapConstantIndex(enumConstantElementValue.u2constantNameIndex);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        classElementValue.u2elementNameIndex =
            remapConstantIndex(classElementValue.u2elementNameIndex);
        classElementValue.u2classInfoIndex       =
            remapConstantIndex(classElementValue.u2classInfoIndex);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        annotationElementValue.u2elementNameIndex =
            remapConstantIndex(annotationElementValue.u2elementNameIndex);
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.u2elementNameIndex =
            remapConstantIndex(arrayElementValue.u2elementNameIndex);
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private void remapConstantIndexArray(int[] array, int length)
    {
        for (int index = 0; index < length; index++)
        {
            array[index] = remapConstantIndex(array[index]);
        }
    }
    private int remapConstantIndex(int constantIndex)
    {
        return constantIndexMap[constantIndex];
    }
}
