public class SigAnnotationElementDelta extends SigDelta<IAnnotationElement>
        implements IAnnotationElementDelta {
    private IValueDelta valueDelta;
    public SigAnnotationElementDelta(IAnnotationElement from,
            IAnnotationElement to) {
        super(from, to);
    }
    public IValueDelta getValueDelta() {
        return valueDelta;
    }
    public void setValueDelta(IValueDelta valueDelta) {
        this.valueDelta = valueDelta;
    }
}
