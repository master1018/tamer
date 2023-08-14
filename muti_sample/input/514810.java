public class MonkeyActivityEvent extends MonkeyEvent {    
    private ComponentName mApp; 
    public MonkeyActivityEvent(ComponentName app) {
        super(EVENT_TYPE_ACTIVITY);
        mApp = app;
    }
    private Intent getEvent() {        
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(mApp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);        
        return intent;
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        Intent intent = getEvent();
        if (verbose > 0) {
            System.out.println(":Switch: " + intent.toURI());
        }
        try {
            iam.startActivity(null, intent, null, null, 0, null, null, 0,
                    false, false);
        } catch (RemoteException e) {
            System.err.println("** Failed talking with activity manager!");
            return MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION;
        } catch (SecurityException e) {
            if (verbose > 0) {
                System.out.println("** Permissions error starting activity "
                        + intent.toURI());
            }
            return MonkeyEvent.INJECT_ERROR_SECURITY_EXCEPTION;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
