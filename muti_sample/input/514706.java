package proguard.classfile;
public interface ClassConstants
{
    public static final String CLASS_FILE_EXTENSION = ".class";
    public static final int MAGIC = 0xCAFEBABE;
    public static final int INTERNAL_CLASS_VERSION_1_0_MAJOR = 45;
    public static final int INTERNAL_CLASS_VERSION_1_0_MINOR = 3;
    public static final int INTERNAL_CLASS_VERSION_1_2_MAJOR = 46;
    public static final int INTERNAL_CLASS_VERSION_1_2_MINOR = 0;
    public static final int INTERNAL_CLASS_VERSION_1_3_MAJOR = 47;
    public static final int INTERNAL_CLASS_VERSION_1_3_MINOR = 0;
    public static final int INTERNAL_CLASS_VERSION_1_4_MAJOR = 48;
    public static final int INTERNAL_CLASS_VERSION_1_4_MINOR = 0;
    public static final int INTERNAL_CLASS_VERSION_1_5_MAJOR = 49;
    public static final int INTERNAL_CLASS_VERSION_1_5_MINOR = 0;
    public static final int INTERNAL_CLASS_VERSION_1_6_MAJOR = 50;
    public static final int INTERNAL_CLASS_VERSION_1_6_MINOR = 0;
    public static final int INTERNAL_CLASS_VERSION_1_0 = (INTERNAL_CLASS_VERSION_1_0_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_0_MINOR;
    public static final int INTERNAL_CLASS_VERSION_1_2 = (INTERNAL_CLASS_VERSION_1_2_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_2_MINOR;
    public static final int INTERNAL_CLASS_VERSION_1_3 = (INTERNAL_CLASS_VERSION_1_3_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_3_MINOR;
    public static final int INTERNAL_CLASS_VERSION_1_4 = (INTERNAL_CLASS_VERSION_1_4_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_4_MINOR;
    public static final int INTERNAL_CLASS_VERSION_1_5 = (INTERNAL_CLASS_VERSION_1_5_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_5_MINOR;
    public static final int INTERNAL_CLASS_VERSION_1_6 = (INTERNAL_CLASS_VERSION_1_6_MAJOR << 16) | INTERNAL_CLASS_VERSION_1_6_MINOR;
    public static final String EXTERNAL_CLASS_VERSION_1_0       = "1.0";
    public static final String EXTERNAL_CLASS_VERSION_1_1       = "1.1";
    public static final String EXTERNAL_CLASS_VERSION_1_2       = "1.2";
    public static final String EXTERNAL_CLASS_VERSION_1_3       = "1.3";
    public static final String EXTERNAL_CLASS_VERSION_1_4       = "1.4";
    public static final String EXTERNAL_CLASS_VERSION_1_5       = "1.5";
    public static final String EXTERNAL_CLASS_VERSION_1_6       = "1.6";
    public static final String EXTERNAL_CLASS_VERSION_1_5_ALIAS = "5";
    public static final String EXTERNAL_CLASS_VERSION_1_6_ALIAS = "6";
    public static final int INTERNAL_ACC_PUBLIC       = 0x0001;
    public static final int INTERNAL_ACC_PRIVATE      = 0x0002;
    public static final int INTERNAL_ACC_PROTECTED    = 0x0004;
    public static final int INTERNAL_ACC_STATIC       = 0x0008;
    public static final int INTERNAL_ACC_FINAL        = 0x0010;
    public static final int INTERNAL_ACC_SUPER        = 0x0020;
    public static final int INTERNAL_ACC_SYNCHRONIZED = 0x0020;
    public static final int INTERNAL_ACC_VOLATILE     = 0x0040;
    public static final int INTERNAL_ACC_TRANSIENT    = 0x0080;
    public static final int INTERNAL_ACC_BRIDGE       = 0x0040;
    public static final int INTERNAL_ACC_VARARGS      = 0x0080;
    public static final int INTERNAL_ACC_NATIVE       = 0x0100;
    public static final int INTERNAL_ACC_INTERFACE    = 0x0200;
    public static final int INTERNAL_ACC_ABSTRACT     = 0x0400;
    public static final int INTERNAL_ACC_STRICT       = 0x0800;
    public static final int INTERNAL_ACC_SYNTHETIC    = 0x1000;
    public static final int INTERNAL_ACC_ANNOTATTION  = 0x2000;
    public static final int INTERNAL_ACC_ENUM         = 0x4000;
    public static final int VALID_INTERNAL_ACC_CLASS  = INTERNAL_ACC_PUBLIC       |
                                                        INTERNAL_ACC_FINAL        |
                                                        INTERNAL_ACC_SUPER        |
                                                        INTERNAL_ACC_INTERFACE    |
                                                        INTERNAL_ACC_ABSTRACT     |
                                                        INTERNAL_ACC_SYNTHETIC    |
                                                        INTERNAL_ACC_ANNOTATTION  |
                                                        INTERNAL_ACC_ENUM;
    public static final int VALID_INTERNAL_ACC_FIELD  = INTERNAL_ACC_PUBLIC       |
                                                        INTERNAL_ACC_PRIVATE      |
                                                        INTERNAL_ACC_PROTECTED    |
                                                        INTERNAL_ACC_STATIC       |
                                                        INTERNAL_ACC_FINAL        |
                                                        INTERNAL_ACC_VOLATILE     |
                                                        INTERNAL_ACC_TRANSIENT    |
                                                        INTERNAL_ACC_SYNTHETIC    |
                                                        INTERNAL_ACC_ENUM;
    public static final int VALID_INTERNAL_ACC_METHOD = INTERNAL_ACC_PUBLIC       |
                                                        INTERNAL_ACC_PRIVATE      |
                                                        INTERNAL_ACC_PROTECTED    |
                                                        INTERNAL_ACC_STATIC       |
                                                        INTERNAL_ACC_FINAL        |
                                                        INTERNAL_ACC_SYNCHRONIZED |
                                                        INTERNAL_ACC_BRIDGE       |
                                                        INTERNAL_ACC_VARARGS      |
                                                        INTERNAL_ACC_NATIVE       |
                                                        INTERNAL_ACC_ABSTRACT     |
                                                        INTERNAL_ACC_STRICT       |
                                                        INTERNAL_ACC_SYNTHETIC;
    public static final String EXTERNAL_ACC_PUBLIC       = "public";
    public static final String EXTERNAL_ACC_PRIVATE      = "private";
    public static final String EXTERNAL_ACC_PROTECTED    = "protected";
    public static final String EXTERNAL_ACC_STATIC       = "static";
    public static final String EXTERNAL_ACC_FINAL        = "final";
    public static final String EXTERNAL_ACC_SUPER        = "super";
    public static final String EXTERNAL_ACC_SYNCHRONIZED = "synchronized";
    public static final String EXTERNAL_ACC_VOLATILE     = "volatile";
    public static final String EXTERNAL_ACC_TRANSIENT    = "transient";
    public static final String EXTERNAL_ACC_NATIVE       = "native";
    public static final String EXTERNAL_ACC_INTERFACE    = "interface";
    public static final String EXTERNAL_ACC_ABSTRACT     = "abstract";
    public static final String EXTERNAL_ACC_STRICT       = "strictfp";
    public static final String EXTERNAL_ACC_ANNOTATION   = "@";
    public static final String EXTERNAL_ACC_ENUM         = "enum";
    public static final int CONSTANT_Utf8               = 1;
    public static final int CONSTANT_Integer            = 3;
    public static final int CONSTANT_Float              = 4;
    public static final int CONSTANT_Long               = 5;
    public static final int CONSTANT_Double             = 6;
    public static final int CONSTANT_Class              = 7;
    public static final int CONSTANT_String             = 8;
    public static final int CONSTANT_Fieldref           = 9;
    public static final int CONSTANT_Methodref          = 10;
    public static final int CONSTANT_InterfaceMethodref = 11;
    public static final int CONSTANT_NameAndType        = 12;
    public static final String ATTR_SourceFile                           = "SourceFile";
    public static final String ATTR_SourceDir                            = "SourceDir";
    public static final String ATTR_InnerClasses                         = "InnerClasses";
    public static final String ATTR_EnclosingMethod                      = "EnclosingMethod";
    public static final String ATTR_Deprecated                           = "Deprecated";
    public static final String ATTR_Synthetic                            = "Synthetic";
    public static final String ATTR_Signature                            = "Signature";
    public static final String ATTR_ConstantValue                        = "ConstantValue";
    public static final String ATTR_Exceptions                           = "Exceptions";
    public static final String ATTR_Code                                 = "Code";
    public static final String ATTR_StackMap                             = "StackMap";
    public static final String ATTR_StackMapTable                        = "StackMapTable";
    public static final String ATTR_LineNumberTable                      = "LineNumberTable";
    public static final String ATTR_LocalVariableTable                   = "LocalVariableTable";
    public static final String ATTR_LocalVariableTypeTable               = "LocalVariableTypeTable";
    public static final String ATTR_RuntimeVisibleAnnotations            = "RuntimeVisibleAnnotations";
    public static final String ATTR_RuntimeInvisibleAnnotations          = "RuntimeInvisibleAnnotations";
    public static final String ATTR_RuntimeVisibleParameterAnnotations   = "RuntimeVisibleParameterAnnotations";
    public static final String ATTR_RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations";
    public static final String ATTR_AnnotationDefault                    = "AnnotationDefault";
    public static final int ELEMENT_VALUE_STRING_CONSTANT = 's';
    public static final int ELEMENT_VALUE_ENUM_CONSTANT   = 'e';
    public static final int ELEMENT_VALUE_CLASS           = 'c';
    public static final int ELEMENT_VALUE_ANNOTATION      = '@';
    public static final int ELEMENT_VALUE_ARRAY           = '[';
    public static final char EXTERNAL_PACKAGE_SEPARATOR     = '.';
    public static final char EXTERNAL_INNER_CLASS_SEPARATOR = '.';
    public static final char INTERNAL_PACKAGE_SEPARATOR     = '/';
    public static final char INTERNAL_INNER_CLASS_SEPARATOR = '$';
    public static final char SPECIAL_CLASS_CHARACTER        = '-';
    public static final char SPECIAL_MEMBER_SEPARATOR       = '$';
    public static final char EXTERNAL_METHOD_ARGUMENTS_OPEN      = '(';
    public static final char EXTERNAL_METHOD_ARGUMENTS_CLOSE     = ')';
    public static final char EXTERNAL_METHOD_ARGUMENTS_SEPARATOR = ',';
    public static final char INTERNAL_METHOD_ARGUMENTS_OPEN  = '(';
    public static final char INTERNAL_METHOD_ARGUMENTS_CLOSE = ')';
    public static final String INTERNAL_PACKAGE_JAVA_LANG         = "java/lang/";
    public static final String INTERNAL_NAME_JAVA_LANG_OBJECT     = "java/lang/Object";
    public static final String INTERNAL_TYPE_JAVA_LANG_OBJECT     = "Ljava/lang/Object;";
    public static final String INTERNAL_NAME_JAVA_LANG_CLONEABLE  = "java/lang/Cloneable";
    public static final String INTERNAL_NAME_JAVA_LANG_THROWABLE  = "java/lang/Throwable";
    public static final String INTERNAL_NAME_JAVA_LANG_CLASS      = "java/lang/Class";
    public static final String INTERNAL_NAME_JAVA_LANG_STRING     = "java/lang/String";
    public static final String INTERNAL_NAME_JAVA_IO_SERIALIZABLE = "java/io/Serializable";
    public static final String INTERNAL_METHOD_NAME_INIT   = "<init>";
    public static final String INTERNAL_METHOD_TYPE_INIT   = "()V";
    public static final String INTERNAL_METHOD_NAME_CLINIT = "<clinit>";
    public static final String INTERNAL_METHOD_TYPE_CLINIT = "()V";
    public static final String INTERNAL_METHOD_NAME_CLASS_FOR_NAME            = "forName";
    public static final String INTERNAL_METHOD_TYPE_CLASS_FOR_NAME            = "(Ljava/lang/String;)Ljava/lang/Class;";
    public static final String INTERNAL_METHOD_NAME_CLASS_GET_COMPONENT_TYPE  = "getComponentType";
    public static final String INTERNAL_METHOD_TYPE_CLASS_GET_COMPONENT_TYPE  = "()Ljava/lang/Class;";
    public static final String INTERNAL_METHOD_NAME_CLASS_GET_FIELD           = "getField";
    public static final String INTERNAL_METHOD_TYPE_CLASS_GET_FIELD           = "(Ljava/lang/String;)Ljava/lang/reflect/Field;";
    public static final String INTERNAL_METHOD_NAME_CLASS_GET_DECLARED_FIELD  = "getDeclaredField";
    public static final String INTERNAL_METHOD_TYPE_CLASS_GET_DECLARED_FIELD  = "(Ljava/lang/String;)Ljava/lang/reflect/Field;";
    public static final String INTERNAL_METHOD_NAME_CLASS_GET_METHOD          = "getMethod";
    public static final String INTERNAL_METHOD_TYPE_CLASS_GET_METHOD          = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;";
    public static final String INTERNAL_METHOD_NAME_CLASS_GET_DECLARED_METHOD = "getDeclaredMethod";
    public static final String INTERNAL_METHOD_TYPE_CLASS_GET_DECLARED_METHOD = "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;";
    public static final String INTERNAL_METHOD_NAME_DOT_CLASS_JAVAC = "class$";
    public static final String INTERNAL_METHOD_TYPE_DOT_CLASS_JAVAC = "(Ljava/lang/String;)Ljava/lang/Class;";
    public static final String INTERNAL_METHOD_NAME_DOT_CLASS_JIKES = "class";
    public static final String INTERNAL_METHOD_TYPE_DOT_CLASS_JIKES = "(Ljava/lang/String;Z)Ljava/lang/Class;";
    public static final String INTERNAL_METHOD_NAME_NEW_INSTANCE = "newInstance";
    public static final String INTERNAL_METHOD_TYPE_NEW_INSTANCE = "()Ljava/lang/Object;";
    public static final char INTERNAL_TYPE_VOID                   = 'V';
    public static final char INTERNAL_TYPE_BOOLEAN                = 'Z';
    public static final char INTERNAL_TYPE_BYTE                   = 'B';
    public static final char INTERNAL_TYPE_CHAR                   = 'C';
    public static final char INTERNAL_TYPE_SHORT                  = 'S';
    public static final char INTERNAL_TYPE_INT                    = 'I';
    public static final char INTERNAL_TYPE_LONG                   = 'J';
    public static final char INTERNAL_TYPE_FLOAT                  = 'F';
    public static final char INTERNAL_TYPE_DOUBLE                 = 'D';
    public static final char INTERNAL_TYPE_CLASS_START            = 'L';
    public static final char INTERNAL_TYPE_CLASS_END              = ';';
    public static final char INTERNAL_TYPE_ARRAY                  = '[';
    public static final char INTERNAL_TYPE_GENERIC_VARIABLE_START = 'T';
    public static final char INTERNAL_TYPE_GENERIC_START          = '<';
    public static final char INTERNAL_TYPE_GENERIC_BOUND          = ':';
    public static final char INTERNAL_TYPE_GENERIC_END            = '>';
    public static final String EXTERNAL_TYPE_JAVA_LANG_OBJECT = "java.lang.Object";
    public static final String EXTERNAL_PACKAGE_JAVA_LANG     = "java.lang.";
    public static final String EXTERNAL_TYPE_VOID    = "void";
    public static final String EXTERNAL_TYPE_BOOLEAN = "boolean";
    public static final String EXTERNAL_TYPE_BYTE    = "byte";
    public static final String EXTERNAL_TYPE_CHAR    = "char";
    public static final String EXTERNAL_TYPE_SHORT   = "short";
    public static final String EXTERNAL_TYPE_INT     = "int";
    public static final String EXTERNAL_TYPE_FLOAT   = "float";
    public static final String EXTERNAL_TYPE_LONG    = "long";
    public static final String EXTERNAL_TYPE_DOUBLE  = "double";
    public static final String EXTERNAL_TYPE_ARRAY   = "[]";
    public static final int TYPICAL_CONSTANT_POOL_SIZE     = 256;
    public static final int TYPICAL_FIELD_COUNT            = 64;
    public static final int TYPICAL_METHOD_COUNT           = 64;
    public static final int TYPICAL_CODE_LENGTH            = 1024;
    public static final int TYPICAL_EXCEPTION_TABLE_LENGTH = 16;
    public static final int TYPICAL_VARIABLES_SIZE         = 64;
    public static final int TYPICAL_STACK_SIZE             = 16;
}
