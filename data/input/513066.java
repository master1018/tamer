package proguard.classfile.attribute.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.preverification.*;
import proguard.util.StringMatcher;
public class AttributeNameFilter
implements   AttributeVisitor
{
    private final StringMatcher    stringMatcher;
    private final AttributeVisitor attributeVisitor;
    public AttributeNameFilter(StringMatcher    stringMatcher,
                               AttributeVisitor attributeVisitor)
    {
        this.stringMatcher    = stringMatcher;
        this.attributeVisitor = attributeVisitor;
    }
    public void visitUnknownAttribute(Clazz clazz, UnknownAttribute unknownAttribute)
    {
        if (accepted(clazz, unknownAttribute))
        {
            unknownAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitSourceFileAttribute(Clazz clazz, SourceFileAttribute sourceFileAttribute)
    {
        if (accepted(clazz, sourceFileAttribute))
        {
            sourceFileAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitSourceDirAttribute(Clazz clazz, SourceDirAttribute sourceDirAttribute)
    {
        if (accepted(clazz, sourceDirAttribute))
        {
            sourceDirAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        if (accepted(clazz, innerClassesAttribute))
        {
            innerClassesAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        if (accepted(clazz, enclosingMethodAttribute))
        {
            enclosingMethodAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, DeprecatedAttribute deprecatedAttribute)
    {
        if (accepted(clazz, deprecatedAttribute))
        {
            deprecatedAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, Field field, DeprecatedAttribute deprecatedAttribute)
    {
        if (accepted(clazz, deprecatedAttribute))
        {
            deprecatedAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitDeprecatedAttribute(Clazz clazz, Method method, DeprecatedAttribute deprecatedAttribute)
    {
        if (accepted(clazz, deprecatedAttribute))
        {
            deprecatedAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, SyntheticAttribute syntheticAttribute)
    {
        if (accepted(clazz, syntheticAttribute))
        {
            syntheticAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, Field field, SyntheticAttribute syntheticAttribute)
    {
        if (accepted(clazz, syntheticAttribute))
        {
            syntheticAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitSyntheticAttribute(Clazz clazz, Method method, SyntheticAttribute syntheticAttribute)
    {
        if (accepted(clazz, syntheticAttribute))
        {
            syntheticAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        if (accepted(clazz, signatureAttribute))
        {
            signatureAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, Field field, SignatureAttribute signatureAttribute)
    {
        if (accepted(clazz, signatureAttribute))
        {
            signatureAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitSignatureAttribute(Clazz clazz, Method method, SignatureAttribute signatureAttribute)
    {
        if (accepted(clazz, signatureAttribute))
        {
            signatureAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitConstantValueAttribute(Clazz clazz, Field field, ConstantValueAttribute constantValueAttribute)
    {
        if (accepted(clazz, constantValueAttribute))
        {
            constantValueAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitExceptionsAttribute(Clazz clazz, Method method, ExceptionsAttribute exceptionsAttribute)
    {
        if (accepted(clazz, exceptionsAttribute))
        {
            exceptionsAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        if (accepted(clazz, codeAttribute))
        {
            codeAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitStackMapAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapAttribute stackMapAttribute)
    {
        if (accepted(clazz, stackMapAttribute))
        {
            stackMapAttribute.accept(clazz, method, codeAttribute, attributeVisitor);
        }
    }
    public void visitStackMapTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, StackMapTableAttribute stackMapTableAttribute)
    {
        if (accepted(clazz, stackMapTableAttribute))
        {
            stackMapTableAttribute.accept(clazz, method, codeAttribute, attributeVisitor);
        }
    }
    public void visitLineNumberTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LineNumberTableAttribute lineNumberTableAttribute)
    {
        if (accepted(clazz, lineNumberTableAttribute))
        {
            lineNumberTableAttribute.accept(clazz, method, codeAttribute, attributeVisitor);
        }
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        if (accepted(clazz, localVariableTableAttribute))
        {
            localVariableTableAttribute.accept(clazz, method, codeAttribute, attributeVisitor);
        }
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        if (accepted(clazz, localVariableTypeTableAttribute))
        {
            localVariableTypeTableAttribute.accept(clazz, method, codeAttribute, attributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeVisibleAnnotationsAttribute))
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeVisibleAnnotationsAttribute))
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeVisibleAnnotationsAttribute))
        {
            runtimeVisibleAnnotationsAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeInvisibleAnnotationsAttribute))
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, attributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeInvisibleAnnotationsAttribute))
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, field, attributeVisitor);
        }
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeInvisibleAnnotationsAttribute))
        {
            runtimeInvisibleAnnotationsAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitRuntimeVisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleParameterAnnotationsAttribute runtimeVisibleParameterAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeVisibleParameterAnnotationsAttribute))
        {
            runtimeVisibleParameterAnnotationsAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitRuntimeInvisibleParameterAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleParameterAnnotationsAttribute runtimeInvisibleParameterAnnotationsAttribute)
    {
        if (accepted(clazz, runtimeInvisibleParameterAnnotationsAttribute))
        {
            runtimeInvisibleParameterAnnotationsAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        if (accepted(clazz, annotationDefaultAttribute))
        {
            annotationDefaultAttribute.accept(clazz, method, attributeVisitor);
        }
    }
    private boolean accepted(Clazz clazz, Attribute attribute)
    {
        return stringMatcher.matches(attribute.getAttributeName(clazz));
    }
}
