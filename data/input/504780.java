package proguard.classfile;
import proguard.classfile.visitor.*;
public interface Member extends VisitorAccepter
{
    public int getAccessFlags();
    public String getName(Clazz clazz);
    public String getDescriptor(Clazz clazz);
    public void accept(Clazz clazz, MemberVisitor memberVisitor);
    public void referencedClassesAccept(ClassVisitor classVisitor);
}
