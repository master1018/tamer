public class SpecErrorEvent extends SpecEvent {
    private static final long serialVersionUID = 8162634387866409578L;
    private Exception reason;
    public SpecErrorEvent(EventRequestSpec eventRequestSpec,
                                 Exception reason) {
        super(eventRequestSpec);
        this.reason = reason;
    }
    public Exception getReason() {
        return reason;
    }
}
