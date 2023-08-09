package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class DoubleConstant extends Constant
{
    public double f8value;
    public DoubleConstant()
    {
    }
    public DoubleConstant(double value)
    {
        f8value = value;
    }
    public double getValue()
    {
        return f8value;
    }
    public void setValue(double value)
    {
        f8value = value;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Double;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitDoubleConstant(clazz, this);
    }
}
