final class KeepSpecificationsPanel extends ClassSpecificationsPanel
{
    private final boolean markClasses;
    private final boolean markConditionally;
    private final boolean allowShrinking;
    private final boolean allowOptimization;
    private final boolean allowObfuscation;
    public KeepSpecificationsPanel(JFrame  owner,
                                   boolean markClasses,
                                   boolean markConditionally,
                                   boolean allowShrinking,
                                   boolean allowOptimization,
                                   boolean allowObfuscation)
    {
        super(owner, true);
        this.markClasses       = markClasses;
        this.markConditionally = markConditionally;
        this.allowShrinking    = allowShrinking;
        this.allowOptimization = allowOptimization;
        this.allowObfuscation  = allowObfuscation;
    }
    protected ClassSpecification createClassSpecification()
    {
        return new KeepClassSpecification(markClasses,
                                     markConditionally,
                                     allowShrinking,
                                     allowOptimization,
                                     allowObfuscation);
    }
    protected void setClassSpecification(ClassSpecification classSpecification)
    {
        classSpecificationDialog.setKeepSpecification((KeepClassSpecification)classSpecification);
    }
    protected ClassSpecification getClassSpecification()
    {
        return classSpecificationDialog.getKeepSpecification();
    }
}
