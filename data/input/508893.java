package proguard.classfile.io;
import proguard.classfile.*;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import java.io.DataInput;
public class LibraryClassReader
extends      SimplifiedVisitor
implements   ClassVisitor,
             MemberVisitor,
             ConstantVisitor
{
    private static final LibraryField[]  EMPTY_LIBRARY_FIELDS  = new LibraryField[0];
    private static final LibraryMethod[] EMPTY_LIBRARY_METHODS = new LibraryMethod[0];
    private final RuntimeDataInput dataInput;
    private final boolean          skipNonPublicClasses;
    private final boolean          skipNonPublicClassMembers;
    private Constant[]      constantPool;
    public LibraryClassReader(DataInput dataInput,
                              boolean   skipNonPublicClasses,
                              boolean   skipNonPublicClassMembers)
    {
        this.dataInput                 = new RuntimeDataInput(dataInput);
        this.skipNonPublicClasses      = skipNonPublicClasses;
        this.skipNonPublicClassMembers = skipNonPublicClassMembers;
    }
    public void visitProgramClass(ProgramClass libraryClass)
    {
    }
    public void visitLibraryClass(LibraryClass libraryClass)
    {
        int u4magic = dataInput.readInt();
        ClassUtil.checkMagicNumber(u4magic);
        int u2minorVersion = dataInput.readUnsignedShort();
        int u2majorVersion = dataInput.readUnsignedShort();
        int u4version = ClassUtil.internalClassVersion(u2majorVersion,
                                                       u2minorVersion);
        ClassUtil.checkVersionNumbers(u4version);
        int u2constantPoolCount = dataInput.readUnsignedShort();
        constantPool = new Constant[u2constantPoolCount];
        for (int index = 1; index < u2constantPoolCount; index++)
        {
            Constant constant = createConstant();
            constant.accept(libraryClass, this);
            int tag = constant.getTag();
            if (tag == ClassConstants.CONSTANT_Class ||
                tag == ClassConstants.CONSTANT_Utf8)
            {
                constantPool[index] = constant;
            }
            if (tag == ClassConstants.CONSTANT_Long ||
                tag == ClassConstants.CONSTANT_Double)
            {
                index++;
            }
        }
        libraryClass.u2accessFlags = dataInput.readUnsignedShort();
        if (skipNonPublicClasses &&
            AccessUtil.accessLevel(libraryClass.getAccessFlags()) < AccessUtil.PUBLIC)
        {
            return;
        }
        int u2thisClass  = dataInput.readUnsignedShort();
        int u2superClass = dataInput.readUnsignedShort();
        libraryClass.thisClassName  = getClassName(u2thisClass);
        libraryClass.superClassName = (u2superClass == 0) ? null :
                                      getClassName(u2superClass);
        int u2interfacesCount = dataInput.readUnsignedShort();
        libraryClass.interfaceNames = new String[u2interfacesCount];
        for (int index = 0; index < u2interfacesCount; index++)
        {
            int u2interface = dataInput.readUnsignedShort();
            libraryClass.interfaceNames[index] = getClassName(u2interface);
        }
        int u2fieldsCount = dataInput.readUnsignedShort();
        LibraryField[] reusableFields = new LibraryField[u2fieldsCount];
        int visibleFieldsCount = 0;
        for (int index = 0; index < u2fieldsCount; index++)
        {
            LibraryField field = new LibraryField();
            this.visitLibraryMember(libraryClass, field);
            if (AccessUtil.accessLevel(field.getAccessFlags()) >=
                (skipNonPublicClassMembers ? AccessUtil.PROTECTED :
                                             AccessUtil.PACKAGE_VISIBLE))
            {
                reusableFields[visibleFieldsCount++] = field;
            }
        }
        if (visibleFieldsCount == 0)
        {
            libraryClass.fields = EMPTY_LIBRARY_FIELDS;
        }
        else
        {
            libraryClass.fields = new LibraryField[visibleFieldsCount];
            System.arraycopy(reusableFields, 0, libraryClass.fields, 0, visibleFieldsCount);
        }
        int u2methodsCount = dataInput.readUnsignedShort();
        LibraryMethod[] reusableMethods = new LibraryMethod[u2methodsCount];
        int visibleMethodsCount = 0;
        for (int index = 0; index < u2methodsCount; index++)
        {
            LibraryMethod method = new LibraryMethod();
            this.visitLibraryMember(libraryClass, method);
            if (AccessUtil.accessLevel(method.getAccessFlags()) >=
                (skipNonPublicClassMembers ? AccessUtil.PROTECTED :
                                             AccessUtil.PACKAGE_VISIBLE))
            {
                reusableMethods[visibleMethodsCount++] = method;
            }
        }
        if (visibleMethodsCount == 0)
        {
            libraryClass.methods = EMPTY_LIBRARY_METHODS;
        }
        else
        {
            libraryClass.methods = new LibraryMethod[visibleMethodsCount];
            System.arraycopy(reusableMethods, 0, libraryClass.methods, 0, visibleMethodsCount);
        }
        skipAttributes();
    }
    public void visitProgramMember(ProgramClass libraryClass, ProgramMember libraryMember)
    {
    }
    public void visitLibraryMember(LibraryClass libraryClass, LibraryMember libraryMember)
    {
        libraryMember.u2accessFlags = dataInput.readUnsignedShort();
        libraryMember.name          = getString(dataInput.readUnsignedShort());
        libraryMember.descriptor    = getString(dataInput.readUnsignedShort());
        skipAttributes();
    }
    public void visitIntegerConstant(Clazz clazz, IntegerConstant integerConstant)
    {
        dataInput.skipBytes(4);
    }
    public void visitLongConstant(Clazz clazz, LongConstant longConstant)
    {
        dataInput.skipBytes(8);
    }
    public void visitFloatConstant(Clazz clazz, FloatConstant floatConstant)
    {
        dataInput.skipBytes(4);
    }
    public void visitDoubleConstant(Clazz clazz, DoubleConstant doubleConstant)
    {
        dataInput.skipBytes(8);
    }
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        dataInput.skipBytes(2);
    }
    public void visitUtf8Constant(Clazz clazz, Utf8Constant utf8Constant)
    {
        int u2length = dataInput.readUnsignedShort();
        byte[] bytes = new byte[u2length];
        dataInput.readFully(bytes);
        utf8Constant.setBytes(bytes);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        dataInput.skipBytes(4);
    }
    public void visitClassConstant(Clazz clazz, ClassConstant classConstant)
    {
        classConstant.u2nameIndex = dataInput.readUnsignedShort();
    }
    public void visitNameAndTypeConstant(Clazz clazz, NameAndTypeConstant nameAndTypeConstant)
    {
        dataInput.skipBytes(4);
    }
    private String getClassName(int constantIndex)
    {
        ClassConstant classEntry = (ClassConstant)constantPool[constantIndex];
        return getString(classEntry.u2nameIndex);
    }
    private String getString(int constantIndex)
    {
        return ((Utf8Constant)constantPool[constantIndex]).getString();
    }
    private Constant createConstant()
    {
        int u1tag = dataInput.readUnsignedByte();
        switch (u1tag)
        {
            case ClassConstants.CONSTANT_Utf8:               return new Utf8Constant();
            case ClassConstants.CONSTANT_Integer:            return new IntegerConstant();
            case ClassConstants.CONSTANT_Float:              return new FloatConstant();
            case ClassConstants.CONSTANT_Long:               return new LongConstant();
            case ClassConstants.CONSTANT_Double:             return new DoubleConstant();
            case ClassConstants.CONSTANT_String:             return new StringConstant();
            case ClassConstants.CONSTANT_Fieldref:           return new FieldrefConstant();
            case ClassConstants.CONSTANT_Methodref:          return new MethodrefConstant();
            case ClassConstants.CONSTANT_InterfaceMethodref: return new InterfaceMethodrefConstant();
            case ClassConstants.CONSTANT_Class:              return new ClassConstant();
            case ClassConstants.CONSTANT_NameAndType:        return new NameAndTypeConstant();
            default: throw new RuntimeException("Unknown constant type ["+u1tag+"] in constant pool");
        }
    }
    private void skipAttributes()
    {
        int u2attributesCount = dataInput.readUnsignedShort();
        for (int index = 0; index < u2attributesCount; index++)
        {
            skipAttribute();
        }
    }
    private void skipAttribute()
    {
        dataInput.skipBytes(2);
        int u4attributeLength = dataInput.readInt();
        dataInput.skipBytes(u4attributeLength);
    }
}
