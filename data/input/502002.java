import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.editor.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
public class ClassShrinker
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             AttributeVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final UsageMarker usageMarker;
    private int[] constantIndexMap = new int[ClassConstants.TYPICAL_CONSTANT_POOL_SIZE];
    private final ConstantPoolRemapper constantPoolRemapper = new ConstantPoolRemapper();
    public ClassShrinker(UsageMarker usageMarker)
    {
        this.usageMarker = usageMarker;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.u2interfacesCount =
            shrinkConstantIndexArray(programClass.constantPool,
                                     programClass.u2interfaces,
                                     programClass.u2interfacesCount);
        programClass.u2constantPoolCount =
            shrinkConstantPool(programClass.constantPool,
                               programClass.u2constantPoolCount);
        programClass.u2fieldsCount =
            shrinkArray(programClass.fields,
                        programClass.u2fieldsCount);
        programClass.u2methodsCount =
            shrinkArray(programClass.methods,
                        programClass.u2methodsCount);
        programClass.u2attributesCount =
            shrinkArray(programClass.attributes,
                        programClass.u2attributesCount);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
        constantPoolRemapper.setConstantIndexMap(constantIndexMap);
        constantPoolRemapper.visitProgramClass(programClass);
        programClass.attributesAccept(new SignatureShrinker());
        programClass.subClasses =
            shrinkToNewArray(programClass.subClasses);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.subClasses =
            shrinkToNewArray(libraryClass.subClasses);
    }
    public void visitProgramMember(ProgramClass programClass, ProgramMember programMember)
    {
        programMember.u2attributesCount =
            shrinkArray(programMember.attributes,
                        programMember.u2attributesCount);
        programMember.attributesAccept(programClass, this);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.u2classesCount =
            shrinkArray(innerClassesAttribute.classes,
                        innerClassesAttribute.u2classesCount);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        if (enclosingMethodAttribute.referencedMethod != null &&
            !usageMarker.isUsed(enclosingMethodAttribute.referencedMethod))
        {
            enclosingMethodAttribute.u2nameAndTypeIndex = 0;
            enclosingMethodAttribute.referencedMethod = null;
        }
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.u2attributesCount =
            shrinkArray(codeAttribute.attributes,
                        codeAttribute.u2attributesCount);
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute.u2annotationsCount =
            shrinkArray(annotationsAttribute.annotations,
                        annotationsAttribute.u2annotationsCount);
        annotationsAttribute.annotationsAccept(clazz, this);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        for (int parameterIndex = 0; parameterIndex < parameterAnnotationsAttribute.u2parametersCount; parameterIndex++)
        {
            parameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex] =
                shrinkArray(parameterAnnotationsAttribute.parameterAnnotations[parameterIndex],
                            parameterAnnotationsAttribute.u2parameterAnnotationsCount[parameterIndex]);
        }
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.u2elementValuesCount =
            shrinkArray(annotation.elementValues,
                        annotation.u2elementValuesCount);
        annotation.elementValuesAccept(clazz, this);
    }
    private class SignatureShrinker
    extends       SimplifiedVisitor
    implements    AttributeVisitor
    {
        public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
        public void visitSignatureAttribute(Clazz clazz, SignatureAttribute  signatureAttribute)
        {
            Clazz[] referencedClasses = signatureAttribute.referencedClasses;
            if (referencedClasses != null)
            {
                String signature = clazz.getString(signatureAttribute.u2signatureIndex);
                InternalTypeEnumeration internalTypeEnumeration =
                    new InternalTypeEnumeration(signature);
                StringBuffer newSignatureBuffer = new StringBuffer();
                int referencedClassIndex    = 0;
                int newReferencedClassIndex = 0;
                while (internalTypeEnumeration.hasMoreTypes())
                {
                    String type       = internalTypeEnumeration.nextType();
                    int    classCount = new DescriptorClassEnumeration(type).classCount();
                    Clazz referencedClass = referencedClasses[referencedClassIndex];
                    if (referencedClass == null ||
                        usageMarker.isUsed(referencedClass))
                    {
                        newSignatureBuffer.append(type);
                        for (int counter = 0; counter < classCount; counter++)
                        {
                            referencedClasses[newReferencedClassIndex++] =
                                referencedClasses[referencedClassIndex++];
                        }
                    }
                    else
                    {
                        referencedClassIndex += classCount;
                    }
                }
                if (newReferencedClassIndex < referencedClassIndex)
                {
                    ((Utf8Constant)((ProgramClass)clazz).constantPool[signatureAttribute.u2signatureIndex]).setString(newSignatureBuffer.toString());
                    while (newReferencedClassIndex < referencedClassIndex)
                    {
                        referencedClasses[newReferencedClassIndex++] = null;
                    }
                }
            }
        }
    }
    public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue) {}
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        arrayElementValue.u2elementValuesCount =
            shrinkArray(arrayElementValue.elementValues,
                        arrayElementValue.u2elementValuesCount);
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private int shrinkConstantPool(Constant[] constantPool, int length)
    {
        if (constantIndexMap.length < length)
        {
            constantIndexMap = new int[length];
        }
        int     counter = 1;
        boolean isUsed  = false;
        for (int index = 1; index < length; index++)
        {
            constantIndexMap[index] = counter;
            Constant constant = constantPool[index];
            if (constant != null)
            {
                isUsed = usageMarker.isUsed(constant);
            }
            if (isUsed)
            {
                constantPool[counter++] = constant;
            }
        }
        for (int index = counter; index < length; index++)
        {
            constantPool[index] = null;
        }
        return counter;
    }
    private int shrinkConstantIndexArray(Constant[] constantPool, int[] array, int length)
    {
        int counter = 0;
        for (int index = 0; index < length; index++)
        {
            if (usageMarker.isUsed(constantPool[array[index]]))
            {
                array[counter++] = array[index];
            }
        }
        for (int index = counter; index < length; index++)
        {
            array[index] = 0;
        }
        return counter;
    }
    private Clazz[] shrinkToNewArray(Clazz[] array)
    {
        if (array == null)
        {
            return null;
        }
        int length = shrinkArray(array, array.length);
        if (length == 0)
        {
            return null;
        }
        if (length == array.length)
        {
            return array;
        }
        Clazz[] newArray = new Clazz[length];
        System.arraycopy(array, 0, newArray, 0, length);
        return newArray;
    }
    private int shrinkArray(VisitorAccepter[] array, int length)
    {
        int counter = 0;
        for (int index = 0; index < length; index++)
        {
            if (usageMarker.isUsed(array[index]))
            {
                array[counter++] = array[index];
            }
        }
        for (int index = counter; index < length; index++)
        {
            array[index] = null;
        }
        return counter;
    }
}
