public class Association {
    private final int associationID;
    private final int maxInStreams;
    private final int maxOutStreams;
    protected Association(int associationID,
                          int maxInStreams,
                          int maxOutStreams) {
        this.associationID = associationID;
        this.maxInStreams = maxInStreams;
        this.maxOutStreams = maxOutStreams;
    }
    public final int associationID() {
        return associationID;
    };
    public final int maxInboundStreams() {
        return maxInStreams;
    };
    public final int maxOutboundStreams() {
        return maxOutStreams;
    };
}
