public class RoundCompleteEventImpl extends RoundCompleteEvent {
    private static final long serialVersionUID = 7067621446720784300L;
    public RoundCompleteEventImpl(AnnotationProcessorEnvironment source,
                                  RoundState rs) {
        super(source, rs);
    }
}
