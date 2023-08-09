package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.annotation.Annotation;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class AnnotatedClassVisitor
extends      SimplifiedVisitor
implements   AnnotationVisitor
{
    private final ClassVisitor classVisitor;
    private Clazz lastVisitedClass;
    public AnnotatedClassVisitor(ClassVisitor classVisitor)
    {
        this.classVisitor = classVisitor;
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        if (!clazz.equals(lastVisitedClass))
        {
            clazz.accept(classVisitor);
            lastVisitedClass = clazz;
        }
    }
}
