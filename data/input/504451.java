package proguard.classfile;
import proguard.classfile.visitor.MemberVisitor;
public abstract class LibraryMember implements Member
{
    private static final int ACC_VISIBLE = ClassConstants.INTERNAL_ACC_PUBLIC |
                                           ClassConstants.INTERNAL_ACC_PROTECTED;
    public int    u2accessFlags;
    public String name;
    public String descriptor;
    public Object visitorInfo;
    protected LibraryMember()
    {
    }
    protected LibraryMember(int    u2accessFlags,
                            String name,
                            String descriptor)
    {
        this.u2accessFlags = u2accessFlags;
        this.name          = name;
        this.descriptor    = descriptor;
    }
    public abstract void accept(LibraryClass  libraryClass,
                                MemberVisitor memberVisitor);
    public int getAccessFlags()
    {
        return u2accessFlags;
    }
    public String getName(Clazz clazz)
    {
        return name;
    }
    public String getDescriptor(Clazz clazz)
    {
        return descriptor;
    }
    public void accept(Clazz clazz, MemberVisitor memberVisitor)
    {
        accept((LibraryClass)clazz, memberVisitor);
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
