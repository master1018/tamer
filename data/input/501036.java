package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class LongConstant extends Constant
{
    public long u8value;
    public LongConstant()
    {
    }
    public LongConstant(long value)
    {
        u8value = value;
    }
    public long getValue()
    {
        return u8value;
    }
    public void setValue(long value)
    {
        u8value = value;
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_Long;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitLongConstant(clazz, this);
    }
}
