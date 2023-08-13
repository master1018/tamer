package proguard.classfile.attribute.annotation.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.annotation.*;
public interface ElementValueVisitor
{
    public void visitConstantElementValue(    Clazz clazz,                Annotation annotation, ConstantElementValue     constantElementValue);
    public void visitEnumConstantElementValue(Clazz clazz,                Annotation annotation, EnumConstantElementValue enumConstantElementValue);
    public void visitClassElementValue(       Clazz clazz,                Annotation annotation, ClassElementValue        classElementValue);
    public void visitAnnotationElementValue(  Clazz clazz,                Annotation annotation, AnnotationElementValue   annotationElementValue);
    public void visitArrayElementValue(       Clazz clazz,                Annotation annotation, ArrayElementValue        arrayElementValue);
}
