package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class FloatConstant extends Constant
{
    public float f4value;
    public FloatConstant()
    {
    }
    public FloatConstant(float value)
    {
        f4value = value;
    }
    public float getValue()
    {
        return f4value;
    }
    public void setValue(float value)
    {
        f4value = value;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Float;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitFloatConstant(clazz, this);
    }
}
