package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class AllAnnotationVisitor
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final AnnotationVisitor annotationVisitor;
    public AllAnnotationVisitor(AnnotationVisitor annotationVisitor)
    {
        this.annotationVisitor = annotationVisitor;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        runtimeVisibleAnnotationsAttribute.annotationsAccept(clazz, annotationVisitor);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        runtimeVisibleAnnotationsAttribute.annotationsAccept(clazz, field, annotationVisitor);
    }
    public void visitRuntimeVisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeVisibleAnnotationsAttribute runtimeVisibleAnnotationsAttribute)
    {
        runtimeVisibleAnnotationsAttribute.annotationsAccept(clazz, method, annotationVisitor);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        runtimeInvisibleAnnotationsAttribute.annotationsAccept(clazz, annotationVisitor);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Field field, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        runtimeInvisibleAnnotationsAttribute.annotationsAccept(clazz, field, annotationVisitor);
    }
    public void visitRuntimeInvisibleAnnotationsAttribute(Clazz clazz, Method method, RuntimeInvisibleAnnotationsAttribute runtimeInvisibleAnnotationsAttribute)
    {
        runtimeInvisibleAnnotationsAttribute.annotationsAccept(clazz, method, annotationVisitor);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, annotationVisitor);
    }
}
