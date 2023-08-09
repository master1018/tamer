public class KeepClassSpecification extends ClassSpecification
{
    public final boolean markClasses;
    public final boolean markConditionally;
    public final boolean allowShrinking;
    public final boolean allowOptimization;
    public final boolean allowObfuscation;
    public KeepClassSpecification(boolean markClasses,
                                  boolean markConditionally,
                                  boolean allowShrinking,
                                  boolean allowOptimization,
                                  boolean allowObfuscation)
    {
        this.markClasses       = markClasses;
        this.markConditionally = markConditionally;
        this.allowShrinking    = allowShrinking;
        this.allowOptimization = allowOptimization;
        this.allowObfuscation  = allowObfuscation;
    }
    public KeepClassSpecification(boolean            markClasses,
                                  boolean            markConditionally,
                                  boolean            allowShrinking,
                                  boolean            allowOptimization,
                                  boolean            allowObfuscation,
                                  ClassSpecification classSpecification)
    {
        super(classSpecification);
        this.markClasses       = markClasses;
        this.markConditionally = markConditionally;
        this.allowShrinking    = allowShrinking;
        this.allowOptimization = allowOptimization;
        this.allowObfuscation  = allowObfuscation;
    }
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }
        KeepClassSpecification other = (KeepClassSpecification)object;
        return
            this.markClasses       == other.markClasses       &&
            this.markConditionally == other.markConditionally &&
            this.allowShrinking    == other.allowShrinking    &&
            this.allowOptimization == other.allowOptimization &&
            this.allowObfuscation  == other.allowObfuscation  &&
            super.equals(other);
    }
    public int hashCode()
    {
        return
            (markClasses       ? 0 :  1) ^
            (markConditionally ? 0 :  2) ^
            (allowShrinking    ? 0 :  4) ^
            (allowOptimization ? 0 :  8) ^
            (allowObfuscation  ? 0 : 16) ^
            super.hashCode();
    }
    public Object clone()
    {
            return super.clone();
    }
}
