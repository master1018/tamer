package proguard.classfile.util;
import proguard.classfile.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class ClassSuperHierarchyInitializer
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor
{
    private final ClassPool      programClassPool;
    private final ClassPool      libraryClassPool;
    private final WarningPrinter missingWarningPrinter;
    private final WarningPrinter dependencyWarningPrinter;
    public ClassSuperHierarchyInitializer(ClassPool      programClassPool,
                                          ClassPool      libraryClassPool,
                                          WarningPrinter missingWarningPrinter,
                                          WarningPrinter dependencyWarningPrinter)
    {
        this.programClassPool         = programClassPool;
        this.libraryClassPool         = libraryClassPool;
        this.missingWarningPrinter    = missingWarningPrinter;
        this.dependencyWarningPrinter = dependencyWarningPrinter;
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.superClassConstantAccept(this);
        programClass.interfaceConstantsAccept(this);
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        String className = libraryClass.getName();
        String superClassName = libraryClass.superClassName;
        if (superClassName != null)
        {
            libraryClass.superClass = findClass(className, superClassName);
        }
        if (libraryClass.interfaceNames != null)
        {
            String[] interfaceNames   = libraryClass.interfaceNames;
            Clazz[]  interfaceClasses = new Clazz[interfaceNames.length];
            for (int index = 0; index < interfaceNames.length; index++)
            {
                interfaceClasses[index] =
                    findClass(className, interfaceNames[index]);
            }
            libraryClass.interfaceClasses = interfaceClasses;
        }
        libraryClass.superClassName = null;
        libraryClass.interfaceNames = null;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClass =
            findClass(clazz.getName(), classConstant.getName(clazz));
    }
    private Clazz findClass(String referencingClassName, String name)
    {
        Clazz clazz = programClassPool.getClass(name);
        if (clazz == null)
        {
            clazz = libraryClassPool.getClass(name);
            if (clazz == null &&
                missingWarningPrinter != null)
            {
                missingWarningPrinter.print(referencingClassName,
                                            name,
                                            "Warning: " +
                                            ClassUtil.externalClassName(referencingClassName) +
                                            ": can't find superclass or interface " +
                                            ClassUtil.externalClassName(name));
            }
        }
        else if (dependencyWarningPrinter != null)
        {
            dependencyWarningPrinter.print(referencingClassName,
                                           name,
                                           "Warning: library class " +
                                           ClassUtil.externalClassName(referencingClassName) +
                                           " extends or implements program class " +
                                           ClassUtil.externalClassName(name));
        }
        return clazz;
    }
}
