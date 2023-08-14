package proguard.classfile.visitor;
import proguard.classfile.Clazz;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.ElementValueVisitor;
import proguard.classfile.constant.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
import proguard.classfile.util.SimplifiedVisitor;
public class ReferencedMemberVisitor
extends      SimplifiedVisitor
implements   ConstantVisitor,
             ElementValueVisitor
{
    private final MemberVisitor memberVisitor;
    public ReferencedMemberVisitor(MemberVisitor memberVisitor)
    {
        this.memberVisitor = memberVisitor;
    }
    public void visitAnyConstant(Clazz clazz, Constant constant) {}
    public void visitStringConstant(Clazz clazz, StringConstant stringConstant)
    {
        stringConstant.referencedMemberAccept(memberVisitor);
    }
    public void visitAnyRefConstant(Clazz clazz, RefConstant refConstant)
    {
        refConstant.referencedMemberAccept(memberVisitor);
    }
    public void visitAnyElementValue(Clazz clazz, Annotation annotation, ElementValue elementValue)
    {
        elementValue.referencedMethodAccept(memberVisitor);
    }
}
