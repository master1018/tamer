package proguard.classfile;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.visitor.*;
public interface Clazz extends VisitorAccepter
{
    public int getAccessFlags();
    public String getName();
    public String getSuperName();
    public int getInterfaceCount();
    public String getInterfaceName(int index);
    public int getTag(int constantIndex);
    public String getString(int constantIndex);
    public String getStringString(int constantIndex);
    public String getClassName(int constantIndex);
    public String getName(int constantIndex);
    public String getType(int constantIndex);
    public void addSubClass(Clazz clazz);
    public Clazz getSuperClass();
    public Clazz getInterface(int index);
    public boolean extends_(Clazz clazz);
    public boolean extendsOrImplements(Clazz clazz);
    Field findField(String name, String descriptor);
    Method findMethod(String name, String descriptor);
    public void accept(ClassVisitor classVisitor);
    public void hierarchyAccept(boolean      visitThisClass,
                                boolean      visitSuperClass,
                                boolean      visitInterfaces,
                                boolean      visitSubclasses,
                                ClassVisitor classVisitor);
    public void subclassesAccept(ClassVisitor classVisitor);
    public void constantPoolEntriesAccept(ConstantVisitor constantVisitor);
    public void constantPoolEntryAccept(int index, ConstantVisitor constantVisitor);
    public void thisClassConstantAccept(ConstantVisitor constantVisitor);
    public void superClassConstantAccept(ConstantVisitor constantVisitor);
    public void interfaceConstantsAccept(ConstantVisitor constantVisitor);
    public void fieldsAccept(MemberVisitor memberVisitor);
    public void fieldAccept(String name, String descriptor, MemberVisitor memberVisitor);
    public void methodsAccept(MemberVisitor memberVisitor);
    public void methodAccept(String name, String descriptor, MemberVisitor memberVisitor);
    public boolean mayHaveImplementations(Method method);
    public void attributesAccept(AttributeVisitor attributeVisitor);
}
