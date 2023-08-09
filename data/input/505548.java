import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.*;
public class AnnotationUsageMarker
extends      SimplifiedVisitor
implements   AttributeVisitor,
             AnnotationVisitor,
             ElementValueVisitor,
             ConstantVisitor,
             ClassVisitor,
             MemberVisitor
{
    private final UsageMarker usageMarker;
    private boolean attributeUsed;
    private boolean annotationUsed;
    private boolean elementValueUsed;
    private boolean classUsed;
    private boolean methodUsed;
    public AnnotationUsageMarker(UsageMarker usageMarker)
    {
        this.usageMarker = usageMarker;
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        attributeUsed = false;
        annotationsAttribute.annotationsAccept(clazz, this);
        if (attributeUsed)
        {
            usageMarker.markAsUsed(annotationsAttribute);
            markConstant(clazz, annotationsAttribute.u2attributeNameIndex);
        }
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        attributeUsed = false;
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
        if (attributeUsed)
        {
            usageMarker.markAsUsed(parameterAnnotationsAttribute);
            markConstant(clazz, parameterAnnotationsAttribute.u2attributeNameIndex);
        }
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
        usageMarker.markAsUsed(annotationDefaultAttribute);
        markConstant(clazz, annotationDefaultAttribute.u2attributeNameIndex);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        if (isReferencedClassUsed(annotation))
        {
            usageMarker.markAsUsed(annotation);
            markConstant(clazz, annotation.u2typeIndex);
            annotation.elementValuesAccept(clazz, this);
            annotationUsed = true;
            attributeUsed  = true;
        }
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        if (isReferencedMethodUsed(constantElementValue))
        {
            usageMarker.markAsUsed(constantElementValue);
            markConstant(clazz, constantElementValue.u2elementNameIndex);
            markConstant(clazz, constantElementValue.u2constantValueIndex);
            elementValueUsed = true;
        }
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        if (isReferencedMethodUsed(enumConstantElementValue))
        {
            classUsed = true;
            enumConstantElementValue.referencedClassesAccept(usageMarker);
            if (classUsed)
            {
                usageMarker.markAsUsed(enumConstantElementValue);
                markConstant(clazz, enumConstantElementValue.u2elementNameIndex);
                markConstant(clazz, enumConstantElementValue.u2typeNameIndex);
                markConstant(clazz, enumConstantElementValue.u2constantNameIndex);
                elementValueUsed = true;
            }
        }
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        if (isReferencedMethodUsed(classElementValue))
        {
            classUsed = true;
            classElementValue.referencedClassesAccept(usageMarker);
            if (classUsed)
            {
                usageMarker.markAsUsed(classElementValue);
                markConstant(clazz, classElementValue.u2elementNameIndex);
                markConstant(clazz, classElementValue.u2classInfoIndex);
                elementValueUsed = true;
            }
        }
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        if (isReferencedMethodUsed(annotationElementValue))
        {
            boolean oldAnnotationUsed = annotationUsed;
            annotationUsed = false;
            annotationElementValue.annotationAccept(clazz, this);
            if (annotationUsed)
            {
                usageMarker.markAsUsed(annotationElementValue);
                markConstant(clazz, annotationElementValue.u2elementNameIndex);
                elementValueUsed = true;
            }
            annotationUsed = oldAnnotationUsed;
        }
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        if (isReferencedMethodUsed(arrayElementValue))
        {
            boolean oldelementValueUsed = elementValueUsed;
            elementValueUsed = false;
            arrayElementValue.elementValuesAccept(clazz, annotation, this);
            if (elementValueUsed)
            {
                usageMarker.markAsUsed(arrayElementValue);
                markConstant(clazz, arrayElementValue.u2elementNameIndex);
            }
            else
            {
                elementValueUsed = oldelementValueUsed;
            }
        }
    }
    public void visitAnyConstant(Clazz clazz, Constant constant)
    {
        usageMarker.markAsUsed(constant);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classUsed = usageMarker.isUsed(classConstant);
        if (!classUsed)
        {
            classUsed = true;
            classConstant.referencedClassAccept(this);
            if (classUsed)
            {
                usageMarker.markAsUsed(classConstant);
                markConstant(clazz, classConstant.u2nameIndex);
            }
        }
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        classUsed = usageMarker.isUsed(programClass);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        classUsed = true;
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        methodUsed = usageMarker.isUsed(programMethod);
    }
    public void visitLibraryMethod(LibraryClass LibraryClass, LibraryMethod libraryMethod)
    {
        classUsed = true;
    }
    private boolean isReferencedClassUsed(Annotation annotation)
    {
        classUsed = true;
        annotation.referencedClassAccept(this);
        return classUsed;
    }
    private boolean isReferencedMethodUsed(ElementValue elementValue)
    {
        methodUsed = true;
        elementValue.referencedMethodAccept(this);
        return methodUsed;
    }
    private void markConstant(Clazz clazz, int index)
    {
        if (index > 0)
        {
            clazz.constantPoolEntryAccept(index, this);
        }
    }
}
