package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class AttributeAdder
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private static final byte[]          EMPTY_BYTES       = new byte[0];
    private static final int[]           EMPTY_INTS        = new int[0];
    private static final Attribute[]     EMPTY_ATTRIBUTES  = new Attribute[0];
    private static final ExceptionInfo[] EMPTY_EXCEPTIONS  = new ExceptionInfo[0];
    private final ProgramClass  targetClass;
    private final ProgramMember targetMember;
    private final CodeAttribute targetCodeAttribute;
    private final boolean       replaceAttributes;
    private final ConstantAdder    constantAdder;
    private final AttributesEditor attributesEditor;
    public AttributeAdder(ProgramClass targetClass,
                          boolean      replaceAttributes)
    {
        this(targetClass, null, null, replaceAttributes);
    }
    public AttributeAdder(ProgramClass  targetClass,
                          ProgramMember targetMember,
                          boolean       replaceAttributes)
    {
        this(targetClass, targetMember, null, replaceAttributes);
    }
    public AttributeAdder(ProgramClass  targetClass,
                          ProgramMember targetMember,
                          CodeAttribute targetCodeAttribute,
                          boolean       replaceAttributes)
    {
        this.targetClass         = targetClass;
        this.targetMember        = targetMember;
        this.targetCodeAttribute = targetCodeAttribute;
        this.replaceAttributes   = replaceAttributes;
        constantAdder    = new ConstantAdder(targetClass);
        attributesEditor = new AttributesEditor(targetClass,
                                                targetMember,
                                                targetCodeAttribute,
                                                replaceAttributes);
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        UnknownAttribute newUnknownAttribute =
            new UnknownAttribute(constantAdder.addConstant(clazz, unknownAttribute.u2attributeNameIndex),
                                 unknownAttribute.u4attributeLength,
                                 unknownAttribute.info);
        attributesEditor.addAttribute(newUnknownAttribute);
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        SourceFileAttribute newSourceFileAttribute =
            new SourceFileAttribute(constantAdder.addConstant(clazz, sourceFileAttribute.u2attributeNameIndex),
                                    constantAdder.addConstant(clazz, sourceFileAttribute.u2sourceFileIndex));
        attributesEditor.addAttribute(newSourceFileAttribute);
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        SourceDirAttribute newSourceDirAttribute =
            new SourceDirAttribute(constantAdder.addConstant(clazz, sourceDirAttribute.u2attributeNameIndex),
                                   constantAdder.addConstant(clazz, sourceDirAttribute.u2sourceDirIndex));
        attributesEditor.addAttribute(newSourceDirAttribute);
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        EnclosingMethodAttribute newEnclosingMethodAttribute =
            new EnclosingMethodAttribute(constantAdder.addConstant(clazz, enclosingMethodAttribute.u2attributeNameIndex),
                                         constantAdder.addConstant(clazz, enclosingMethodAttribute.u2classIndex),
                                         enclosingMethodAttribute.u2nameAndTypeIndex == 0 ? 0 :
                                         constantAdder.addConstant(clazz, enclosingMethodAttribute.u2nameAndTypeIndex));
        newEnclosingMethodAttribute.referencedClass  = enclosingMethodAttribute.referencedClass;
        newEnclosingMethodAttribute.referencedMethod = enclosingMethodAttribute.referencedMethod;
        attributesEditor.addAttribute(newEnclosingMethodAttribute);
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        DeprecatedAttribute newDeprecatedAttribute =
            new DeprecatedAttribute(constantAdder.addConstant(clazz, deprecatedAttribute.u2attributeNameIndex));
        attributesEditor.addAttribute(newDeprecatedAttribute);
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        SyntheticAttribute newSyntheticAttribute =
            new SyntheticAttribute(constantAdder.addConstant(clazz, syntheticAttribute.u2attributeNameIndex));
        attributesEditor.addAttribute(newSyntheticAttribute);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        SignatureAttribute newSignatureAttribute =
            new SignatureAttribute(constantAdder.addConstant(clazz, signatureAttribute.u2attributeNameIndex),
                                   constantAdder.addConstant(clazz, signatureAttribute.u2signatureIndex));
        newSignatureAttribute.referencedClasses = signatureAttribute.referencedClasses;
        attributesEditor.addAttribute(newSignatureAttribute);
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        ConstantValueAttribute newConstantValueAttribute =
            new ConstantValueAttribute(constantAdder.addConstant(clazz, constantValueAttribute.u2attributeNameIndex),
                                       constantAdder.addConstant(clazz, constantValueAttribute.u2constantValueIndex));
        attributesEditor.addAttribute(newConstantValueAttribute);
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        ExceptionsAttribute newExceptionsAttribute =
            new ExceptionsAttribute(constantAdder.addConstant(clazz, exceptionsAttribute.u2attributeNameIndex),
                                    0,
                                    exceptionsAttribute.u2exceptionIndexTableLength > 0 ?
                                        new int[exceptionsAttribute.u2exceptionIndexTableLength] :
                                        EMPTY_INTS);
        exceptionsAttribute.exceptionEntriesAccept((ProgramClass)clazz,
                                                   new ExceptionAdder(targetClass,
                                                                      newExceptionsAttribute));
        attributesEditor.addAttribute(newExceptionsAttribute);
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        CodeAttribute newCodeAttribute =
            new CodeAttribute(constantAdder.addConstant(clazz, codeAttribute.u2attributeNameIndex),
                              codeAttribute.u2maxStack,
                              codeAttribute.u2maxLocals,
                              0,
                              EMPTY_BYTES,
                              0,
                              codeAttribute.u2exceptionTableLength > 0 ?
                                  new ExceptionInfo[codeAttribute.u2exceptionTableLength] :
                                  EMPTY_EXCEPTIONS,
                              0,
                              codeAttribute.u2attributesCount > 0 ?
                                  new Attribute[codeAttribute.u2attributesCount] :
                                  EMPTY_ATTRIBUTES);
        CodeAttributeComposer codeAttributeComposer = new CodeAttributeComposer();
        codeAttributeComposer.beginCodeFragment(codeAttribute.u4codeLength);
        codeAttribute.instructionsAccept(clazz,
                                         method,
                                         new InstructionAdder(targetClass,
                                                              codeAttributeComposer));
        codeAttributeComposer.appendLabel(codeAttribute.u4codeLength);
        codeAttribute.exceptionsAccept(clazz,
                                       method,
                                       new ExceptionInfoAdder(targetClass,
                                                              codeAttributeComposer));
        codeAttributeComposer.endCodeFragment();
        codeAttribute.attributesAccept(clazz,
                                       method,
                                       new AttributeAdder(targetClass,
                                                          targetMember,
                                                          newCodeAttribute,
                                                          replaceAttributes));
        codeAttributeComposer.visitCodeAttribute(targetClass,
                                                 (Method)targetMember,
                                                 newCodeAttribute);
        attributesEditor.addAttribute(newCodeAttribute);
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        LineNumberTableAttribute newLineNumberTableAttribute =
            new LineNumberTableAttribute(constantAdder.addConstant(clazz, lineNumberTableAttribute.u2attributeNameIndex),
                                         0,
                                         new LineNumberInfo[lineNumberTableAttribute.u2lineNumberTableLength]);
        lineNumberTableAttribute.lineNumbersAccept(clazz,
                                                   method,
                                                   codeAttribute,
                                                   new LineNumberInfoAdder(newLineNumberTableAttribute));
        attributesEditor.addAttribute(newLineNumberTableAttribute);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        LocalVariableTableAttribute newLocalVariableTableAttribute =
            new LocalVariableTableAttribute(constantAdder.addConstant(clazz, localVariableTableAttribute.u2attributeNameIndex),
                                            0,
                                            new LocalVariableInfo[localVariableTableAttribute.u2localVariableTableLength]);
        localVariableTableAttribute.localVariablesAccept(clazz,
                                                         method,
                                                         codeAttribute,
                                                         new LocalVariableInfoAdder(targetClass, newLocalVariableTableAttribute));
        attributesEditor.addAttribute(newLocalVariableTableAttribute);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        LocalVariableTypeTableAttribute newLocalVariableTypeTableAttribute =
            new LocalVariableTypeTableAttribute(constantAdder.addConstant(clazz, localVariableTypeTableAttribute.u2attributeNameIndex),
                                            0,
                                            new LocalVariableTypeInfo[localVariableTypeTableAttribute.u2localVariableTypeTableLength]);
        localVariableTypeTableAttribute.localVariablesAccept(clazz,
                                                             method,
                                                             codeAttribute,
                                                             new LocalVariableTypeInfoAdder(targetClass, newLocalVariableTypeTableAttribute));
        attributesEditor.addAttribute(newLocalVariableTypeTableAttribute);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        RuntimeVisibleAnnotationsAttribute newAnnotationsAttribute =
            new RuntimeVisibleAnnotationsAttribute(constantAdder.addConstant(clazz, runtimeVisibleAnnotationsAttribute.u2attributeNameIndex),
                                                   0,
                                                   new Annotation[runtimeVisibleAnnotationsAttribute.u2annotationsCount]);
        runtimeVisibleAnnotationsAttribute.annotationsAccept(clazz,
                                                             new AnnotationAdder(targetClass,
                                                                                 newAnnotationsAttribute));
        attributesEditor.addAttribute(newAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        RuntimeInvisibleAnnotationsAttribute newAnnotationsAttribute =
            new RuntimeInvisibleAnnotationsAttribute(constantAdder.addConstant(clazz, runtimeInvisibleAnnotationsAttribute.u2attributeNameIndex),
                                                     0,
                                                     new Annotation[runtimeInvisibleAnnotationsAttribute.u2annotationsCount]);
        runtimeInvisibleAnnotationsAttribute.annotationsAccept(clazz,
                                                               new AnnotationAdder(targetClass,
                                                                                   newAnnotationsAttribute));
        attributesEditor.addAttribute(newAnnotationsAttribute);
    }
    public void visitRuntimeVisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute)
    {
        RuntimeVisibleParameterAnnotationsAttribute newParameterAnnotationsAttribute =
            new RuntimeVisibleParameterAnnotationsAttribute(constantAdder.addConstant(clazz, runtimeVisibleParameterAnnotationsAttribute.u2attributeNameIndex),
                                                            0,
                                                            new int[runtimeVisibleParameterAnnotationsAttribute.u2parametersCount],
                                                            new Annotation[runtimeVisibleParameterAnnotationsAttribute.u2parametersCount][]);
        runtimeVisibleParameterAnnotationsAttribute.annotationsAccept(clazz,
                                                                      method,
                                                                      new AnnotationAdder(targetClass,
                                                                                          newParameterAnnotationsAttribute));
        attributesEditor.addAttribute(newParameterAnnotationsAttribute);
    }
    public void visitRuntimeInvisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute)
    {
        RuntimeInvisibleParameterAnnotationsAttribute newParameterAnnotationsAttribute =
            new RuntimeInvisibleParameterAnnotationsAttribute(constantAdder.addConstant(clazz, runtimeInvisibleParameterAnnotationsAttribute.u2attributeNameIndex),
                                                              0,
                                                              new int[runtimeInvisibleParameterAnnotationsAttribute.u2parametersCount],
                                                              new Annotation[runtimeInvisibleParameterAnnotationsAttribute.u2parametersCount][]);
        runtimeInvisibleParameterAnnotationsAttribute.annotationsAccept(clazz,
                                                                        method,
                                                                        new AnnotationAdder(targetClass,
                                                                                            newParameterAnnotationsAttribute));
        attributesEditor.addAttribute(newParameterAnnotationsAttribute);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        AnnotationDefaultAttribute newAnnotationDefaultAttribute =
            new AnnotationDefaultAttribute(constantAdder.addConstant(clazz, annotationDefaultAttribute.u2attributeNameIndex),
                                           null);
        annotationDefaultAttribute.defaultValueAccept(clazz,
                                                      new ElementValueAdder(targetClass,
                                                                            newAnnotationDefaultAttribute,
                                                                            false));
        attributesEditor.addAttribute(newAnnotationDefaultAttribute);
    }
}
