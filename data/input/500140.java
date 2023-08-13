package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
public class ElementValueAdder
implements   ElementValueVisitor
{
    private static final ElementValue[] EMPTY_ELEMENT_VALUES = new ElementValue[0];
    private final ProgramClass               targetClass;
    private final AnnotationDefaultAttribute targetAnnotationDefaultAttribute;
    private final ConstantAdder       constantAdder;
    private final ElementValuesEditor elementValuesEditor;
    public ElementValueAdder(ProgramClass               targetClass,
                             AnnotationDefaultAttribute targetAnnotationDefaultAttribute,
                             boolean                    replaceElementValues)
    {
        this.targetClass                      = targetClass;
        this.targetAnnotationDefaultAttribute = targetAnnotationDefaultAttribute;
        constantAdder       = new ConstantAdder(targetClass);
        elementValuesEditor = null;
    }
    public ElementValueAdder(ProgramClass targetClass,
                             Annotation   targetAnnotation,
                             boolean      replaceElementValues)
    {
        this.targetClass                      = targetClass;
        this.targetAnnotationDefaultAttribute = null;
        constantAdder       = new ConstantAdder(targetClass);
        elementValuesEditor = new ElementValuesEditor(targetClass,
                                                      targetAnnotation,
                                                      replaceElementValues);
    }
    public ElementValueAdder(ProgramClass      targetClass,
                             ArrayElementValue targetArrayElementValue,
                             boolean           replaceElementValues)
    {
        this.targetClass                      = targetClass;
        this.targetAnnotationDefaultAttribute = null;
        constantAdder       = new ConstantAdder(targetClass);
        elementValuesEditor = new ElementValuesEditor(targetClass,
                                                      targetArrayElementValue,
                                                      replaceElementValues);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        ConstantElementValue newConstantElementValue =
            new ConstantElementValue(constantElementValue.u1tag,
                                     constantElementValue.u2elementNameIndex == 0 ? 0 :
                                     constantAdder.addConstant(clazz, constantElementValue.u2elementNameIndex),
                                     constantAdder.addConstant(clazz, constantElementValue.u2constantValueIndex));
        newConstantElementValue.referencedClass  = constantElementValue.referencedClass;
        newConstantElementValue.referencedMethod = constantElementValue.referencedMethod;
        addElementValue(newConstantElementValue);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        EnumConstantElementValue newEnumConstantElementValue =
            new EnumConstantElementValue(enumConstantElementValue.u2elementNameIndex == 0 ? 0 :
                                         constantAdder.addConstant(clazz, enumConstantElementValue.u2elementNameIndex),
                                         constantAdder.addConstant(clazz, enumConstantElementValue.u2typeNameIndex),
                                         constantAdder.addConstant(clazz, enumConstantElementValue.u2constantNameIndex));
        newEnumConstantElementValue.referencedClass  = enumConstantElementValue.referencedClass;
        newEnumConstantElementValue.referencedMethod = enumConstantElementValue.referencedMethod;
        newEnumConstantElementValue.referencedClasses = enumConstantElementValue.referencedClasses;
        addElementValue(newEnumConstantElementValue);
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        ClassElementValue newClassElementValue =
            new ClassElementValue(classElementValue.u2elementNameIndex == 0 ? 0 :
                                  constantAdder.addConstant(clazz, classElementValue.u2elementNameIndex),
                                  constantAdder.addConstant(clazz, classElementValue.u2classInfoIndex));
        newClassElementValue.referencedClass  = classElementValue.referencedClass;
        newClassElementValue.referencedMethod = classElementValue.referencedMethod;
        newClassElementValue.referencedClasses = classElementValue.referencedClasses;
        addElementValue(newClassElementValue);
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        AnnotationElementValue newAnnotationElementValue =
            new AnnotationElementValue(annotationElementValue.u2elementNameIndex == 0 ? 0 :
                                       constantAdder.addConstant(clazz, annotationElementValue.u2elementNameIndex),
                                       new Annotation());
        newAnnotationElementValue.referencedClass  = annotationElementValue.referencedClass;
        newAnnotationElementValue.referencedMethod = annotationElementValue.referencedMethod;
        annotationElementValue.annotationAccept(clazz,
                                                new AnnotationAdder(targetClass,
                                                                    newAnnotationElementValue));
        addElementValue(newAnnotationElementValue);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        ArrayElementValue newArrayElementValue =
            new ArrayElementValue(arrayElementValue.u2elementNameIndex == 0 ? 0 :
                                  constantAdder.addConstant(clazz, arrayElementValue.u2elementNameIndex),
                                  0,
                                  arrayElementValue.u2elementValuesCount > 0 ?
                                      new ElementValue[arrayElementValue.u2elementValuesCount] :
                                      EMPTY_ELEMENT_VALUES);
        newArrayElementValue.referencedClass  = arrayElementValue.referencedClass;
        newArrayElementValue.referencedMethod = arrayElementValue.referencedMethod;
        arrayElementValue.elementValuesAccept(clazz,
                                              annotation,
                                              new ElementValueAdder(targetClass,
                                                                    newArrayElementValue,
                                                                    false));
        addElementValue(newArrayElementValue);
    }
    private void addElementValue(ElementValue newElementValue)
    {
        if (targetAnnotationDefaultAttribute != null)
        {
            targetAnnotationDefaultAttribute.defaultValue = newElementValue;
        }
        else
        {
            elementValuesEditor.addElementValue(newElementValue);
        }
    }
}