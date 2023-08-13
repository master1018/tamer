import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.preverification.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.instruction.*;
import proguard.classfile.instruction.visitor.InstructionVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
class      UsageMarker
extends    SimplifiedVisitor
implements ClassVisitor,
           MemberVisitor,
           ConstantVisitor,
           AttributeVisitor,
           InnerClassesInfoVisitor,
           ExceptionInfoVisitor,
           StackMapFrameVisitor,
           VerificationTypeVisitor,
           LocalVariableInfoVisitor,
           LocalVariableTypeInfoVisitor,
           InstructionVisitor
{
    private static final Object POSSIBLY_USED = new Object();
    private static final Object USED          = new Object();
    private final MyInterfaceUsageMarker          interfaceUsageMarker          = new MyInterfaceUsageMarker();
    private final MyPossiblyUsedMemberUsageMarker possiblyUsedMemberUsageMarker = new MyPossiblyUsedMemberUsageMarker();
    public void visitProgramClass(ProgramClass programClass)
    {
        if (shouldBeMarkedAsUsed(programClass))
        {
            markAsUsed(programClass);
            markProgramClassBody(programClass);
        }
    }
    protected void markProgramClassBody(ProgramClass programClass)
    {
        markConstant(programClass, programClass.u2thisClass);
        if (programClass.u2superClass != 0)
        {
            markConstant(programClass, programClass.u2superClass);
        }
        programClass.hierarchyAccept(false, false, true, false,
                                     interfaceUsageMarker);
        programClass.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_CLINIT,
                                  ClassConstants.INTERNAL_METHOD_TYPE_CLINIT,
                                  this);
        programClass.methodAccept(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                  ClassConstants.INTERNAL_METHOD_TYPE_INIT,
                                  this);
        programClass.fieldsAccept(possiblyUsedMemberUsageMarker);
        programClass.methodsAccept(possiblyUsedMemberUsageMarker);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        if (shouldBeMarkedAsUsed(libraryClass))
        {
            markAsUsed(libraryClass);
            Clazz superClass = libraryClass.superClass;
            if (superClass != null)
            {
                superClass.accept(this);
            }
            Clazz[] interfaceClasses = libraryClass.interfaceClasses;
            if (interfaceClasses != null)
            {
                for (int index = 0; index < interfaceClasses.length; index++)
                {
                    if (interfaceClasses[index] != null)
                    {
                        interfaceClasses[index].accept(this);
                    }
                }
            }
            libraryClass.methodsAccept(this);
        }
    }
    private class MyInterfaceUsageMarker
    implements    ClassVisitor
    {
        public void visitProgramClass(ProgramClass programClass)
        {
            if (shouldBeMarkedAsPossiblyUsed(programClass))
            {
                markAsPossiblyUsed(programClass);
            }
        }
        public void visitLibraryClass(LibraryClass libraryClass)
        {
            UsageMarker.this.visitLibraryClass(libraryClass);
        }
    }
    private class MyPossiblyUsedMemberUsageMarker
    extends       SimplifiedVisitor
    implements    MemberVisitor
    {
        public void visitProgramField(ProgramClass programClass, ProgramField programField)
        {
            if (isPossiblyUsed(programField))
            {
                markAsUsed(programField);
                markConstant(programClass, programField.u2nameIndex);
                markConstant(programClass, programField.u2descriptorIndex);
                programField.attributesAccept(programClass, UsageMarker.this);
                programField.referencedClassesAccept(UsageMarker.this);
            }
        }
        public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
        {
            if (isPossiblyUsed(programMethod))
            {
                markAsUsed(programMethod);
                markProgramMethodBody(programClass, programMethod);
            }
        }
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        if (shouldBeMarkedAsUsed(programField))
        {
            if (isUsed(programClass))
            {
                markAsUsed(programField);
                markProgramFieldBody(programClass, programField);
            }
            else if (shouldBeMarkedAsPossiblyUsed(programField))
            {
                markAsPossiblyUsed(programField);
            }
        }
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        if (shouldBeMarkedAsUsed(programMethod))
        {
            if (isUsed(programClass))
            {
                markAsUsed(programMethod);
                markProgramMethodBody(programClass, programMethod);
                markMethodHierarchy(programClass, programMethod);
            }
            else if (shouldBeMarkedAsPossiblyUsed(programMethod))
            {
                markAsPossiblyUsed(programMethod);
                markMethodHierarchy(programClass, programMethod);
            }
        }
    }
    public void visitLibraryField(LibraryClass programClass, LibraryField programField) {}
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        if (shouldBeMarkedAsUsed(libraryMethod))
        {
            markAsUsed(libraryMethod);
            markMethodHierarchy(libraryClass, libraryMethod);
        }
    }
    protected void markProgramFieldBody(ProgramClass programClass, ProgramField programField)
    {
        markConstant(programClass, programField.u2nameIndex);
        markConstant(programClass, programField.u2descriptorIndex);
        programField.attributesAccept(programClass, this);
        programField.referencedClassesAccept(this);
    }
    protected void markProgramMethodBody(ProgramClass programClass, ProgramMethod programMethod)
    {
        markConstant(programClass, programMethod.u2nameIndex);
        markConstant(programClass, programMethod.u2descriptorIndex);
        programMethod.attributesAccept(programClass, this);
        programMethod.referencedClassesAccept(this);
    }
    protected void markMethodHierarchy(Clazz clazz, Method method)
    {
        if ((method.getAccessFlags() &
             (ClassConstants.INTERNAL_ACC_PRIVATE |
              ClassConstants.INTERNAL_ACC_STATIC)) == 0)
        {
            clazz.accept(new ConcreteClassDownTraveler(
                         new ClassHierarchyTraveler(true, true, false, true,
                         new NamedMethodVisitor(method.getName(clazz),
                                                method.getDescriptor(clazz),
                         new MemberAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE | ClassConstants.INTERNAL_ACC_STATIC | ClassConstants.INTERNAL_ACC_ABSTRACT,
                         this)))));
        }
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        if (shouldBeMarkedAsUsed(integerConstant))
        {
            markAsUsed(integerConstant);
        }
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        if (shouldBeMarkedAsUsed(longConstant))
        {
            markAsUsed(longConstant);
        }
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        if (shouldBeMarkedAsUsed(floatConstant))
        {
            markAsUsed(floatConstant);
        }
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        if (shouldBeMarkedAsUsed(doubleConstant))
        {
            markAsUsed(doubleConstant);
        }
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        if (shouldBeMarkedAsUsed(stringConstant))
        {
            markAsUsed(stringConstant);
            markConstant(clazz, stringConstant.u2stringIndex);
            stringConstant.referencedClassAccept(this);
            stringConstant.referencedMemberAccept(this);
        }
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        if (shouldBeMarkedAsUsed(utf8Constant))
        {
            markAsUsed(utf8Constant);
        }
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        if (shouldBeMarkedAsUsed(refConstant))
        {
            markAsUsed(refConstant);
            markConstant(clazz, refConstant.u2classIndex);
            markConstant(clazz, refConstant.u2nameAndTypeIndex);
            refConstant.referencedClassAccept(this);
            refConstant.referencedMemberAccept(this);
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        if (shouldBeMarkedAsUsed(classConstant))
        {
            markAsUsed(classConstant);
            markConstant(clazz, classConstant.u2nameIndex);
            classConstant.referencedClassAccept(this);
        }
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        if (shouldBeMarkedAsUsed(nameAndTypeConstant))
        {
            markAsUsed(nameAndTypeConstant);
            markConstant(clazz, nameAndTypeConstant.u2nameIndex);
            markConstant(clazz, nameAndTypeConstant.u2descriptorIndex);
        }
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        markAsUsed(unknownAttribute);
        markConstant(clazz, unknownAttribute.u2attributeNameIndex);
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        markAsUsed(sourceFileAttribute);
        markConstant(clazz, sourceFileAttribute.u2attributeNameIndex);
        markConstant(clazz, sourceFileAttribute.u2sourceFileIndex);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        markAsUsed(sourceDirAttribute);
        markConstant(clazz, sourceDirAttribute.u2attributeNameIndex);
        markConstant(clazz, sourceDirAttribute.u2sourceDirIndex);
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        markAsUsed(enclosingMethodAttribute);
        markConstant(clazz, enclosingMethodAttribute.u2attributeNameIndex);
        markConstant(clazz, enclosingMethodAttribute.u2classIndex);
        if (enclosingMethodAttribute.u2nameAndTypeIndex != 0)
        {
            markConstant(clazz, enclosingMethodAttribute.u2nameAndTypeIndex);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        markAsUsed(deprecatedAttribute);
        markConstant(clazz, deprecatedAttribute.u2attributeNameIndex);
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        markAsUsed(syntheticAttribute);
        markConstant(clazz, syntheticAttribute.u2attributeNameIndex);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        markAsUsed(signatureAttribute);
        markConstant(clazz, signatureAttribute.u2attributeNameIndex);
        markConstant(clazz, signatureAttribute.u2signatureIndex);
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        markAsUsed(constantValueAttribute);
        markConstant(clazz, constantValueAttribute.u2attributeNameIndex);
        markConstant(clazz, constantValueAttribute.u2constantValueIndex);
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        markAsUsed(exceptionsAttribute);
        markConstant(clazz, exceptionsAttribute.u2attributeNameIndex);
        exceptionsAttribute.exceptionEntriesAccept((ProgramClass)clazz, this);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        markAsUsed(codeAttribute);
        markConstant(clazz, codeAttribute.u2attributeNameIndex);
        codeAttribute.instructionsAccept(clazz, method, this);
        codeAttribute.exceptionsAccept(clazz, method, this);
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        markAsUsed(stackMapAttribute);
        markConstant(clazz, stackMapAttribute.u2attributeNameIndex);
        stackMapAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        markAsUsed(stackMapTableAttribute);
        markConstant(clazz, stackMapTableAttribute.u2attributeNameIndex);
        stackMapTableAttribute.stackMapFramesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        markAsUsed(lineNumberTableAttribute);
        markConstant(clazz, lineNumberTableAttribute.u2attributeNameIndex);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        markAsUsed(localVariableTableAttribute);
        markConstant(clazz, localVariableTableAttribute.u2attributeNameIndex);
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        markAsUsed(localVariableTypeTableAttribute);
        markConstant(clazz, localVariableTypeTableAttribute.u2attributeNameIndex);
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
    }
    public void visitExceptionInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, ExceptionInfo exceptionInfo)
    {
        markAsUsed(exceptionInfo);
        if (exceptionInfo.u2catchType != 0)
        {
            markConstant(clazz, exceptionInfo.u2catchType);
        }
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        if (innerClassesInfo.u2innerClassIndex != 0 &&
            clazz.getName().equals(clazz.getClassName(innerClassesInfo.u2innerClassIndex)))
        {
            markAsUsed(innerClassesInfo);
            innerClassesInfo.innerClassConstantAccept(clazz, this);
            innerClassesInfo.outerClassConstantAccept(clazz, this);
            innerClassesInfo.innerNameConstantAccept(clazz, this);
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
        markConstant(clazz, objectType.u2classIndex);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        markConstant(clazz, localVariableInfo.u2nameIndex);
        markConstant(clazz, localVariableInfo.u2descriptorIndex);
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        markConstant(clazz, localVariableTypeInfo.u2nameIndex);
        markConstant(clazz, localVariableTypeInfo.u2signatureIndex);
    }
    public void visitAnyInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, Instruction instruction) {}
    public void visitConstantInstruction(Clazz clazz, Method method, CodeAttribute codeAttribute, int offset, ConstantInstruction constantInstruction)
    {
        markConstant(clazz, constantInstruction.constantIndex);
    }
    protected void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }
    protected boolean shouldBeMarkedAsUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() != USED;
    }
    protected boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
    protected void markAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(POSSIBLY_USED);
    }
    protected boolean shouldBeMarkedAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() != USED &&
               visitorAccepter.getVisitorInfo() != POSSIBLY_USED;
    }
    protected boolean isPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == POSSIBLY_USED;
    }
    protected void markAsUnused(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(null);
    }
    private void markConstant(Clazz clazz, int index)
    {
         clazz.constantPoolEntryAccept(index, this);
    }
}
