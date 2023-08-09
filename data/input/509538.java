public class MonkeyNoopEvent extends MonkeyEvent {
    public MonkeyNoopEvent() {
        super(MonkeyEvent.EVENT_TYPE_NOOP);
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 1) {
            System.out.println("NOOP");
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
