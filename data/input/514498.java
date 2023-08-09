class BroadcastRecord extends Binder {
    final Intent intent;    
    final ProcessRecord callerApp; 
    final String callerPackage; 
    final int callingPid;   
    final int callingUid;   
    final boolean ordered;  
    final boolean sticky;   
    final boolean initialSticky; 
    final String requiredPermission; 
    final List receivers;   
    final IIntentReceiver resultTo; 
    long dispatchTime;      
    long receiverTime;      
    long finishTime;        
    int resultCode;         
    String resultData;      
    Bundle resultExtras;    
    boolean resultAbort;    
    int nextReceiver;       
    IBinder receiver;       
    int state;
    int anrCount;           
    static final int IDLE = 0;
    static final int APP_RECEIVE = 1;
    static final int CALL_IN_RECEIVE = 2;
    static final int CALL_DONE_RECEIVE = 3;
    BroadcastFilter curFilter;
    ProcessRecord curApp;       
    ComponentName curComponent; 
    ActivityInfo curReceiver;   
    void dump(PrintWriter pw, String prefix) {
        pw.println(prefix + this);
        pw.println(prefix + intent);
        if (sticky) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                pw.println(prefix + "extras: " + bundle.toString());
            }
        }
        pw.println(prefix + "proc=" + callerApp);
        pw.println(prefix + "caller=" + callerPackage
                + " callingPid=" + callingPid
                + " callingUid=" + callingUid);
        if (requiredPermission != null) {
            pw.println(prefix + "requiredPermission=" + requiredPermission);
        }
        pw.println(prefix + "dispatchTime=" + dispatchTime + " ("
                + (SystemClock.uptimeMillis()-dispatchTime) + "ms since now)");
        if (finishTime != 0) {
            pw.println(prefix + "finishTime=" + finishTime + " ("
                    + (SystemClock.uptimeMillis()-finishTime) + "ms since now)");
        } else {
            pw.println(prefix + "receiverTime=" + receiverTime + " ("
                    + (SystemClock.uptimeMillis()-receiverTime) + "ms since now)");
        }
        if (anrCount != 0) {
            pw.println(prefix + "anrCount=" + anrCount);
        }
        if (resultTo != null || resultCode != -1 || resultData != null) {
            pw.println(prefix + "resultTo=" + resultTo
                  + " resultCode=" + resultCode + " resultData=" + resultData);
        }
        if (resultExtras != null) {
            pw.println(prefix + "resultExtras=" + resultExtras);
        }
        if (resultAbort || ordered || sticky || initialSticky) {
            pw.println(prefix + "resultAbort=" + resultAbort
                    + " ordered=" + ordered + " sticky=" + sticky
                    + " initialSticky=" + initialSticky);
        }
        if (nextReceiver != 0 || receiver != null) {
            pw.println(prefix + "nextReceiver=" + nextReceiver
                  + " receiver=" + receiver);
        }
        if (curFilter != null) {
            pw.println(prefix + "curFilter=" + curFilter);
        }
        if (curReceiver != null) {
            pw.println(prefix + "curReceiver=" + curReceiver);
        }
        if (curApp != null) {
            pw.println(prefix + "curApp=" + curApp);
            pw.println(prefix + "curComponent="
                    + (curComponent != null ? curComponent.toShortString() : "--"));
            if (curReceiver != null && curReceiver.applicationInfo != null) {
                pw.println(prefix + "curSourceDir=" + curReceiver.applicationInfo.sourceDir);
            }
        }
        String stateStr = " (?)";
        switch (state) {
            case IDLE:              stateStr=" (IDLE)"; break;
            case APP_RECEIVE:       stateStr=" (APP_RECEIVE)"; break;
            case CALL_IN_RECEIVE:   stateStr=" (CALL_IN_RECEIVE)"; break;
            case CALL_DONE_RECEIVE: stateStr=" (CALL_DONE_RECEIVE)"; break;
        }
        pw.println(prefix + "state=" + state + stateStr);
        final int N = receivers != null ? receivers.size() : 0;
        String p2 = prefix + "  ";
        PrintWriterPrinter printer = new PrintWriterPrinter(pw);
        for (int i=0; i<N; i++) {
            Object o = receivers.get(i);
            pw.println(prefix + "Receiver #" + i + ": " + o);
            if (o instanceof BroadcastFilter)
                ((BroadcastFilter)o).dumpBrief(pw, p2);
            else if (o instanceof ResolveInfo)
                ((ResolveInfo)o).dump(printer, p2);
        }
    }
    BroadcastRecord(Intent _intent, ProcessRecord _callerApp, String _callerPackage,
            int _callingPid, int _callingUid, String _requiredPermission,
            List _receivers, IIntentReceiver _resultTo, int _resultCode,
            String _resultData, Bundle _resultExtras, boolean _serialized,
            boolean _sticky, boolean _initialSticky) {
        intent = _intent;
        callerApp = _callerApp;
        callerPackage = _callerPackage;
        callingPid = _callingPid;
        callingUid = _callingUid;
        requiredPermission = _requiredPermission;
        receivers = _receivers;
        resultTo = _resultTo;
        resultCode = _resultCode;
        resultData = _resultData;
        resultExtras = _resultExtras;
        ordered = _serialized;
        sticky = _sticky;
        initialSticky = _initialSticky;
        nextReceiver = 0;
        state = IDLE;
    }
    public String toString() {
        return "BroadcastRecord{"
            + Integer.toHexString(System.identityHashCode(this))
            + " " + intent.getAction() + "}";
    }
}
