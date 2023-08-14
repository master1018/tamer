package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.AnnotationVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class AnnotationAdder
extends      SimplifiedVisitor
implements   AnnotationVisitor
{
    private static final ElementValue[] EMPTY_ELEMENT_VALUES = new ElementValue[0];
    private final ProgramClass                        targetClass;
    private final AnnotationElementValue              targetAnnotationElementValue;
    private final AnnotationsAttributeEditor          annotationsAttributeEditor;
    private final ParameterAnnotationsAttributeEditor parameterAnnotationsAttributeEditor;
    private final ConstantAdder constantAdder;
    public AnnotationAdder(ProgramClass           targetClass,
                           AnnotationElementValue targetAnnotationElementValue)
    {
        this.targetClass                         = targetClass;
        this.targetAnnotationElementValue        = targetAnnotationElementValue;
        this.annotationsAttributeEditor          = null;
        this.parameterAnnotationsAttributeEditor = null;
        constantAdder = new ConstantAdder(targetClass);
    }
    public AnnotationAdder(ProgramClass         targetClass,
                           AnnotationsAttribute targetAnnotationsAttribute)
    {
        this.targetClass                         = targetClass;
        this.targetAnnotationElementValue        = null;
        this.annotationsAttributeEditor          = new AnnotationsAttributeEditor(targetAnnotationsAttribute);
        this.parameterAnnotationsAttributeEditor = null;
        constantAdder = new ConstantAdder(targetClass);
    }
    public AnnotationAdder(ProgramClass                  targetClass,
                           ParameterAnnotationsAttribute targetParameterAnnotationsAttribute)
    {
        this.targetClass                         = targetClass;
        this.targetAnnotationElementValue        = null;
        this.annotationsAttributeEditor          = null;
        this.parameterAnnotationsAttributeEditor = new ParameterAnnotationsAttributeEditor(targetParameterAnnotationsAttribute);
        constantAdder = new ConstantAdder(targetClass);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        Annotation newAnnotation =
            new Annotation(constantAdder.addConstant(clazz, annotation.u2typeIndex),
                           0,
                           annotation.u2elementValuesCount > 0 ?
                               new ElementValue[annotation.u2elementValuesCount] :
                               EMPTY_ELEMENT_VALUES);
        newAnnotation.referencedClasses = annotation.referencedClasses;
        annotation.elementValuesAccept(clazz,
                                       new ElementValueAdder(targetClass,
                                                             newAnnotation,
                                                             false));
        if (targetAnnotationElementValue != null)
        {
            targetAnnotationElementValue.annotationValue = newAnnotation;
        }
        else
        {
            annotationsAttributeEditor.addAnnotation(newAnnotation);
        }
    }
    public void visitAnnotation(Clazz clazz, Method method, int parameterIndex, Annotation annotation)
    {
        Annotation newAnnotation =
            new Annotation(constantAdder.addConstant(clazz, annotation.u2typeIndex),
                           0,
                           annotation.u2elementValuesCount > 0 ?
                               new ElementValue[annotation.u2elementValuesCount] :
                               EMPTY_ELEMENT_VALUES);
        newAnnotation.referencedClasses = annotation.referencedClasses;
        annotation.elementValuesAccept(clazz,
                                       new ElementValueAdder(targetClass,
                                                             newAnnotation,
                                                             false));
        parameterAnnotationsAttributeEditor.addAnnotation(parameterIndex, newAnnotation);
    }
}