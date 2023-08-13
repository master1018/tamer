package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.*;
public class ClassReferenceInitializer
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor,
             AttributeVisitor,
             LocalVariableInfoVisitor,
             LocalVariableTypeInfoVisitor,
             AnnotationVisitor,
             ElementValueVisitor
{
    private final ClassPool      programClassPool;
    private final ClassPool      libraryClassPool;
    private final WarningPrinter missingClassWarningPrinter;
    private final WarningPrinter missingMemberWarningPrinter;
    private final WarningPrinter dependencyWarningPrinter;
    private final MemberFinder memberFinder = new MemberFinder();
    public ClassReferenceInitializer(ClassPool      programClassPool,
                                     ClassPool      libraryClassPool,
                                     WarningPrinter missingClassWarningPrinter,
                                     WarningPrinter missingMemberWarningPrinter,
                                     WarningPrinter dependencyWarningPrinter)
    {
        this.programClassPool            = programClassPool;
        this.libraryClassPool            = libraryClassPool;
        this.missingClassWarningPrinter  = missingClassWarningPrinter;
        this.missingMemberWarningPrinter = missingMemberWarningPrinter;
        this.dependencyWarningPrinter    = dependencyWarningPrinter;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(this);
        programClass.fieldsAccept(this);
        programClass.methodsAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        libraryClass.fieldsAccept(this);
        libraryClass.methodsAccept(this);
    }
    public void visitProgramField(ProgramClass programClass, ProgramField programField)
    {
        programField.referencedClass =
            findReferencedClass(programClass.getName(),
                                programField.getDescriptor(programClass));
        programField.attributesAccept(programClass, this);
    }
    public void visitProgramMethod(ProgramClass programClass, ProgramMethod programMethod)
    {
        programMethod.referencedClasses =
            findReferencedClasses(programClass.getName(),
                                  programMethod.getDescriptor(programClass));
        programMethod.attributesAccept(programClass, this);
    }
    public void visitLibraryField(LibraryClass libraryClass, LibraryField libraryField)
    {
        libraryField.referencedClass =
            findReferencedClass(libraryClass.getName(),
                                libraryField.getDescriptor(libraryClass));
    }
    public void visitLibraryMethod(LibraryClass libraryClass, LibraryMethod libraryMethod)
    {
        libraryMethod.referencedClasses =
            findReferencedClasses(libraryClass.getName(),
                                  libraryMethod.getDescriptor(libraryClass));
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.javaLangStringClass =
            findClass(clazz.getName(), ClassConstants.INTERNAL_NAME_JAVA_LANG_STRING);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        String className = refConstant.getClassName(clazz);
        Clazz referencedClass = findClass(clazz.getName(), className);
        if (referencedClass != null &&
            !ClassUtil.isInternalArrayType(className))
        {
            String name = refConstant.getName(clazz);
            String type = refConstant.getType(clazz);
            boolean isFieldRef = refConstant.getTag() == ClassConstants.CONSTANT_Fieldref;
            refConstant.referencedMember = memberFinder.findMember(clazz,
                                                                   referencedClass,
                                                                   name,
                                                                   type,
                                                                   isFieldRef);
            refConstant.referencedClass  = memberFinder.correspondingClass();
            if (refConstant.referencedMember == null)
            {
                missingMemberWarningPrinter.print(clazz.getName(),
                                                  className,
                                                  "Warning: " +
                                                  ClassUtil.externalClassName(clazz.getName()) +
                                                  ": can't find referenced " +
                                                  (isFieldRef ?
                                                      "field '"  + ClassUtil.externalFullFieldDescription(0, name, type) :
                                                      "method '" + ClassUtil.externalFullMethodDescription(className, 0, name, type)) +
                                                  "' in class " +
                                                  ClassUtil.externalClassName(className));
            }
        }
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        String className = clazz.getName();
        classConstant.referencedClass =
            findClass(className, classConstant.getName(clazz));
        classConstant.javaLangClassClass =
            findClass(className, ClassConstants.INTERNAL_NAME_JAVA_LANG_CLASS);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        String className          = clazz.getName();
        String enclosingClassName = enclosingMethodAttribute.getClassName(clazz);
        Clazz referencedClass = findClass(className, enclosingClassName);
        if (referencedClass == null)
        {
            missingClassWarningPrinter.print(className,
                                             enclosingClassName,
                                             "Warning: " +
                                             ClassUtil.externalClassName(className) +
                                             ": can't find enclosing class " +
                                             ClassUtil.externalClassName(enclosingClassName));
            return;
        }
        if (enclosingMethodAttribute.u2nameAndTypeIndex == 0)
        {
            return;
        }
        String name = enclosingMethodAttribute.getName(clazz);
        String type = enclosingMethodAttribute.getType(clazz);
        Method referencedMethod = referencedClass.findMethod(name, type);
        if (referencedMethod == null)
        {
            missingMemberWarningPrinter.print(className,
                                              enclosingClassName,
                                              "Warning: " +
                                              ClassUtil.externalClassName(className) +
                                              ": can't find enclosing method '" +
                                              ClassUtil.externalFullMethodDescription(enclosingClassName, 0, name, type) +
                                              "' in class " +
                                              ClassUtil.externalClassName(enclosingClassName));
            return;
        }
        enclosingMethodAttribute.referencedClass  = referencedClass;
        enclosingMethodAttribute.referencedMethod = referencedMethod;
    }
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        codeAttribute.attributesAccept(clazz, method, this);
    }
    public void visitLocalVariableTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTableAttribute localVariableTableAttribute)
    {
        localVariableTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitLocalVariableTypeTableAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeTableAttribute localVariableTypeTableAttribute)
    {
        localVariableTypeTableAttribute.localVariablesAccept(clazz, method, codeAttribute, this);
    }
    public void visitSignatureAttribute(Clazz clazz, SignatureAttribute signatureAttribute)
    {
        signatureAttribute.referencedClasses =
            findReferencedClasses(clazz.getName(),
                                  clazz.getString(signatureAttribute.u2signatureIndex));
    }
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute.annotationsAccept(clazz, this);
    }
    public void visitAnyParameterAnnotationsAttribute(Clazz clazz, Method method, ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        parameterAnnotationsAttribute.annotationsAccept(clazz, method, this);
    }
    public void visitAnnotationDefaultAttribute(Clazz clazz, Method method, AnnotationDefaultAttribute annotationDefaultAttribute)
    {
        annotationDefaultAttribute.defaultValueAccept(clazz, this);
    }
    public void visitLocalVariableInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableInfo localVariableInfo)
    {
        localVariableInfo.referencedClass =
            findReferencedClass(clazz.getName(),
                                clazz.getString(localVariableInfo.u2descriptorIndex));
    }
    public void visitLocalVariableTypeInfo(Clazz clazz, Method method, CodeAttribute codeAttribute, LocalVariableTypeInfo localVariableTypeInfo)
    {
        localVariableTypeInfo.referencedClasses =
            findReferencedClasses(clazz.getName(),
                                  clazz.getString(localVariableTypeInfo.u2signatureIndex));
    }
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        annotation.referencedClasses =
            findReferencedClasses(clazz.getName(),
                                  clazz.getString(annotation.u2typeIndex));
        annotation.elementValuesAccept(clazz, this);
    }
    public void visitConstantElementValue(Clazz clazz, Annotation annotation, ConstantElementValue constantElementValue)
    {
        initializeElementValue(clazz, annotation, constantElementValue);
    }
    public void visitEnumConstantElementValue(Clazz clazz, Annotation annotation, EnumConstantElementValue enumConstantElementValue)
    {
        initializeElementValue(clazz, annotation, enumConstantElementValue);
        enumConstantElementValue.referencedClasses =
            findReferencedClasses(clazz.getName(),
                                  clazz.getString(enumConstantElementValue.u2typeNameIndex));
    }
    public void visitClassElementValue(Clazz clazz, Annotation annotation, ClassElementValue classElementValue)
    {
        initializeElementValue(clazz, annotation, classElementValue);
        classElementValue.referencedClasses =
            findReferencedClasses(clazz.getName(),
                                  clazz.getString(classElementValue.u2classInfoIndex));
    }
    public void visitAnnotationElementValue(Clazz clazz, Annotation annotation, AnnotationElementValue annotationElementValue)
    {
        initializeElementValue(clazz, annotation, annotationElementValue);
        annotationElementValue.annotationAccept(clazz, this);
    }
    public void visitArrayElementValue(Clazz clazz, Annotation annotation, ArrayElementValue arrayElementValue)
    {
        initializeElementValue(clazz, annotation, arrayElementValue);
        arrayElementValue.elementValuesAccept(clazz, annotation, this);
    }
    private void initializeElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
    {
        if (annotation                      != null &&
            annotation.referencedClasses    != null &&
            elementValue.u2elementNameIndex != 0)
        {
            String name = clazz.getString(elementValue.u2elementNameIndex);
            Clazz referencedClass = annotation.referencedClasses[0];
            elementValue.referencedClass  = referencedClass;
            elementValue.referencedMethod = referencedClass.findMethod(name, null);
        }
    }
    private Clazz findReferencedClass(String referencingClassName,
                                      String descriptor)
    {
        DescriptorClassEnumeration enumeration =
            new DescriptorClassEnumeration(descriptor);
        enumeration.nextFluff();
        if (enumeration.hasMoreClassNames())
        {
            return findClass(referencingClassName, enumeration.nextClassName());
        }
        return null;
    }
    private Clazz[] findReferencedClasses(String referencingClassName,
                                          String descriptor)
    {
        DescriptorClassEnumeration enumeration =
            new DescriptorClassEnumeration(descriptor);
        int classCount = enumeration.classCount();
        if (classCount > 0)
        {
            Clazz[] referencedClasses = new Clazz[classCount];
            boolean foundReferencedClasses = false;
            for (int index = 0; index < classCount; index++)
            {
                String fluff = enumeration.nextFluff();
                String name  = enumeration.nextClassName();
                Clazz referencedClass = findClass(referencingClassName, name);
                if (referencedClass != null)
                {
                    referencedClasses[index] = referencedClass;
                    foundReferencedClasses = true;
                }
            }
            if (foundReferencedClasses)
            {
                return referencedClasses;
            }
        }
        return null;
    }
    private Clazz findClass(String referencingClassName, String name)
    {
        if (ClassUtil.isInternalArrayType(name) &&
            !ClassUtil.isInternalClassType(name))
        {
            return null;
        }
        Clazz clazz = programClassPool.getClass(name);
        if (clazz == null)
        {
            clazz = libraryClassPool.getClass(name);
            if (clazz == null &&
                missingClassWarningPrinter != null)
            {
                missingClassWarningPrinter.print(referencingClassName,
                                                 name,
                                                 "Warning: " +
                                                 ClassUtil.externalClassName(referencingClassName) +
                                                 ": can't find referenced class " +
                                                 ClassUtil.externalClassName(name));
            }
        }
        else if (dependencyWarningPrinter != null)
        {
            dependencyWarningPrinter.print(referencingClassName,
                                           name,
                                           "Warning: library class " +
                                           ClassUtil.externalClassName(referencingClassName) +
                                           " depends on program class " +
                                           ClassUtil.externalClassName(name));
        }
        return clazz;
    }
}
