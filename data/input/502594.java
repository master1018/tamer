package proguard.classfile.attribute;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public class InnerClassesInfo implements VisitorAccepter
{
    public int u2innerClassIndex;
    public int u2outerClassIndex;
    public int u2innerNameIndex;
    public int u2innerClassAccessFlags;
    public Object visitorInfo;
    protected int getInnerClassIndex()
    {
        return u2innerClassIndex;
    }
    protected int getInnerNameIndex()
    {
        return u2innerNameIndex;
    }
    protected void setInnerNameIndex(int index)
    {
        u2innerNameIndex = index;
    }
    public void innerClassConstantAccept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        if (u2innerClassIndex != 0)
        {
            clazz.constantPoolEntryAccept(u2innerClassIndex,
                                                 constantVisitor);
        }
    }
    public void outerClassConstantAccept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        if (u2outerClassIndex != 0)
        {
            clazz.constantPoolEntryAccept(u2outerClassIndex,
                                                 constantVisitor);
        }
    }
    public void innerNameConstantAccept(Clazz clazz, ConstantVisitor constantVisitor)
    {
        if (u2innerNameIndex != 0)
        {
            clazz.constantPoolEntryAccept(u2innerNameIndex,
                                                 constantVisitor);
        }
    }
    public Object getVisitorInfo()
    {
        return visitorInfo;
    }
    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
