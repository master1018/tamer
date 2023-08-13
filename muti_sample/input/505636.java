import proguard.classfile.*;
import proguard.classfile.visitor.*;
final class ShortestUsageMark
{
    private final boolean certain;
    private final String  reason;
    private final int     depth;
    private       Clazz   clazz;
    private       Member  member;
    public ShortestUsageMark(String reason)
    {
        this.certain = true;
        this.reason  = reason;
        this.depth   = 0;
    }
    public ShortestUsageMark(ShortestUsageMark previousUsageMark,
                             String            reason,
                             int               cost,
                             Clazz             clazz)
    {
        this(previousUsageMark, reason, cost, clazz, null);
    }
    public ShortestUsageMark(ShortestUsageMark previousUsageMark,
                             String            reason,
                             int               cost,
                             Clazz             clazz,
                             Member            member)
    {
        this.certain = true;
        this.reason  = reason;
        this.depth   = previousUsageMark.depth + cost;
        this.clazz   = clazz;
        this.member  = member;
    }
    public ShortestUsageMark(ShortestUsageMark otherUsageMark,
                             boolean           certain)
    {
        this.certain = certain;
        this.reason  = otherUsageMark.reason;
        this.depth   = otherUsageMark.depth;
        this.clazz   = otherUsageMark.clazz;
        this.member  = otherUsageMark.member;
    }
    public boolean isCertain()
    {
        return certain;
    }
    public String getReason()
    {
        return reason;
    }
    public boolean isShorter(ShortestUsageMark otherUsageMark)
    {
        return this.depth < otherUsageMark.depth;
    }
    public boolean isCausedBy(Clazz clazz)
    {
        return clazz.equals(this.clazz);
    }
    public void acceptClassVisitor(ClassVisitor classVisitor)
    {
        if (clazz  != null &&
            member == null)
        {
            clazz.accept(classVisitor);
        }
    }
    public void acceptMemberVisitor(MemberVisitor memberVisitor)
    {
        if (clazz  != null &&
            member != null)
        {
            member.accept(clazz, memberVisitor);
        }
    }
    public String toString()
    {
        return "certain=" + certain + ", depth="+depth+": " +
               reason +
               (clazz      != null ? clazz.getName() : "(none)") + ": " +
               (member     != null ? member.getName(clazz) : "(none)");
    }
}
