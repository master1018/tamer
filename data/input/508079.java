package proguard.classfile.editor;
import proguard.classfile.attribute.annotation.*;
public class AnnotationsAttributeEditor
{
    private AnnotationsAttribute targetAnnotationsAttribute;
    public AnnotationsAttributeEditor(AnnotationsAttribute targetAnnotationsAttribute)
    {
        this.targetAnnotationsAttribute = targetAnnotationsAttribute;
    }
    public void addAnnotation(Annotation annotation)
    {
        int          annotationsCount = targetAnnotationsAttribute.u2annotationsCount;
        Annotation[] annotations      = targetAnnotationsAttribute.annotations;
        if (annotations.length <= annotationsCount)
        {
            targetAnnotationsAttribute.annotations = new Annotation[annotationsCount+1];
            System.arraycopy(annotations, 0,
                             targetAnnotationsAttribute.annotations, 0,
                             annotationsCount);
            annotations = targetAnnotationsAttribute.annotations;
        }
        annotations[targetAnnotationsAttribute.u2annotationsCount++] = annotation;
    }
}