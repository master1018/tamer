package proguard.classfile.editor;
import proguard.classfile.attribute.annotation.*;
public class ParameterAnnotationsAttributeEditor
{
    private ParameterAnnotationsAttribute targetParameterAnnotationsAttribute;
    public ParameterAnnotationsAttributeEditor(ParameterAnnotationsAttribute targetParameterAnnotationsAttribute)
    {
        this.targetParameterAnnotationsAttribute = targetParameterAnnotationsAttribute;
    }
    public void addAnnotation(int parameterIndex, Annotation annotation)
    {
        int          annotationsCount = targetParameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex];
        Annotation[] annotations      = targetParameterAnnotationsAttribute.parameterAnnotations[parameterIndex];
        if (annotations == null ||
            annotations.length <= annotationsCount)
        {
            targetParameterAnnotationsAttribute.parameterAnnotations[parameterIndex] = new Annotation[annotationsCount+1];
            if (annotations != null)
            {
                System.arraycopy(annotations, 0,
                                 targetParameterAnnotationsAttribute.parameterAnnotations[parameterIndex], 0,
                                 annotationsCount);
            }
            annotations = targetParameterAnnotationsAttribute.parameterAnnotations[parameterIndex];
        }
        annotations[targetParameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex]++] = annotation;
    }
}