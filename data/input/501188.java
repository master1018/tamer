import proguard.classfile.Clazz;
public class ClassOptimizationInfo
{
    private boolean isInstantiated                = false;
    private boolean isInstanceofed                = false;
    private boolean isDotClassed                  = false;
    private boolean isCaught                      = false;
    private boolean containsPackageVisibleMembers = false;
    private boolean invokesPackageVisibleMembers  = false;
    private Clazz   targetClass;
    public void setInstantiated()
    {
        isInstantiated = true;
    }
    public boolean isInstantiated()
    {
        return isInstantiated;
    }
    public void setInstanceofed()
    {
        isInstanceofed = true;
    }
    public boolean isInstanceofed()
    {
        return isInstanceofed;
    }
    public void setDotClassed()
    {
        isDotClassed = true;
    }
    public boolean isDotClassed()
    {
        return isDotClassed;
    }
    public void setCaught()
    {
        isCaught = true;
    }
    public boolean isCaught()
    {
        return isCaught;
    }
    public void setContainsPackageVisibleMembers()
    {
        containsPackageVisibleMembers = true;
    }
    public boolean containsPackageVisibleMembers()
    {
        return containsPackageVisibleMembers;
    }
    public void setInvokesPackageVisibleMembers()
    {
        invokesPackageVisibleMembers = true;
    }
    public boolean invokesPackageVisibleMembers()
    {
        return invokesPackageVisibleMembers;
    }
    public void setTargetClass(Clazz targetClass)
    {
        this.targetClass = targetClass;
    }
    public Clazz getTargetClass()
    {
        return targetClass;
    }
    public void merge(ClassOptimizationInfo other)
    {
        this.isInstantiated                |= other.isInstantiated;
        this.isInstanceofed                |= other.isInstanceofed;
        this.isDotClassed                  |= other.isDotClassed;
        this.isCaught                      |= other.isCaught;
        this.containsPackageVisibleMembers |= other.containsPackageVisibleMembers;
        this.invokesPackageVisibleMembers  |= other.invokesPackageVisibleMembers;
    }
    public static void setClassOptimizationInfo(Clazz clazz)
    {
        clazz.setVisitorInfo(new ClassOptimizationInfo());
    }
    public static ClassOptimizationInfo getClassOptimizationInfo(Clazz clazz)
    {
        Object visitorInfo = clazz.getVisitorInfo();
        return visitorInfo instanceof ClassOptimizationInfo ?
            (ClassOptimizationInfo)visitorInfo :
            null;
    }
}
