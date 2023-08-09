import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import java.util.List;
public class FullyQualifiedClassNameChecker
extends      SimplifiedVisitor
implements   ClassVisitor
{
    private final ClassPool      programClassPool;
    private final ClassPool      libraryClassPool;
    private final WarningPrinter notePrinter;
    public FullyQualifiedClassNameChecker(ClassPool      programClassPool,
                                          ClassPool      libraryClassPool,
                                          WarningPrinter notePrinter)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
        this.notePrinter      = notePrinter;
    }
    public void checkClassSpecifications(List classSpecifications)
    {
        if (classSpecifications != null)
        {
            for (int index = 0; index < classSpecifications.size(); index++)
            {
                ClassSpecification classSpecification =
                    (ClassSpecification)classSpecifications.get(index);
                checkType(classSpecification.annotationType);
                checkClassName(classSpecification.className);
                checkType(classSpecification.extendsAnnotationType);
                checkClassName(classSpecification.extendsClassName);
                checkMemberSpecifications(classSpecification.fieldSpecifications,  true);
                checkMemberSpecifications(classSpecification.methodSpecifications, false);
            }
        }
    }
    private void checkMemberSpecifications(List memberSpecifications, boolean isField)
    {
        if (memberSpecifications != null)
        {
            for (int index = 0; index < memberSpecifications.size(); index++)
            {
                MemberSpecification memberSpecification =
                    (MemberSpecification)memberSpecifications.get(index);
                checkType(memberSpecification.annotationType);
                if (isField)
                {
                     checkType(memberSpecification.descriptor);
                }
                else
                {
                    checkDescriptor(memberSpecification.descriptor);
                }
            }
        }
    }
    private void checkDescriptor(String descriptor)
    {
        if (descriptor != null)
        {
            InternalTypeEnumeration internalTypeEnumeration =
                new InternalTypeEnumeration(descriptor);
            checkType(internalTypeEnumeration.returnType());
            while (internalTypeEnumeration.hasMoreTypes())
            {
                checkType(internalTypeEnumeration.nextType());
            }
        }
    }
    private void checkType(String type)
    {
        if (type != null)
        {
            checkClassName(ClassUtil.internalClassNameFromType(type));
        }
    }
    private void checkClassName(String className)
    {
        if (className != null                            &&
            !containsWildCards(className)                &&
            programClassPool.getClass(className) == null &&
            libraryClassPool.getClass(className) == null &&
            notePrinter.accepts(className))
        {
            notePrinter.print(className,
                              "Note: the configuration refers to the unknown class '" +
                              ClassUtil.externalClassName(className) + "'");
            String fullyQualifiedClassName =
                "**" + ClassConstants.INTERNAL_PACKAGE_SEPARATOR +
                className.substring(className.lastIndexOf(ClassConstants.INTERNAL_PACKAGE_SEPARATOR)+1);
            ClassNameFilter classNameFilter =
                new ClassNameFilter(fullyQualifiedClassName, this);
            programClassPool.classesAccept(classNameFilter);
            libraryClassPool.classesAccept(classNameFilter);
        }
    }
    private static boolean containsWildCards(String string)
    {
        return string != null &&
            (string.indexOf('*')   >= 0 ||
             string.indexOf('?')   >= 0 ||
             string.indexOf(',')   >= 0 ||
             string.indexOf("
    }
    public void visitAnyClass(Clazz clazz)
    {
        System.out.println("      Maybe you meant the fully qualified name '" +
                           ClassUtil.externalClassName(clazz.getName()) + "'?");
    }
}
