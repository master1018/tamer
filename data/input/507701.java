import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.constant.ClassConstant;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.ClassVisitor;
import proguard.util.*;
import java.util.*;
public class ClassObfuscator
extends      SimplifiedVisitor
implements   ClassVisitor,
             AttributeVisitor,
             InnerClassesInfoVisitor,
             ConstantVisitor
{
    private final DictionaryNameFactory classNameFactory;
    private final DictionaryNameFactory packageNameFactory;
    private final boolean               useMixedCaseClassNames;
    private final StringMatcher         keepPackageNamesMatcher;
    private final String                flattenPackageHierarchy;
    private final String                repackageClasses;
    private final boolean               allowAccessModification;
    private final Set classNamesToAvoid                       = new HashSet();
    private final Map packagePrefixMap                        = new HashMap();
    private final Map packagePrefixPackageNameFactoryMap      = new HashMap();
    private final Map packagePrefixClassNameFactoryMap        = new HashMap();
    private final Map packagePrefixNumericClassNameFactoryMap = new HashMap();
    private String  newClassName;
    private boolean numericClassName;
    public ClassObfuscator(ClassPool             programClassPool,
                           DictionaryNameFactory classNameFactory,
                           DictionaryNameFactory packageNameFactory,
                           boolean               useMixedCaseClassNames,
                           List                  keepPackageNames,
                           String                flattenPackageHierarchy,
                           String                repackageClasses,
                           boolean               allowAccessModification)
    {
        this.classNameFactory   = classNameFactory;
        this.packageNameFactory = packageNameFactory;
        if (flattenPackageHierarchy != null &&
            flattenPackageHierarchy.length() > 0)
        {
            flattenPackageHierarchy += ClassConstants.INTERNAL_PACKAGE_SEPARATOR;
        }
        if (repackageClasses != null &&
            repackageClasses.length() > 0)
        {
            repackageClasses += ClassConstants.INTERNAL_PACKAGE_SEPARATOR;
        }
        this.useMixedCaseClassNames  = useMixedCaseClassNames;
        this.keepPackageNamesMatcher = keepPackageNames == null ? null :
            new ListParser(new FileNameParser()).parse(keepPackageNames);
        this.flattenPackageHierarchy = flattenPackageHierarchy;
        this.repackageClasses        = repackageClasses;
        this.allowAccessModification = allowAccessModification;
        packagePrefixMap.put("", "");
        programClassPool.classesAccept(new MyKeepCollector());
    }
    public void visitProgramClass(ProgramClass programClass)
    {
        newClassName = newClassName(programClass);
        if (newClassName == null)
        {
            programClass.attributesAccept(this);
            String newPackagePrefix = newClassName != null ?
                newClassName + ClassConstants.INTERNAL_INNER_CLASS_SEPARATOR :
                newPackagePrefix(ClassUtil.internalPackagePrefix(programClass.getName()));
            newClassName = newClassName != null && numericClassName ?
                generateUniqueNumericClassName(newPackagePrefix) :
                generateUniqueClassName(newPackagePrefix);
            setNewClassName(programClass, newClassName);
        }
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitInnerClassesAttribute(Clazz clazz, InnerClassesAttribute innerClassesAttribute)
    {
        innerClassesAttribute.innerClassEntriesAccept(clazz, this);
    }
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        enclosingMethodAttribute.referencedClassAccept(this);
        String innerClassName = clazz.getName();
        String outerClassName = clazz.getClassName(enclosingMethodAttribute.u2classIndex);
        numericClassName = isNumericClassName(innerClassName,
                                              outerClassName);
    }
    public void visitInnerClassesInfo(Clazz clazz, InnerClassesInfo innerClassesInfo)
    {
        int innerClassIndex = innerClassesInfo.u2innerClassIndex;
        int outerClassIndex = innerClassesInfo.u2outerClassIndex;
        if (innerClassIndex != 0 &&
            outerClassIndex != 0)
        {
            String innerClassName = clazz.getClassName(innerClassIndex);
            if (innerClassName.equals(clazz.getName()))
            {
                clazz.constantPoolEntryAccept(outerClassIndex, this);
                String outerClassName = clazz.getClassName(outerClassIndex);
                numericClassName = isNumericClassName(innerClassName,
                                                      outerClassName);
            }
        }
    }
    private boolean isNumericClassName(String innerClassName,
                                       String outerClassName)
    {
        int innerClassNameStart  = outerClassName.length() + 1;
        int innerClassNameLength = innerClassName.length();
        if (innerClassNameStart >= innerClassNameLength)
        {
            return false;
        }
        for (int index = innerClassNameStart; index < innerClassNameLength; index++)
        {
            if (!Character.isDigit(innerClassName.charAt(index)))
            {
                return false;
            }
        }
        return true;
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.referencedClassAccept(this);
    }
    private class MyKeepCollector implements ClassVisitor
    {
        public void visitProgramClass(ProgramClass programClass)
        {
            String newClassName = newClassName(programClass);
            if (newClassName != null)
            {
                classNamesToAvoid.add(mixedCaseClassName(newClassName));
                if (repackageClasses == null ||
                    !allowAccessModification)
                {
                    String className = programClass.getName();
                    mapPackageName(className,
                                   newClassName,
                                   repackageClasses        == null &&
                                   flattenPackageHierarchy == null);
                }
            }
        }
        public void visitLibraryClass(LibraryClass libraryClass)
        {
        }
        private void mapPackageName(String  className,
                                    String  newClassName,
                                    boolean recursively)
        {
            String packagePrefix    = ClassUtil.internalPackagePrefix(className);
            String newPackagePrefix = ClassUtil.internalPackagePrefix(newClassName);
            do
            {
                packagePrefixMap.put(packagePrefix, newPackagePrefix);
                if (!recursively)
                {
                    break;
                }
                packagePrefix    = ClassUtil.internalPackagePrefix(packagePrefix);
                newPackagePrefix = ClassUtil.internalPackagePrefix(newPackagePrefix);
            }
            while (packagePrefix.length()    > 0 &&
                   newPackagePrefix.length() > 0);
        }
    }
    private String newPackagePrefix(String packagePrefix)
    {
        String newPackagePrefix = (String)packagePrefixMap.get(packagePrefix);
        if (newPackagePrefix == null)
        {
            if (keepPackageNamesMatcher != null &&
                keepPackageNamesMatcher.matches(packagePrefix.length() > 0 ?
                    packagePrefix.substring(0, packagePrefix.length()-1) :
                    packagePrefix))
            {
                return packagePrefix;
            }
            if (repackageClasses != null)
            {
                return repackageClasses;
            }
            String newSuperPackagePrefix = flattenPackageHierarchy != null ?
                flattenPackageHierarchy :
                newPackagePrefix(ClassUtil.internalPackagePrefix(packagePrefix));
            newPackagePrefix = generateUniquePackagePrefix(newSuperPackagePrefix);
            packagePrefixMap.put(packagePrefix, newPackagePrefix);
        }
        return newPackagePrefix;
    }
    private String generateUniquePackagePrefix(String newSuperPackagePrefix)
    {
        NameFactory packageNameFactory =
            (NameFactory)packagePrefixPackageNameFactoryMap.get(newSuperPackagePrefix);
        if (packageNameFactory == null)
        {
            packageNameFactory = new SimpleNameFactory(useMixedCaseClassNames);
            if (this.packageNameFactory != null)
            {
                packageNameFactory =
                    new DictionaryNameFactory(this.packageNameFactory,
                                              packageNameFactory);
            }
            packagePrefixPackageNameFactoryMap.put(newSuperPackagePrefix,
                                                   packageNameFactory);
        }
        return generateUniquePackagePrefix(newSuperPackagePrefix, packageNameFactory);
    }
    private String generateUniquePackagePrefix(String      newSuperPackagePrefix,
                                               NameFactory packageNameFactory)
    {
        String newPackagePrefix;
        do
        {
            newPackagePrefix = newSuperPackagePrefix +
                               packageNameFactory.nextName() +
                               ClassConstants.INTERNAL_PACKAGE_SEPARATOR;
        }
        while (packagePrefixMap.containsValue(newPackagePrefix));
        return newPackagePrefix;
    }
    private String generateUniqueClassName(String newPackagePrefix)
    {
        NameFactory classNameFactory =
            (NameFactory)packagePrefixClassNameFactoryMap.get(newPackagePrefix);
        if (classNameFactory == null)
        {
            classNameFactory = new SimpleNameFactory(useMixedCaseClassNames);
            if (this.classNameFactory != null)
            {
                classNameFactory =
                    new DictionaryNameFactory(this.classNameFactory,
                                              classNameFactory);
            }
            packagePrefixClassNameFactoryMap.put(newPackagePrefix,
                                                 classNameFactory);
        }
        return generateUniqueClassName(newPackagePrefix, classNameFactory);
    }
    private String generateUniqueNumericClassName(String newPackagePrefix)
    {
        NameFactory classNameFactory =
            (NameFactory)packagePrefixNumericClassNameFactoryMap.get(newPackagePrefix);
        if (classNameFactory == null)
        {
            classNameFactory = new NumericNameFactory();
            packagePrefixNumericClassNameFactoryMap.put(newPackagePrefix,
                                                        classNameFactory);
        }
        return generateUniqueClassName(newPackagePrefix, classNameFactory);
    }
    private String generateUniqueClassName(String      newPackagePrefix,
                                           NameFactory classNameFactory)
    {
        String newClassName;
        do
        {
            newClassName = newPackagePrefix +
                           classNameFactory.nextName();
        }
        while (classNamesToAvoid.contains(mixedCaseClassName(newClassName)));
        return newClassName;
    }
    private String mixedCaseClassName(String className)
    {
        return useMixedCaseClassNames ?
            className :
            className.toLowerCase();
    }
    static void setNewClassName(Clazz clazz, String name)
    {
        clazz.setVisitorInfo(name);
    }
    static String newClassName(Clazz clazz)
    {
        Object visitorInfo = clazz.getVisitorInfo();
        return visitorInfo instanceof String ?
            (String)visitorInfo :
            null;
    }
}
