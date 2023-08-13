import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.AttributeVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
public class NameAndTypeUsageMarker
extends      SimplifiedVisitor
implements   ClassVisitor,
             ConstantVisitor,
             AttributeVisitor
{
    private static final Object USED = new Object();
    public void visitProgramClass(ProgramClass programClass)
    {
        programClass.constantPoolEntriesAccept(this);
        programClass.attributesAccept(this);
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitFieldrefConstant(Clazz clazz, FieldrefConstant fieldrefConstant)
    {
        visitRefConstant(clazz, fieldrefConstant);
    }
    public void visitInterfaceMethodrefConstant(Clazz clazz, InterfaceMethodrefConstant interfaceMethodrefConstant)
    {
        visitRefConstant(clazz, interfaceMethodrefConstant);
    }
    public void visitMethodrefConstant(Clazz clazz, MethodrefConstant methodrefConstant)
    {
        visitRefConstant(clazz, methodrefConstant);
    }
    private void visitRefConstant(Clazz clazz, RefConstant refConstant)
    {
        markNameAndTypeConstant(clazz, refConstant.u2nameAndTypeIndex);
    }
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitEnclosingMethodAttribute(Clazz clazz, EnclosingMethodAttribute enclosingMethodAttribute)
    {
        if (enclosingMethodAttribute.u2nameAndTypeIndex != 0)
        {
            markNameAndTypeConstant(clazz, enclosingMethodAttribute.u2nameAndTypeIndex);
        }
    }
    private void markNameAndTypeConstant(Clazz clazz, int index)
    {
         markAsUsed((NameAndTypeConstant)((ProgramClass)clazz).getConstant(index));
    }
    private static void markAsUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(USED);
    }
    static boolean isUsed(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == USED;
    }
}
