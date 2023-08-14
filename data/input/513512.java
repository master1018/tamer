package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.obfuscate.AttributeShrinker;
public class RequiredAttributeFilter
implements   AttributeVisitor
{
    private final AttributeVisitor requiredAttributeVisitor;
    private final AttributeVisitor optionalAttributeVisitor;
    public RequiredAttributeFilter(AttributeVisitor requiredAttributeVisitor)
    {
        this(requiredAttributeVisitor, null);
    }
    public RequiredAttributeFilter(AttributeVisitor requiredAttributeVisitor,
                                   AttributeVisitor optionalAttributeVisitor)
    {
        this.requiredAttributeVisitor = requiredAttributeVisitor;
        this.optionalAttributeVisitor = optionalAttributeVisitor;
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            unknownAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            sourceFileAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            sourceDirAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            innerClassesAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            enclosingMethodAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            deprecatedAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, Field field, DeprecatedAttribute deprecatedAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            deprecatedAttribute.accept(clazz, field, optionalAttributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, Method method, DeprecatedAttribute deprecatedAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            deprecatedAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            syntheticAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, Field field, SyntheticAttribute syntheticAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            syntheticAttribute.accept(clazz, field, optionalAttributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, Method method, SyntheticAttribute syntheticAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            syntheticAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            signatureAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, Field field, SignatureAttribute signatureAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            signatureAttribute.accept(clazz, field, optionalAttributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, Method method, SignatureAttribute signatureAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            signatureAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        if (requiredAttributeVisitor != null)
        {
            constantValueAttribute.accept(clazz, field, requiredAttributeVisitor);
        }
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            exceptionsAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (requiredAttributeVisitor != null)
        {
            codeAttribute.accept(clazz, method, requiredAttributeVisitor);
        }
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            stackMapAttribute.accept(clazz, method, codeAttribute, optionalAttributeVisitor);
        }
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        if (requiredAttributeVisitor != null)
        {
            stackMapTableAttribute.accept(clazz, method, codeAttribute, requiredAttributeVisitor);
        }
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            lineNumberTableAttribute.accept(clazz, method, codeAttribute, optionalAttributeVisitor);
        }
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            localVariableTableAttribute.accept(clazz, method, codeAttribute, optionalAttributeVisitor);
        }
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            localVariableTypeTableAttribute.accept(clazz, method, codeAttribute, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, field, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, field, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeVisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeVisibleParameterAnnotationsAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitRuntimeInvisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            runtimeInvisibleParameterAnnotationsAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        if (optionalAttributeVisitor != null)
        {
            annotationDefaultAttribute.accept(clazz, method, optionalAttributeVisitor);
        }
    }
}
