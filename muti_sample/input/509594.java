package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.Annotation;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.util.*;
public class AnnotationTypeFilter
extends      SimplifiedVisitor
implements   AnnotationVisitor
{
    private final StringMatcher     regularExpressionMatcher;
    private final AnnotationVisitor annotationVisitor;
    public AnnotationTypeFilter(String            regularExpression,
                                AnnotationVisitor annotationVisitor)
    {
        this.regularExpressionMatcher = new ListParser(new ClassNameParser()).parse(regularExpression);
        this.annotationVisitor        = annotationVisitor;
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        if (accepted(annotation.getType(clazz)))
        {
            annotationVisitor.visitAnnotation(clazz, annotation);
        }
    }
    public void visitAnnotation(Clazz clazz, Field field, Annotation annotation)
    {
        if (accepted(annotation.getType(clazz)))
        {
            annotationVisitor.visitAnnotation(clazz, field, annotation);
        }
    }
    public void visitAnnotation(Clazz clazz, Method method, Annotation annotation)
    {
        if (accepted(annotation.getType(clazz)))
        {
            annotationVisitor.visitAnnotation(clazz, method, annotation);
        }
    }
    public void visitAnnotation(Clazz clazz, Method method, int parameterIndex, Annotation annotation)
    {
        if (accepted(annotation.getType(clazz)))
        {
            annotationVisitor.visitAnnotation(clazz, method, parameterIndex, annotation);
        }
    }
    private boolean accepted(String name)
    {
        return regularExpressionMatcher.matches(name);
    }
}
