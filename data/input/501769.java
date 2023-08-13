package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class NameAndTypeConstant extends Constant
{
    public int u2nameIndex;
    public int u2descriptorIndex;
    public NameAndTypeConstant()
    {
    }
    public NameAndTypeConstant(int u2nameIndex,
                               int u2descriptorIndex)
    {
        this.u2nameIndex       = u2nameIndex;
        this.u2descriptorIndex = u2descriptorIndex;
    }
    protected int getNameIndex()
    {
        return u2nameIndex;
    }
    protected void setNameIndex(int index)
    {
        u2nameIndex = index;
    }
    protected int getDescriptorIndex()
    {
        return u2descriptorIndex;
    }
    protected void setDescriptorIndex(int index)
    {
        u2descriptorIndex = index;
    }
    public String getName(Clazz clazz)
    {
        return clazz.getString(u2nameIndex);
    }
    public String getType(Clazz clazz)
    {
        return clazz.getString(u2descriptorIndex);
    }
    public int getTag()
    {
        return ClassConstants.CONSTANT_NameAndType;
    }
    public void accept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        constantVisitor.visitNameAndTypeConstant(clazz, this);
    }
}
