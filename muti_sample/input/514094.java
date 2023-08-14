package proguard.classfile.editor;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.util.SimplifiedVisitor;
public class StackSizeUpdater
extends      SimplifiedVisitor
implements   AttributeVisitor
{
    private final StackSizeComputer stackSizeComputer = new StackSizeComputer();
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) {}
    public void visitCodeAttribute(Clazz clazz, Method method, CodeAttribute codeAttribute)
    {
        stackSizeComputer.visitCodeAttribute(clazz, method, codeAttribute);
        codeAttribute.u2maxStack = stackSizeComputer.getMaxStackSize();
    }
}
