package proguard.classfile.attribute.annotation;
import proguard.classfile.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
public class RuntimeInvisibleParameterAnnotationsAttribute extends ParameterAnnotationsAttribute
{
    public RuntimeInvisibleParameterAnnotationsAttribute()
    {
    }
    public RuntimeInvisibleParameterAnnotationsAttribute(int            u2attributeNameIndex,
                                                         int            u2parametersCount,
                                                         int[]          u2parameterAnnotationsCount,
                                                         Annotation[][] parameterAnnotations)
    {
        super(u2attributeNameIndex,
              u2parametersCount,
              u2parameterAnnotationsCount,
              parameterAnnotations);
    }
    public void accept(Clazz clazz, Method method, AttributeVisitor attributeVisitor)
    {
        attributeVisitor.visitRuntimeInvisibleParameterAnnotationsAttribute(clazz, method, this);
    }
}
