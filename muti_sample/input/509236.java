package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.Annotation;
public interface AnnotationVisitor
{
    public void visitAnnotation(Clazz clazz,                                    Annotation annotation);
    public void visitAnnotation(Clazz clazz, Field  field,                      Annotation annotation);
    public void visitAnnotation(Clazz clazz, Method method,                     Annotation annotation);
    public void visitAnnotation(Clazz clazz, Method method, int parameterIndex, Annotation annotation);
}
