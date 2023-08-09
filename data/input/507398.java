package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class IntegerConstant extends Constant
{
    public int u4value;
    public IntegerConstant()
    {
    }
    public IntegerConstant(int value)
    {
        u4value = value;
    }
    public int getValue()
    {
        return u4value;
    }
    public void setValue(int value)
    {
        u4value = value;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Integer;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitIntegerConstant(clazz, this);
    }
}
