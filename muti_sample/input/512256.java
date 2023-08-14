public class KeepSpecificationElement extends ClassSpecificationElement
{
    private boolean allowShrinking;
    private boolean allowOptimization;
    private boolean allowObfuscation;
    public void appendTo(List    keepSpecifications,
                         boolean markClasses,
                         boolean markConditionally)
    {
        KeepSpecificationElement keepSpecificationElement = isReference() ?
            (KeepSpecificationElement)getCheckedRef(this.getClass(),
                                                     this.getClass().getName()) :
            this;
        KeepClassSpecification keepClassSpecification =
            new KeepClassSpecification(markClasses,
                                  markConditionally,
                                  allowShrinking,
                                  allowOptimization,
                                  allowObfuscation,
                                  createClassSpecification(keepSpecificationElement));
        keepSpecifications.add(keepClassSpecification);
    }
    public void setAllowshrinking(boolean allowShrinking)
    {
        this.allowShrinking = allowShrinking;
    }
    public void setAllowoptimization(boolean allowOptimization)
    {
        this.allowOptimization = allowOptimization;
    }
    public void setAllowobfuscation(boolean allowObfuscation)
    {
        this.allowObfuscation = allowObfuscation;
    }
}
