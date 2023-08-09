public class SigAnnotationFieldDelta extends SigMemberDelta<IAnnotationField>
        implements IAnnotationFieldDelta {
    private IValueDelta defaultValueDelta;
    public SigAnnotationFieldDelta(IAnnotationField from, IAnnotationField to) {
        super(from, to);
    }
    public IValueDelta getDefaultValueDelta() {
        return defaultValueDelta;
    }
    public void setDefaultValueDelta(IValueDelta valueDelta) {
        this.defaultValueDelta = valueDelta;
    }
}
