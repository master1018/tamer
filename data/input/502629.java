package proguard.classfile.constant;
import proguard.classfile.*;
import proguard.classfile.constant.visitor.ConstantVisitor;
public abstract class Constant implements VisitorAccepter
{
    public Object visitorInfo;
    public abstract int getTag();
    public abstract void accept(Clazz clazz, ConstantVisitor constantVisitor);
    public Object getVisitorInfo()
    {
        return visitorInfo;
    }
    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
