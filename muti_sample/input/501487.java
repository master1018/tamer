package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.constant.*;
public class ConstantPoolEditor
{
    private static final boolean DEBUG = false;
    private ProgramClass targetClass;
    public ConstantPoolEditor(ProgramClass targetClass)
    {
        this.targetClass = targetClass;
    }
    public int addIntegerConstant(int value)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Integer)
            {
                IntegerConstant integerConstant = (IntegerConstant)constant;
                if (integerConstant.getValue() == value)
                {
                    return index;
                }
            }
        }
        return addConstant(new IntegerConstant(value));
    }
    public int addLongConstant(long value)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Long)
            {
                LongConstant longConstant = (LongConstant)constant;
                if (longConstant.getValue() == value)
                {
                    return index;
                }
            }
        }
        return addConstant(new LongConstant(value));
    }
    public int addFloatConstant(float value)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Float)
            {
                FloatConstant floatConstant = (FloatConstant)constant;
                if (floatConstant.getValue() == value)
                {
                    return index;
                }
            }
        }
        return addConstant(new FloatConstant(value));
    }
    public int addDoubleConstant(double value)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Double)
            {
                DoubleConstant doubleConstant = (DoubleConstant)constant;
                if (doubleConstant.getValue() == value)
                {
                    return index;
                }
            }
        }
        return addConstant(new DoubleConstant(value));
    }
    public int addStringConstant(String string,
                                 Clazz  referencedClass,
                                 Member referencedMember)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_String)
            {
                StringConstant stringConstant = (StringConstant)constant;
                if (stringConstant.getString(targetClass).equals(string))
                {
                    return index;
                }
            }
        }
        return addConstant(new StringConstant(addUtf8Constant(string),
                                              referencedClass,
                                              referencedMember));
    }
    public int addFieldrefConstant(Clazz  referencedClass,
                                   Member referencedMember)
    {
        return addFieldrefConstant(referencedClass.getName(),
                                   referencedMember.getName(referencedClass),
                                   referencedMember.getDescriptor(referencedClass),
                                   referencedClass,
                                   referencedMember);
    }
    public int addFieldrefConstant(String className,
                                   String name,
                                   String descriptor,
                                   Clazz  referencedClass,
                                   Member referencedMember)
    {
        return addFieldrefConstant(className,
                                   addNameAndTypeConstant(name, descriptor),
                                   referencedClass,
                                   referencedMember);
    }
    public int addFieldrefConstant(String className,
                                   int    nameAndTypeIndex,
                                   Clazz  referencedClass,
                                   Member referencedMember)
    {
        return addFieldrefConstant(addClassConstant(className, referencedClass),
                                   nameAndTypeIndex,
                                   referencedClass,
                                   referencedMember);
    }
    public int addFieldrefConstant(int    classIndex,
                                   String name,
                                   String descriptor,
                                   Clazz  referencedClass,
                                   Member referencedMember)
    {
        return addFieldrefConstant(classIndex,
                                   addNameAndTypeConstant(name, descriptor),
                                   referencedClass,
                                   referencedMember);
    }
    public int addFieldrefConstant(int    classIndex,
                                   int    nameAndTypeIndex,
                                   Clazz  referencedClass,
                                   Member referencedMember)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Fieldref)
            {
                FieldrefConstant fieldrefConstant = (FieldrefConstant)constant;
                if (fieldrefConstant.u2classIndex         == classIndex &&
                    fieldrefConstant.u2nameAndTypeIndex   == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }
        return addConstant(new FieldrefConstant(classIndex,
                                                nameAndTypeIndex,
                                                referencedClass,
                                                referencedMember));
    }
    public int addInterfaceMethodrefConstant(String className,
                                             String name,
                                             String descriptor,
                                             Clazz  referencedClass,
                                             Member referencedMember)
    {
        return addInterfaceMethodrefConstant(className,
                                             addNameAndTypeConstant(name, descriptor),
                                             referencedClass,
                                             referencedMember);
    }
    public int addInterfaceMethodrefConstant(String className,
                                             int    nameAndTypeIndex,
                                             Clazz  referencedClass,
                                             Member referencedMember)
    {
        return addInterfaceMethodrefConstant(addClassConstant(className, referencedClass),
                                             nameAndTypeIndex,
                                             referencedClass,
                                             referencedMember);
    }
    public int addInterfaceMethodrefConstant(Clazz  referencedClass,
                                             Member referencedMember)
    {
        return addInterfaceMethodrefConstant(referencedClass.getName(),
                                             referencedMember.getName(referencedClass),
                                             referencedMember.getDescriptor(referencedClass),
                                             referencedClass,
                                             referencedMember);
    }
    public int addInterfaceMethodrefConstant(int    classIndex,
                                             String name,
                                             String descriptor,
                                             Clazz  referencedClass,
                                             Member referencedMember)
    {
        return addInterfaceMethodrefConstant(classIndex,
                                             addNameAndTypeConstant(name, descriptor),
                                             referencedClass,
                                             referencedMember);
    }
    public int addInterfaceMethodrefConstant(int    classIndex,
                                             int    nameAndTypeIndex,
                                             Clazz  referencedClass,
                                             Member referencedMember)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                            constant.getTag() == ClassConstants.CONSTANT_InterfaceMethodref)
            {
                InterfaceMethodrefConstant methodrefConstant = (InterfaceMethodrefConstant)constant;
                if (methodrefConstant.u2classIndex       == classIndex &&
                    methodrefConstant.u2nameAndTypeIndex == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }
        return addConstant(new InterfaceMethodrefConstant(classIndex,
                                                          nameAndTypeIndex,
                                                          referencedClass,
                                                          referencedMember));
    }
    public int addMethodrefConstant(Clazz  referencedClass,
                                    Member referencedMember)
    {
        return addMethodrefConstant(referencedClass.getName(),
                                    referencedMember.getName(referencedClass),
                                    referencedMember.getDescriptor(referencedClass),
                                    referencedClass,
                                    referencedMember);
    }
    public int addMethodrefConstant(String className,
                                    String name,
                                    String descriptor,
                                    Clazz  referencedClass,
                                    Member referencedMember)
    {
        return addMethodrefConstant(className,
                                    addNameAndTypeConstant(name, descriptor),
                                    referencedClass,
                                    referencedMember);
    }
    public int addMethodrefConstant(String className,
                                    int    nameAndTypeIndex,
                                    Clazz  referencedClass,
                                    Member referencedMember)
    {
        return addMethodrefConstant(addClassConstant(className, referencedClass),
                                    nameAndTypeIndex,
                                    referencedClass,
                                    referencedMember);
    }
    public int addMethodrefConstant(int    classIndex,
                                    String name,
                                    String descriptor,
                                    Clazz  referencedClass,
                                    Member referencedMember)
    {
        return addMethodrefConstant(classIndex,
                                    addNameAndTypeConstant(name, descriptor),
                                    referencedClass,
                                    referencedMember);
    }
    public int addMethodrefConstant(int    classIndex,
                                    int    nameAndTypeIndex,
                                    Clazz  referencedClass,
                                    Member referencedMember)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Methodref)
            {
                MethodrefConstant methodrefConstant = (MethodrefConstant)constant;
                if (methodrefConstant.u2classIndex       == classIndex &&
                    methodrefConstant.u2nameAndTypeIndex == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }
        return addConstant(new MethodrefConstant(classIndex,
                                                 nameAndTypeIndex,
                                                 referencedClass,
                                                 referencedMember));
    }
    public int addClassConstant(Clazz referencedClass)
    {
        return addClassConstant(referencedClass.getName(),
                                referencedClass);
    }
    public int addClassConstant(String name,
                                Clazz  referencedClass)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Class)
            {
                ClassConstant classConstant = (ClassConstant)constant;
                if (classConstant.getName(targetClass).equals(name))
                {
                    return index;
                }
            }
        }
        int nameIndex = addUtf8Constant(name);
        return addConstant(new ClassConstant(nameIndex, referencedClass));
    }
    public int addNameAndTypeConstant(String name,
                                      String type)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_NameAndType)
            {
                NameAndTypeConstant nameAndTypeConstant = (NameAndTypeConstant)constant;
                if (nameAndTypeConstant.getName(targetClass).equals(name) &&
                    nameAndTypeConstant.getType(targetClass).equals(type))
                {
                    return index;
                }
            }
        }
        return addConstant(new NameAndTypeConstant(addUtf8Constant(name),
                                                   addUtf8Constant(type)));
    }
    public int addUtf8Constant(String string)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        for (int index = 1; index < constantPoolCount; index++)
        {
            Constant constant = constantPool[index];
            if (constant != null &&
                constant.getTag() == ClassConstants.CONSTANT_Utf8)
            {
                Utf8Constant utf8Constant = (Utf8Constant)constant;
                if (utf8Constant.getString().equals(string))
                {
                    return index;
                }
            }
        }
        return addConstant(new Utf8Constant(string));
    }
    public int addConstant(Constant constant)
    {
        int        constantPoolCount = targetClass.u2constantPoolCount;
        Constant[] constantPool      = targetClass.constantPool;
        if (constantPool.length < constantPoolCount+2)
        {
            targetClass.constantPool = new Constant[constantPoolCount+2];
            System.arraycopy(constantPool, 0,
                             targetClass.constantPool, 0,
                             constantPoolCount);
            constantPool = targetClass.constantPool;
        }
        if (DEBUG)
        {
            System.out.println(targetClass.getName()+": adding ["+constant.getClass().getName()+"] at index "+targetClass.u2constantPoolCount);
        }
        constantPool[targetClass.u2constantPoolCount++] = constant;
        int tag = constant.getTag();
        if (tag == ClassConstants.CONSTANT_Long ||
            tag == ClassConstants.CONSTANT_Double)
        {
            constantPool[targetClass.u2constantPoolCount++] = null;
        }
        return constantPoolCount;
    }
}
