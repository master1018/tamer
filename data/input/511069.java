package proguard.classfile.util;
import proguard.classfile.ClassConstants;
import java.util.List;
public class ClassUtil
{
    private static final String EMPTY_STRING = "";
    public static void checkMagicNumber(int magicNumber) throws UnsupportedOperationException
    {
        if (magicNumber != ClassConstants.MAGIC)
        {
            throw new UnsupportedOperationException("Invalid magic number ["+Integer.toHexString(magicNumber)+"] in class");
        }
    }
    public static int internalClassVersion(int majorVersion, int minorVersion)
    {
        return (majorVersion << 16) | minorVersion;
    }
    public static int internalMajorClassVersion(int classVersion)
    {
        return classVersion >>> 16;
    }
    public static int internalMinorClassVersion(int classVersion)
    {
        return classVersion & 0xffff;
    }
    public static int internalClassVersion(String classVersion)
    {
        return
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_0) ||
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_1) ? ClassConstants.INTERNAL_CLASS_VERSION_1_0 :
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_2) ? ClassConstants.INTERNAL_CLASS_VERSION_1_2 :
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_3) ? ClassConstants.INTERNAL_CLASS_VERSION_1_3 :
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_4) ? ClassConstants.INTERNAL_CLASS_VERSION_1_4 :
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_5_ALIAS) ||
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_5) ? ClassConstants.INTERNAL_CLASS_VERSION_1_5 :
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_6_ALIAS) ||
            classVersion.equals(ClassConstants.EXTERNAL_CLASS_VERSION_1_6) ? ClassConstants.INTERNAL_CLASS_VERSION_1_6 :
                                                                             0;
    }
    public static String externalClassVersion(int classVersion)
    {
        switch (classVersion)
        {
            case ClassConstants.INTERNAL_CLASS_VERSION_1_0: return ClassConstants.EXTERNAL_CLASS_VERSION_1_0;
            case ClassConstants.INTERNAL_CLASS_VERSION_1_2: return ClassConstants.EXTERNAL_CLASS_VERSION_1_2;
            case ClassConstants.INTERNAL_CLASS_VERSION_1_3: return ClassConstants.EXTERNAL_CLASS_VERSION_1_3;
            case ClassConstants.INTERNAL_CLASS_VERSION_1_4: return ClassConstants.EXTERNAL_CLASS_VERSION_1_4;
            case ClassConstants.INTERNAL_CLASS_VERSION_1_5: return ClassConstants.EXTERNAL_CLASS_VERSION_1_5;
            case ClassConstants.INTERNAL_CLASS_VERSION_1_6: return ClassConstants.EXTERNAL_CLASS_VERSION_1_6;
            default:                                        return null;
        }
    }
    public static void checkVersionNumbers(int classVersion) throws UnsupportedOperationException
    {
        if (classVersion < ClassConstants.INTERNAL_CLASS_VERSION_1_0 ||
            classVersion > ClassConstants.INTERNAL_CLASS_VERSION_1_6)
        {
            throw new UnsupportedOperationException("Unsupported version number ["+
                                                    internalMajorClassVersion(classVersion)+"."+
                                                    internalMinorClassVersion(classVersion)+"] for class format");
        }
    }
    public static String internalClassName(String externalClassName)
    {
        return externalClassName.replace(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR,
                                         ClassConstants.INTERNAL_PACKAGE_SEPARATOR);
    }
    public static String externalFullClassDescription(int    accessFlags,
                                                      String internalClassName)
    {
        return externalClassAccessFlags(accessFlags) +
               externalClassName(internalClassName);
    }
    public static String externalClassName(String internalClassName)
    {
        return 
               internalClassName.replace(ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
                                         ClassConstants.EXTERNAL_PACKAGE_SEPARATOR);
    }
    public static String externalShortClassName(String externalClassName)
    {
        int index = externalClassName.lastIndexOf(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR);
        return externalClassName.substring(index+1);
    }
    public static boolean isInternalArrayType(String internalType)
    {
        return internalType.length() > 1 &&
               internalType.charAt(0) == ClassConstants.INTERNAL_TYPE_ARRAY;
    }
    public static int internalArrayTypeDimensionCount(String internalType)
    {
        int dimensions = 0;
        while (internalType.charAt(dimensions) == ClassConstants.INTERNAL_TYPE_ARRAY)
        {
            dimensions++;
        }
        return dimensions;
    }
    public static boolean isInternalArrayInterfaceName(String internalClassName)
    {
        return ClassConstants.INTERNAL_NAME_JAVA_LANG_OBJECT.equals(internalClassName)    ||
               ClassConstants.INTERNAL_NAME_JAVA_LANG_CLONEABLE.equals(internalClassName) ||
               ClassConstants.INTERNAL_NAME_JAVA_IO_SERIALIZABLE.equals(internalClassName);
    }
    public static boolean isInternalPrimitiveType(char internalType)
    {
        return internalType == ClassConstants.INTERNAL_TYPE_BOOLEAN ||
               internalType == ClassConstants.INTERNAL_TYPE_BYTE    ||
               internalType == ClassConstants.INTERNAL_TYPE_CHAR    ||
               internalType == ClassConstants.INTERNAL_TYPE_SHORT   ||
               internalType == ClassConstants.INTERNAL_TYPE_INT     ||
               internalType == ClassConstants.INTERNAL_TYPE_FLOAT   ||
               internalType == ClassConstants.INTERNAL_TYPE_LONG    ||
               internalType == ClassConstants.INTERNAL_TYPE_DOUBLE;
    }
    public static boolean isInternalCategory2Type(String internalType)
    {
        return internalType.length() == 1 &&
               (internalType.charAt(0) == ClassConstants.INTERNAL_TYPE_LONG ||
                internalType.charAt(0) == ClassConstants.INTERNAL_TYPE_DOUBLE);
    }
    public static boolean isInternalClassType(String internalType)
    {
        int length = internalType.length();
        return length > 1 &&
               internalType.charAt(length-1) == ClassConstants.INTERNAL_TYPE_CLASS_END;
    }
    public static String internalTypeFromClassName(String internalClassName)
    {
        return internalArrayTypeFromClassName(internalClassName, 0);
    }
    public static String internalArrayTypeFromClassName(String internalClassName,
                                                        int    dimensionCount)
    {
        StringBuffer buffer = new StringBuffer(internalClassName.length() + dimensionCount + 2);
        for (int dimension = 0; dimension < dimensionCount; dimension++)
        {
            buffer.append(ClassConstants.INTERNAL_TYPE_ARRAY);
        }
        return buffer.append(ClassConstants.INTERNAL_TYPE_CLASS_START)
                     .append(internalClassName)
                     .append(ClassConstants.INTERNAL_TYPE_CLASS_END)
                     .toString();
    }
    public static String internalTypeFromArrayType(String internalArrayType)
    {
        int index = internalArrayType.lastIndexOf(ClassConstants.INTERNAL_TYPE_ARRAY);
        return internalArrayType.substring(index+1);
    }
    public static String internalClassNameFromClassType(String internalClassType)
    {
        return isInternalClassType(internalClassType) ?
            internalClassType.substring(internalClassType.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_START)+1,
                                        internalClassType.length()-1) :
            internalClassType;
    }
    public static String internalClassNameFromType(String internalClassType)
    {
        if (!isInternalClassType(internalClassType))
        {
            return null;
        }
        if (isInternalArrayType(internalClassType))
        {
            internalClassType = internalTypeFromArrayType(internalClassType);
        }
        return internalClassNameFromClassType(internalClassType);
    }
    public static String internalMethodReturnType(String internalMethodDescriptor)
    {
        int index = internalMethodDescriptor.indexOf(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        return internalMethodDescriptor.substring(index + 1);
    }
    public static int internalMethodParameterCount(String internalMethodDescriptor)
    {
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(internalMethodDescriptor);
        int counter = 0;
        while (internalTypeEnumeration.hasMoreTypes())
        {
            internalTypeEnumeration.nextType();
            counter++;
        }
        return counter;
    }
    public static int internalMethodParameterSize(String internalMethodDescriptor)
    {
        return internalMethodParameterSize(internalMethodDescriptor, true);
    }
    public static int internalMethodParameterSize(String internalMethodDescriptor,
                                                  int    accessFlags)
    {
        return internalMethodParameterSize(internalMethodDescriptor,
                                           (accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0);
    }
    public static int internalMethodParameterSize(String  internalMethodDescriptor,
                                                  boolean isStatic)
    {
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(internalMethodDescriptor);
        int size = isStatic ? 0 : 1;
        while (internalTypeEnumeration.hasMoreTypes())
        {
            String internalType = internalTypeEnumeration.nextType();
            size += internalTypeSize(internalType);
        }
        return size;
    }
    public static int internalTypeSize(String internalType)
    {
        if (internalType.length() == 1)
        {
            char internalPrimitiveType = internalType.charAt(0);
            if      (internalPrimitiveType == ClassConstants.INTERNAL_TYPE_LONG ||
                     internalPrimitiveType == ClassConstants.INTERNAL_TYPE_DOUBLE)
            {
                return 2;
            }
            else if (internalPrimitiveType == ClassConstants.INTERNAL_TYPE_VOID)
            {
                return 0;
            }
        }
        return 1;
    }
    public static String internalType(String externalType)
    {
        int dimensionCount = externalArrayTypeDimensionCount(externalType);
        if (dimensionCount > 0)
        {
            externalType = externalType.substring(0, externalType.length() - dimensionCount * ClassConstants.EXTERNAL_TYPE_ARRAY.length());
        }
        char internalTypeChar =
            externalType.equals(ClassConstants.EXTERNAL_TYPE_VOID   ) ?
                                ClassConstants.INTERNAL_TYPE_VOID     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_BOOLEAN) ?
                                ClassConstants.INTERNAL_TYPE_BOOLEAN  :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_BYTE   ) ?
                                ClassConstants.INTERNAL_TYPE_BYTE     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_CHAR   ) ?
                                ClassConstants.INTERNAL_TYPE_CHAR     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_SHORT  ) ?
                                ClassConstants.INTERNAL_TYPE_SHORT    :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_INT    ) ?
                                ClassConstants.INTERNAL_TYPE_INT      :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_FLOAT  ) ?
                                ClassConstants.INTERNAL_TYPE_FLOAT    :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_LONG   ) ?
                                ClassConstants.INTERNAL_TYPE_LONG     :
            externalType.equals(ClassConstants.EXTERNAL_TYPE_DOUBLE ) ?
                                ClassConstants.INTERNAL_TYPE_DOUBLE   :
            externalType.equals("%"                                 ) ?
                                '%'                                   :
                                (char)0;
        String internalType =
            internalTypeChar != 0 ? String.valueOf(internalTypeChar) :
                                    ClassConstants.INTERNAL_TYPE_CLASS_START +
                                    internalClassName(externalType) +
                                    ClassConstants.INTERNAL_TYPE_CLASS_END;
        for (int count = 0; count < dimensionCount; count++)
        {
            internalType = ClassConstants.INTERNAL_TYPE_ARRAY + internalType;
        }
        return internalType;
    }
    public static int externalArrayTypeDimensionCount(String externalType)
    {
        int dimensions = 0;
        int length = ClassConstants.EXTERNAL_TYPE_ARRAY.length();
        int offset = externalType.length() - length;
        while (externalType.regionMatches(offset,
                                          ClassConstants.EXTERNAL_TYPE_ARRAY,
                                          0,
                                          length))
        {
            dimensions++;
            offset -= length;
        }
        return dimensions;
    }
    public static String externalType(String internalType)
    {
        int dimensionCount = internalArrayTypeDimensionCount(internalType);
        if (dimensionCount > 0)
        {
            internalType = internalType.substring(dimensionCount);
        }
        char internalTypeChar = internalType.charAt(0);
        String externalType =
            internalTypeChar == ClassConstants.INTERNAL_TYPE_VOID        ?
                                ClassConstants.EXTERNAL_TYPE_VOID        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_BOOLEAN     ?
                                ClassConstants.EXTERNAL_TYPE_BOOLEAN     :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_BYTE        ?
                                ClassConstants.EXTERNAL_TYPE_BYTE        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_CHAR        ?
                                ClassConstants.EXTERNAL_TYPE_CHAR        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_SHORT       ?
                                ClassConstants.EXTERNAL_TYPE_SHORT       :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_INT         ?
                                ClassConstants.EXTERNAL_TYPE_INT         :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_FLOAT       ?
                                ClassConstants.EXTERNAL_TYPE_FLOAT       :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_LONG        ?
                                ClassConstants.EXTERNAL_TYPE_LONG        :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_DOUBLE      ?
                                ClassConstants.EXTERNAL_TYPE_DOUBLE      :
            internalTypeChar == '%'                                      ?
                                "%"                                      :
            internalTypeChar == ClassConstants.INTERNAL_TYPE_CLASS_START ?
                                externalClassName(internalType.substring(1, internalType.indexOf(ClassConstants.INTERNAL_TYPE_CLASS_END))) :
                                null;
        if (externalType == null)
        {
            throw new IllegalArgumentException("Unknown type ["+internalType+"]");
        }
        for (int count = 0; count < dimensionCount; count++)
        {
            externalType += ClassConstants.EXTERNAL_TYPE_ARRAY;
        }
        return externalType;
    }
    public static boolean isInternalMethodDescriptor(String internalDescriptor)
    {
        return internalDescriptor.charAt(0) == ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN;
    }
    public static boolean isExternalMethodNameAndArguments(String externalMemberNameAndArguments)
    {
        return externalMemberNameAndArguments.indexOf(ClassConstants.EXTERNAL_METHOD_ARGUMENTS_OPEN) > 0;
    }
    public static String externalMethodName(String externalMethodNameAndArguments)
    {
        ExternalTypeEnumeration externalTypeEnumeration =
            new ExternalTypeEnumeration(externalMethodNameAndArguments);
        return externalTypeEnumeration.methodName();
    }
    public static String internalMethodDescriptor(String externalReturnType,
                                                  String externalMethodNameAndArguments)
    {
        StringBuffer internalMethodDescriptor = new StringBuffer();
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);
        ExternalTypeEnumeration externalTypeEnumeration =
            new ExternalTypeEnumeration(externalMethodNameAndArguments);
        while (externalTypeEnumeration.hasMoreTypes())
        {
            internalMethodDescriptor.append(internalType(externalTypeEnumeration.nextType()));
        }
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        internalMethodDescriptor.append(internalType(externalReturnType));
        return internalMethodDescriptor.toString();
    }
    public static String internalMethodDescriptor(String externalReturnType,
                                                  List   externalArguments)
    {
        StringBuffer internalMethodDescriptor = new StringBuffer();
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_OPEN);
        for (int index = 0; index < externalArguments.size(); index++)
        {
            internalMethodDescriptor.append(internalType((String)externalArguments.get(index)));
        }
        internalMethodDescriptor.append(ClassConstants.INTERNAL_METHOD_ARGUMENTS_CLOSE);
        internalMethodDescriptor.append(internalType(externalReturnType));
        return internalMethodDescriptor.toString();
    }
    public static String externalFullFieldDescription(int    accessFlags,
                                                      String fieldName,
                                                      String internalFieldDescriptor)
    {
        return externalFieldAccessFlags(accessFlags) +
               externalType(internalFieldDescriptor) +
               ' ' +
               fieldName;
    }
    public static String externalFullMethodDescription(String internalClassName,
                                                       int    accessFlags,
                                                       String internalMethodName,
                                                       String internalMethodDescriptor)
    {
        return externalMethodAccessFlags(accessFlags) +
               externalMethodReturnTypeAndName(internalClassName,
                                               internalMethodName,
                                               internalMethodDescriptor) +
               ClassConstants.EXTERNAL_METHOD_ARGUMENTS_OPEN +
               externalMethodArguments(internalMethodDescriptor) +
               ClassConstants.EXTERNAL_METHOD_ARGUMENTS_CLOSE;
    }
    public static String externalClassAccessFlags(int accessFlags)
    {
        return externalClassAccessFlags(accessFlags, "");
    }
    public static String externalClassAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }
        StringBuffer string = new StringBuffer(50);
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_ANNOTATTION) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ANNOTATION);
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_INTERFACE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_INTERFACE).append(' ');
        }
        else if ((accessFlags & ClassConstants.INTERNAL_ACC_ENUM) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ENUM).append(' ');
        }
        else if ((accessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ABSTRACT).append(' ');
        }
        return string.toString();
    }
    public static String externalFieldAccessFlags(int accessFlags)
    {
        return externalFieldAccessFlags(accessFlags, "");
    }
    public static String externalFieldAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }
        StringBuffer string = new StringBuffer(50);
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PRIVATE).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PROTECTED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PROTECTED).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STATIC).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_VOLATILE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_VOLATILE).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_TRANSIENT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_TRANSIENT).append(' ');
        }
        return string.toString();
    }
    public static String externalMethodAccessFlags(int accessFlags)
    {
        return externalMethodAccessFlags(accessFlags, "");
    }
    public static String externalMethodAccessFlags(int accessFlags, String prefix)
    {
        if (accessFlags == 0)
        {
            return EMPTY_STRING;
        }
        StringBuffer string = new StringBuffer(50);
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PUBLIC).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PRIVATE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PRIVATE).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_PROTECTED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_PROTECTED).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STATIC) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STATIC).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_FINAL) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_FINAL).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_SYNCHRONIZED) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_SYNCHRONIZED).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_NATIVE) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_NATIVE).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_ABSTRACT).append(' ');
        }
        if ((accessFlags & ClassConstants.INTERNAL_ACC_STRICT) != 0)
        {
            string.append(prefix).append(ClassConstants.EXTERNAL_ACC_STRICT).append(' ');
        }
        return string.toString();
    }
    public static String externalMethodReturnType(String internalMethodDescriptor)
    {
        return externalType(internalMethodReturnType(internalMethodDescriptor));
    }
    private static String externalMethodReturnTypeAndName(String internalClassName,
                                                          String internalMethodName,
                                                          String internalMethodDescriptor)
    {
        return internalMethodName.equals(ClassConstants.INTERNAL_METHOD_NAME_INIT) ?
            externalShortClassName(externalClassName(internalClassName)) :
            (externalMethodReturnType(internalMethodDescriptor) +
             ' ' +
             internalMethodName);
    }
    public static String externalMethodArguments(String internalMethodDescriptor)
    {
        StringBuffer externalMethodNameAndArguments = new StringBuffer();
        InternalTypeEnumeration internalTypeEnumeration =
            new InternalTypeEnumeration(internalMethodDescriptor);
        while (internalTypeEnumeration.hasMoreTypes())
        {
            externalMethodNameAndArguments.append(externalType(internalTypeEnumeration.nextType()));
            if (internalTypeEnumeration.hasMoreTypes())
            {
                externalMethodNameAndArguments.append(ClassConstants.EXTERNAL_METHOD_ARGUMENTS_SEPARATOR);
            }
        }
        return externalMethodNameAndArguments.toString();
    }
    public static String internalPackageName(String internalClassName)
    {
        String internalPackagePrefix = internalPackagePrefix(internalClassName);
        int length = internalPackagePrefix.length();
        return length > 0 ?
            internalPackagePrefix.substring(0, length - 1) :
            "";
    }
    public static String internalPackagePrefix(String internalClassName)
    {
        return internalClassName.substring(0, internalClassName.lastIndexOf(ClassConstants.INTERNAL_PACKAGE_SEPARATOR,
                                                                            internalClassName.length() - 2) + 1);
    }
    public static String externalPackageName(String externalClassName)
    {
        String externalPackagePrefix = externalPackagePrefix(externalClassName);
        int length = externalPackagePrefix.length();
        return length > 0 ?
            externalPackagePrefix.substring(0, length - 1) :
            "";
    }
    public static String externalPackagePrefix(String externalClassName)
    {
        return externalClassName.substring(0, externalClassName.lastIndexOf(ClassConstants.EXTERNAL_PACKAGE_SEPARATOR,
                                                                            externalClassName.length() - 2) + 1);
    }
}
