import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.AllAttributeVisitor;
import proguard.classfile.visitor.*;
import java.util.List;
public class ClassSpecificationVisitorFactory
{
    public static ClassPoolVisitor createClassPoolVisitor(List          keepClassSpecifications,
                                                          ClassVisitor  classVisitor,
                                                          MemberVisitor memberVisitor,
                                                          boolean       shrinking,
                                                          boolean       optimizing,
                                                          boolean       obfuscating)
    {
        MultiClassPoolVisitor multiClassPoolVisitor = new MultiClassPoolVisitor();
        if (keepClassSpecifications != null)
        {
            for (int index = 0; index < keepClassSpecifications.size(); index++)
            {
                KeepClassSpecification keepClassSpecification =
                    (KeepClassSpecification)keepClassSpecifications.get(index);
                if ((shrinking   && !keepClassSpecification.allowShrinking)    ||
                    (optimizing  && !keepClassSpecification.allowOptimization) ||
                    (obfuscating && !keepClassSpecification.allowObfuscation))
                {
                    multiClassPoolVisitor.addClassPoolVisitor(
                        createClassPoolVisitor(keepClassSpecification,
                                               classVisitor,
                                               memberVisitor));
                }
            }
        }
        return multiClassPoolVisitor;
    }
    public static ClassPoolVisitor createClassPoolVisitor(List          classSpecifications,
                                                          ClassVisitor  classVisitor,
                                                          MemberVisitor memberVisitor)
    {
        MultiClassPoolVisitor multiClassPoolVisitor = new MultiClassPoolVisitor();
        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                ClassSpecification classSpecification =
                    (ClassSpecification)classSpecifications.get(index);
                multiClassPoolVisitor.addClassPoolVisitor(
                    createClassPoolVisitor(classSpecification,
                                           classVisitor,
                                           memberVisitor));
            }
        }
        return multiClassPoolVisitor;
    }
    private static ClassPoolVisitor createClassPoolVisitor(KeepClassSpecification keepClassSpecification,
                                                           ClassVisitor      classVisitor,
                                                           MemberVisitor     memberVisitor)
    {
        if (!keepClassSpecification.markClasses &&
            !keepClassSpecification.markConditionally)
        {
            classVisitor = null;
        }
        if (keepClassSpecification.markConditionally)
        {
            ClassVisitor composedClassVisitor =
                createCombinedClassVisitor(keepClassSpecification,
                                           classVisitor,
                                           memberVisitor);
            classVisitor =
                createClassMemberTester(keepClassSpecification,
                                        composedClassVisitor);
            memberVisitor = null;
        }
        return createClassPoolVisitor((ClassSpecification)keepClassSpecification,
                                      classVisitor,
                                      memberVisitor);
    }
    private static ClassPoolVisitor createClassPoolVisitor(ClassSpecification classSpecification,
                                                           ClassVisitor       classVisitor,
                                                           MemberVisitor      memberVisitor)
    {
        ClassVisitor composedClassVisitor =
            createCombinedClassVisitor(classSpecification,
                                       classVisitor,
                                       memberVisitor);
        String className = classSpecification.className;
        String extendsAnnotationType = classSpecification.extendsAnnotationType;
        String extendsClassName      = classSpecification.extendsClassName;
        if (className != null &&
            (extendsAnnotationType != null ||
             extendsClassName      != null ||
             containsWildCards(className)))
        {
            composedClassVisitor =
                new ClassNameFilter(className, composedClassVisitor);
            className = null;
        }
        String annotationType = classSpecification.annotationType;
        if (annotationType != null)
        {
            composedClassVisitor =
                new AllAttributeVisitor(
                new AllAnnotationVisitor(
                new AnnotationTypeFilter(annotationType,
                new AnnotatedClassVisitor(composedClassVisitor))));
        }
        if (classSpecification.requiredSetAccessFlags   != 0 ||
            classSpecification.requiredUnsetAccessFlags != 0)
        {
            composedClassVisitor =
                new ClassAccessFilter(classSpecification.requiredSetAccessFlags,
                                      classSpecification.requiredUnsetAccessFlags,
                                      composedClassVisitor);
        }
        if (extendsAnnotationType != null ||
            extendsClassName      != null)
        {
            composedClassVisitor =
                new ClassHierarchyTraveler(false, false, false, true,
                                           composedClassVisitor);
            if (extendsAnnotationType != null)
            {
                composedClassVisitor =
                    new AllAttributeVisitor(
                    new AllAnnotationVisitor(
                    new AnnotationTypeFilter(extendsAnnotationType,
                    new AnnotatedClassVisitor(composedClassVisitor))));
            }
            if (extendsClassName != null)
            {
                if (containsWildCards(extendsClassName))
                {
                    composedClassVisitor =
                        new ClassNameFilter(extendsClassName,
                                            composedClassVisitor);
                }
                else
                {
                    className = extendsClassName;
                }
            }
        }
        return className != null ?
            (ClassPoolVisitor)new NamedClassVisitor(composedClassVisitor, className) :
            (ClassPoolVisitor)new AllClassVisitor(composedClassVisitor);
    }
    private static ClassVisitor createCombinedClassVisitor(ClassSpecification classSpecification,
                                                           ClassVisitor       classVisitor,
                                                           MemberVisitor      memberVisitor)
    {
        if (classSpecification.fieldSpecifications  == null &&
            classSpecification.methodSpecifications == null)
        {
            memberVisitor = null;
        }
        MultiClassVisitor multiClassVisitor = new MultiClassVisitor();
        if (classVisitor != null)
        {
            if (memberVisitor == null)
            {
                return classVisitor;
            }
            multiClassVisitor.addClassVisitor(classVisitor);
        }
        if (memberVisitor != null)
        {
            ClassVisitor memberClassVisitor =
                createClassVisitor(classSpecification, memberVisitor);
            if (classVisitor == null)
            {
                return memberClassVisitor;
            }
            multiClassVisitor.addClassVisitor(memberClassVisitor);
        }
        return multiClassVisitor;
    }
    private static ClassVisitor createClassVisitor(ClassSpecification classSpecification,
                                                   MemberVisitor      memberVisitor)
    {
        MultiClassVisitor multiClassVisitor = new MultiClassVisitor();
        addMemberVisitors(classSpecification.fieldSpecifications,  true,  multiClassVisitor, memberVisitor);
        addMemberVisitors(classSpecification.methodSpecifications, false, multiClassVisitor, memberVisitor);
        return new ClassHierarchyTraveler(true, true, false, false,
                                          multiClassVisitor);
    }
    private static void addMemberVisitors(List              memberSpecifications,
                                          boolean           isField,
                                          MultiClassVisitor multiClassVisitor,
                                          MemberVisitor     memberVisitor)
    {
        if (memberSpecifications != null)
        {
            for (int index = 0; index < memberSpecifications.size(); index++)
            {
                MemberSpecification memberSpecification =
                    (MemberSpecification)memberSpecifications.get(index);
                multiClassVisitor.addClassVisitor(
                    createClassVisitor(memberSpecification,
                                       isField,
                                       memberVisitor));
            }
        }
    }
    private static ClassVisitor createClassMemberTester(ClassSpecification classSpecification,
                                                        ClassVisitor       classVisitor)
    {
        return createClassMemberTester(classSpecification.fieldSpecifications,
                                       true,
               createClassMemberTester(classSpecification.methodSpecifications,
                                       false,
                                       classVisitor));
    }
    private static ClassVisitor createClassMemberTester(List         memberSpecifications,
                                                        boolean      isField,
                                                        ClassVisitor classVisitor)
    {
        if (memberSpecifications != null)
        {
            for (int index = 0; index < memberSpecifications.size(); index++)
            {
                MemberSpecification memberSpecification =
                    (MemberSpecification)memberSpecifications.get(index);
                classVisitor =
                    createClassVisitor(memberSpecification,
                                       isField,
                                       new MemberToClassVisitor(classVisitor));
            }
        }
        return classVisitor;
    }
    private static ClassVisitor createClassVisitor(MemberSpecification memberSpecification,
                                                   boolean             isField,
                                                   MemberVisitor       memberVisitor)
    {
        String name       = memberSpecification.name;
        String descriptor = memberSpecification.descriptor;
        boolean fullySpecified =
            name       != null &&
            descriptor != null &&
            !containsWildCards(name) &&
            !containsWildCards(descriptor);
        if (!fullySpecified)
        {
            if (descriptor != null)
            {
                memberVisitor =
                    new MemberDescriptorFilter(descriptor, memberVisitor);
            }
            if (name != null)
            {
                memberVisitor =
                    new MemberNameFilter(name, memberVisitor);
            }
        }
        if (memberSpecification.annotationType != null)
        {
            memberVisitor =
                new AllAttributeVisitor(
                new AllAnnotationVisitor(
                new AnnotationTypeFilter(memberSpecification.annotationType,
                new AnnotationToMemberVisitor(memberVisitor))));
        }
        if (memberSpecification.requiredSetAccessFlags   != 0 ||
            memberSpecification.requiredUnsetAccessFlags != 0)
        {
            memberVisitor =
                new MemberAccessFilter(memberSpecification.requiredSetAccessFlags,
                                       memberSpecification.requiredUnsetAccessFlags,
                                       memberVisitor);
        }
        return isField ?
            fullySpecified ?
                (ClassVisitor)new NamedFieldVisitor(name, descriptor, memberVisitor) :
                (ClassVisitor)new AllFieldVisitor(memberVisitor) :
            fullySpecified ?
                (ClassVisitor)new NamedMethodVisitor(name, descriptor, memberVisitor) :
                (ClassVisitor)new AllMethodVisitor(memberVisitor);
    }
    private static boolean containsWildCards(String string)
    {
        return string != null &&
            (string.indexOf('*')   >= 0 ||
             string.indexOf('?')   >= 0 ||
             string.indexOf('%')   >= 0 ||
             string.indexOf(',')   >= 0 ||
             string.indexOf("
    }
}
