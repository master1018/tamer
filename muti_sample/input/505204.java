package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
public interface AttributeVisitor
{
    public void visitUnknownAttribute(               Clazz clazz,                UnknownAttribute         unknownAttribute);
    public void visitSourceFileAttribute(            Clazz clazz,                SourceFileAttribute      sourceFileAttribute);
    public void visitSourceDirAttribute(             Clazz clazz,                SourceDirAttribute       sourceDirAttribute);
    public void visitInnerClassesAttribute(          Clazz clazz,                InnerClassesAttribute    innerClassesAttribute);
    public void visitEnclosingMethodAttribute(       Clazz clazz,                EnclosingMethodAttribute enclosingMethodAttribute);
    public void visitDeprecatedAttribute(            Clazz clazz,                DeprecatedAttribute deprecatedAttribute);
    public void visitDeprecatedAttribute(            Clazz clazz, Field  field,  DeprecatedAttribute deprecatedAttribute);
    public void visitDeprecatedAttribute(            Clazz clazz, Method method, DeprecatedAttribute deprecatedAttribute);
    public void visitSyntheticAttribute(             Clazz clazz,                SyntheticAttribute  syntheticAttribute);
    public void visitSyntheticAttribute(             Clazz clazz, Field  field,  SyntheticAttribute  syntheticAttribute);
    public void visitSyntheticAttribute(             Clazz clazz, Method method, SyntheticAttribute  syntheticAttribute);
    public void visitSignatureAttribute(             Clazz clazz,                SignatureAttribute  signatureAttribute);
    public void visitSignatureAttribute(             Clazz clazz, Field  field,  SignatureAttribute  signatureAttribute);
    public void visitSignatureAttribute(             Clazz clazz, Method method, SignatureAttribute  signatureAttribute);
    public void visitConstantValueAttribute(         Clazz clazz, Field  field,  ConstantValueAttribute constantValueAttribute);
    public void visitExceptionsAttribute(            Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute);
    public void visitCodeAttribute(                  Clazz clazz, Method method, CodeAttribute       codeAttribute);
    public void visitStackMapAttribute(              Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute               stackMapAttribute);
    public void visitStackMapTableAttribute(         Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute          stackMapTableAttribute);
    public void visitLineNumberTableAttribute(       Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute        lineNumberTableAttribute);
    public void visitLocalVariableTableAttribute(    Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute     localVariableTableAttribute);
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute);
    public void visitRuntimeVisibleAnnotationsAttribute(           Clazz clazz,                RuntimeVisibleAnnotationsAttribute   runtimeVisibleAnnotationsAttribute);
    public void visitRuntimeVisibleAnnotationsAttribute(           Clazz clazz, Field  field,  RuntimeVisibleAnnotationsAttribute   runtimeVisibleAnnotationsAttribute);
    public void visitRuntimeVisibleAnnotationsAttribute(           Clazz clazz, Method method, RuntimeVisibleAnnotationsAttribute   runtimeVisibleAnnotationsAttribute);
    public void visitRuntimeInvisibleAnnotationsAttribute(         Clazz clazz,                RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute);
    public void visitRuntimeInvisibleAnnotationsAttribute(         Clazz clazz, Field  field,  RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute);
    public void visitRuntimeInvisibleAnnotationsAttribute(         Clazz clazz, Method method, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute);
    public void visitRuntimeVisibleParameterAnnotationsAttribute(  Clazz clazz, Method method, RuntimeVisibleParameterAnnotationsAttribute   runtimeVisibleParameterAnnotationsAttribute);
    public void visitRuntimeInvisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute);
    public void visitAnnotationDefaultAttribute(                   Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute);
}
