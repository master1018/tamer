public abstract class RoundCompleteEvent extends java.util.EventObject {
    private RoundState rs;
    protected RoundCompleteEvent(AnnotationProcessorEnvironment source,
                                 RoundState rs) {
        super(source);
        this.rs = rs;
    }
    public RoundState getRoundState() {
        return rs;
    }
    public AnnotationProcessorEnvironment getSource() {
        return (AnnotationProcessorEnvironment)super.getSource();
    }
}
