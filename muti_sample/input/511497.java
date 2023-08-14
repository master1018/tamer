package proguard.classfile.io;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.preverification.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import java.io.*;
public class ProgramClassWriter
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor,
             AttributeVisitor
{
    private RuntimeDataOutput dataOutput;
    private final ConstantBodyWriter         constantBodyWriter         = new ConstantBodyWriter();
    private final AttributeBodyWriter        attributeBodyWriter        = new AttributeBodyWriter();
    private final StackMapFrameBodyWriter    stackMapFrameBodyWriter    = new StackMapFrameBodyWriter();
    private final VerificationTypeBodyWriter verificationTypeBodyWriter = new VerificationTypeBodyWriter();
    private final ElementValueBodyWriter     elementValueBodyWriter     = new ElementValueBodyWriter();
    public ProgramClassWriter(DataOutput dataOutput)
    {
        this.dataOutput = new RuntimeDataOutput(dataOutput);
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        dataOutput.writeInt(programClass.u4magic);
        dataOutput.writeShort(ClassUtil.internalMinorClassVersion(programClass.u4version));
        dataOutput.writeShort(ClassUtil.internalMajorClassVersion(programClass.u4version));
        dataOutput.writeShort(programClass.u2constantPoolCount);
        programClass.constantPoolEntriesAccept(this);
        dataOutput.writeShort(programClass.u2accessFlags);
        dataOutput.writeShort(programClass.u2thisClass);
        dataOutput.writeShort(programClass.u2superClass);
        dataOutput.writeShort(programClass.u2interfacesCount);
        for (int index = 0; index < programClass.u2interfacesCount; index++)
        {
            dataOutput.writeShort(programClass.u2interfaces[index]);
        }
        dataOutput.writeShort(programClass.u2fieldsCount);
        programClass.fieldsAccept(this);
        dataOutput.writeShort(programClass.u2methodsCount);
        programClass.methodsAccept(this);
        dataOutput.writeShort(programClass.u2attributesCount);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        dataOutput.writeShort(programField.u2accessFlags);
        dataOutput.writeShort(programField.u2nameIndex);
        dataOutput.writeShort(programField.u2descriptorIndex);
        dataOutput.writeShort(programField.u2attributesCount);
        programField.attributesAccept(programClass, this);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        dataOutput.writeShort(programMethod.u2accessFlags);
        dataOutput.writeShort(programMethod.u2nameIndex);
        dataOutput.writeShort(programMethod.u2descriptorIndex);
        dataOutput.writeShort(programMethod.u2attributesCount);
        programMethod.attributesAccept(programClass, this);
    }
    public void visitLibraryMember(LibraryClass libraryClass, LibraryMember libraryMember)
    {
    }
    public void visitAnyConstant(Clazz clazz, Constant constant)
    {
        dataOutput.writeByte(constant.getTag());
        constant.accept(clazz, constantBodyWriter);
    }
    private class ConstantBodyWriter
    extends       SimplifiedVisitor
    implements    ConstantVisitor
    {
        public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
        {
            dataOutput.writeInt(integerConstant.u4value);
        }
        public void visitLongConstant(Clazz clazz, LongConstant longConstant)
        {
            dataOutput.writeLong(longConstant.u8value);
        }
        public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
        {
            dataOutput.writeFloat(floatConstant.f4value);
        }
        public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
        {
            dataOutput.writeDouble(doubleConstant.f8value);
        }
        public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
        {
            dataOutput.writeShort(stringConstant.u2stringIndex);
        }
        public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
        {
            byte[] bytes = utf8Constant.getBytes();
            dataOutput.writeShort(bytes.length);
            dataOutput.write(bytes);
        }
        public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
        {
            dataOutput.writeShort(refConstant.u2classIndex);
            dataOutput.writeShort(refConstant.u2nameAndTypeIndex);
        }
        public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
        {
            dataOutput.writeShort(classConstant.u2nameIndex);
        }
        public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
        {
            dataOutput.writeShort(nameAndTypeConstant.u2nameIndex);
            dataOutput.writeShort(nameAndTypeConstant.u2descriptorIndex);
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute)
    {
        dataOutput.writeShort(attribute.u2attributeNameIndex);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        RuntimeDataOutput oldDataOutput = dataOutput;
        dataOutput = new RuntimeDataOutput(new DataOutputStream(byteArrayOutputStream));
        attribute.accept(clazz, null, null, attributeBodyWriter);
        dataOutput = oldDataOutput;
        byte[] info = byteArrayOutputStream.toByteArray();
        dataOutput.writeInt(info.length);
        dataOutput.write(info);
    }
    private class AttributeBodyWriter
    extends       SimplifiedVisitor
    implements    AttributeVisitor,
                  InnerClassesInfoVisitor,
                  ExceptionInfoVisitor,
                  StackMapFrameVisitor,
                  VerificationTypeVisitor,
                  LineNumberInfoVisitor,
                  LocalVariableInfoVisitor,
                  LocalVariableTypeInfoVisitor,
                  AnnotationVisitor,
                  ElementValueVisitor
    {
        public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
        {
            dataOutput.write(unknownAttribute.info);
        }
        public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
        {
            dataOutput.writeShort(sourceFileAttribute.u2sourceFileIndex);
        }
        public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
        {
            dataOutput.writeShort(sourceDirAttribute.u2sourceDirIndex);
        }
        public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
        {
            dataOutput.writeShort(innerClassesAttribute.u2classesCount);
            innerClassesAttribute.innerClassEntriesAccept(clazz, this);
        }
        public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
        {
            dataOutput.writeShort(enclosingMethodAttribute.u2classIndex);
            dataOutput.writeShort(enclosingMethodAttribute.u2nameAndTypeIndex);
        }
        public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
        {
        }
        public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
        {
        }
        public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
        {
            dataOutput.writeShort(signatureAttribute.u2signatureIndex);
        }
        public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
        {
            dataOutput.writeShort(constantValueAttribute.u2constantValueIndex);
        }
        public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
        {
            dataOutput.writeShort(exceptionsAttribute.u2exceptionIndexTableLength);
            for (int index = 0; index < exceptionsAttribute.u2exceptionIndexTableLength; index++)
            {
                dataOutput.writeShort(exceptionsAttribute.u2exceptionIndexTable[index]);
            }
        }
        public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
        {
            dataOutput.writeShort(codeAttribute.u2maxStack);
            dataOutput.writeShort(codeAttribute.u2maxLocals);
            dataOutput.writeInt(codeAttribute.u4codeLength);
            dataOutput.write(codeAttribute.code, 0, codeAttribute.u4codeLength);
            dataOutput.writeShort(codeAttribute.u2exceptionTableLength);
            codeAttribute.exceptionsAccept(clazz, method, this);
            dataOutput.writeShort(codeAttribute.u2attributesCount);
            codeAttribute.attributesAccept(clazz, method, ProgramClassWriter.this);
        }
        public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
        {
            dataOutput.writeShort(stackMapAttribute.u2stackMapFramesCount);
            stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, stackMapFrameBodyWriter);
        }
        public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
        {
            dataOutput.writeShort(stackMapTableAttribute.u2stackMapFramesCount);
            stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
        }
        public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
        {
            dataOutput.writeShort(lineNumberTableAttribute.u2lineNumberTableLength);
            lineNumberTableAttribute.lineNumbersAccept(clazz, method, codeAttribute, this);
        }
        public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
        {
            dataOutput.writeShort(localVariableTableAttribute.u2localVariableTableLength);
            localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        }
        public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
        {
            dataOutput.writeShort(localVariableTypeTableAttribute.u2localVariableTypeTableLength);
            localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
        }
        public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
        {
            dataOutput.writeShort(annotationsAttribute.u2annotationsCount);
            annotationsAttribute.annotationsAccept(clazz, this);
        }
        public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
        {
            dataOutput.writeByte(parameterAnnotationsAttribute.u2parametersCount);
            for (int parameterIndex = 0; parameterIndex < parameterAnnotationsAttribute.u2parametersCount; parameterIndex++)
            {
                int          u2annotationsCount = parameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex];
                Annotation[] annotations        = parameterAnnotationsAttribute.parameterAnnotations[parameterIndex];
                dataOutput.writeShort(u2annotationsCount);
                for (int index = 0; index < u2annotationsCount; index++)
                {
                    Annotation annotation = annotations[index];
                    this.visitAnnotation(clazz, annotation);
                }
            }
        }
        public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
        {
            annotationDefaultAttribute.defaultValue.accept(clazz, null, this);
        }
        public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
        {
            dataOutput.writeShort(innerClassesInfo.u2innerClassIndex);
            dataOutput.writeShort(innerClassesInfo.u2outerClassIndex);
            dataOutput.writeShort(innerClassesInfo.u2innerNameIndex);
            dataOutput.writeShort(innerClassesInfo.u2innerClassAccessFlags);
        }
        public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
        {
            dataOutput.writeShort(exceptionInfo.u2startPC);
            dataOutput.writeShort(exceptionInfo.u2endPC);
            dataOutput.writeShort(exceptionInfo.u2handlerPC);
            dataOutput.writeShort(exceptionInfo.u2catchType);
        }
        public void visitAnyStackMapFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, StackMapFrame stackMapFrame)
        {
            dataOutput.writeByte(stackMapFrame.getTag());
            stackMapFrame.accept(clazz, method, codeAttribute, offset, stackMapFrameBodyWriter);
        }
        public void visitLineNumberInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberInfo lineNumberInfo)
        {
            dataOutput.writeShort(lineNumberInfo.u2startPC);
            dataOutput.writeShort(lineNumberInfo.u2lineNumber);
        }
        public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
        {
            dataOutput.writeShort(localVariableInfo.u2startPC);
            dataOutput.writeShort(localVariableInfo.u2length);
            dataOutput.writeShort(localVariableInfo.u2nameIndex);
            dataOutput.writeShort(localVariableInfo.u2descriptorIndex);
            dataOutput.writeShort(localVariableInfo.u2index);
        }
        public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
        {
            dataOutput.writeShort(localVariableTypeInfo.u2startPC);
            dataOutput.writeShort(localVariableTypeInfo.u2length);
            dataOutput.writeShort(localVariableTypeInfo.u2nameIndex);
            dataOutput.writeShort(localVariableTypeInfo.u2signatureIndex);
            dataOutput.writeShort(localVariableTypeInfo.u2index);
        }
        public void visitAnnotation(Clazz clazz, Annotation annotation)
        {
            dataOutput.writeShort(annotation.u2typeIndex);
            dataOutput.writeShort(annotation.u2elementValuesCount);
            annotation.elementValuesAccept(clazz, this);
        }
        public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
        {
            int u2elementNameIndex = elementValue.u2elementNameIndex;
            if (u2elementNameIndex != 0)
            {
                dataOutput.writeShort(u2elementNameIndex);
            }
            dataOutput.writeByte(elementValue.getTag());
            elementValue.accept(clazz, annotation, elementValueBodyWriter);
        }
    }
    private class StackMapFrameBodyWriter
    extends       SimplifiedVisitor
    implements    StackMapFrameVisitor,
                  VerificationTypeVisitor
    {
        public void visitSameZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameZeroFrame sameZeroFrame)
        {
            if (sameZeroFrame.getTag() == StackMapFrame.SAME_ZERO_FRAME_EXTENDED)
            {
                dataOutput.writeShort(sameZeroFrame.u2offsetDelta);
            }
        }
        public void visitSameOneFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, SameOneFrame sameOneFrame)
        {
            if (sameOneFrame.getTag() == StackMapFrame.SAME_ONE_FRAME_EXTENDED)
            {
                dataOutput.writeShort(sameOneFrame.u2offsetDelta);
            }
            sameOneFrame.stackItemAccept(clazz, method, codeAttribute, offset, this);
        }
        public void visitLessZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, LessZeroFrame lessZeroFrame)
        {
            dataOutput.writeShort(lessZeroFrame.u2offsetDelta);
        }
        public void visitMoreZeroFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, MoreZeroFrame moreZeroFrame)
        {
            dataOutput.writeShort(moreZeroFrame.u2offsetDelta);
            moreZeroFrame.additionalVariablesAccept(clazz, method, codeAttribute, offset, this);
        }
        public void visitFullFrame(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, FullFrame fullFrame)
        {
            dataOutput.writeShort(fullFrame.u2offsetDelta);
            dataOutput.writeShort(fullFrame.variablesCount);
            fullFrame.variablesAccept(clazz, method, codeAttribute, offset, this);
            dataOutput.writeShort(fullFrame.stackCount);
            fullFrame.stackAccept(clazz, method, codeAttribute, offset, this);
        }
        public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType)
        {
            dataOutput.writeByte(verificationType.getTag());
            verificationType.accept(clazz, method, codeAttribute, offset, verificationTypeBodyWriter);
        }
    }
    private class VerificationTypeBodyWriter
    extends       SimplifiedVisitor
    implements    VerificationTypeVisitor
    {
        public void visitAnyVerificationType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, VerificationType verificationType)
        {
        }
        public void visitObjectType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ObjectType objectType)
        {
            dataOutput.writeShort(objectType.u2classIndex);
        }
        public void visitUninitializedType(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, UninitializedType uninitializedType)
        {
            dataOutput.writeShort(uninitializedType.u2newInstructionOffset);
        }
    }
    private class ElementValueBodyWriter
    extends       SimplifiedVisitor
    implements    ElementValueVisitor
    {
        public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
        {
            dataOutput.writeShort(constantElementValue.u2constantValueIndex);
        }
        public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
        {
            dataOutput.writeShort(enumConstantElementValue.u2typeNameIndex);
            dataOutput.writeShort(enumConstantElementValue.u2constantNameIndex);
        }
        public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
        {
            dataOutput.writeShort(classElementValue.u2classInfoIndex);
        }
        public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
        {
            attributeBodyWriter.visitAnnotation(clazz, annotationElementValue.annotationValue);
        }
        public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
        {
            dataOutput.writeShort(arrayElementValue.u2elementValuesCount);
            arrayElementValue.elementValuesAccept(clazz, annotation, attributeBodyWriter);
        }
    }
}
