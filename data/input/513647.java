package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.visitor.AnnotationVisitor;
public abstract class ParameterAnnotationsAttribute extends Attribute
{
    public int            u2parametersCount;
    public int[]          u2parameterAnnotationsCount;
    public Annotation[][] parameterAnnotations;
    protected ParameterAnnotationsAttribute()
    {
    }
    protected ParameterAnnotationsAttribute(int            u2attributeNameIndex,
                                            int            u2parametersCount,
                                            int[]          u2parameterAnnotationsCount,
                                            Annotation[][] parameterAnnotations)
    {
        super(u2attributeNameIndex);
        this.u2parametersCount           = u2parametersCount;
        this.u2parameterAnnotationsCount = u2parameterAnnotationsCount;
        this.parameterAnnotations        = parameterAnnotations;
    }
    public void annotationsAccept(Clazz clazz, Method method, AnnotationVisitor annotationVisitor)
    {
        for (int parameterIndex = 0; parameterIndex < u2parametersCount; parameterIndex++)
        {
            int          annotationsCount = u2parameterAnnotationsCount[parameterIndex];
            Annotation[] annotations      = parameterAnnotations[parameterIndex];
            for (int index = 0; index < annotationsCount; index++)
            {
                annotationVisitor.visitAnnotation(clazz, method, parameterIndex, annotations[index]);
            }
        }
    }
}
