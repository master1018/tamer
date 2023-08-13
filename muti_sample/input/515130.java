public class MonkeyInstrumentationEvent extends MonkeyEvent {
    String mRunnerName;
    String mTestCaseName;
    public MonkeyInstrumentationEvent(String testCaseName, String runnerName) {
        super(EVENT_TYPE_ACTIVITY);
        mTestCaseName = testCaseName;
        mRunnerName = runnerName;
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        ComponentName cn = ComponentName.unflattenFromString(mRunnerName);
        if (cn == null || mTestCaseName == null)
            throw new IllegalArgumentException("Bad component name");
        Bundle args = new Bundle();
        args.putString("class", mTestCaseName);
        try {
            iam.startInstrumentation(cn, null, 0, args, null);
        } catch (RemoteException e) {
            System.err.println("** Failed talking with activity manager!");
            return MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
