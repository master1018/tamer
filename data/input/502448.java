abstract public class SyncCommon extends EventDisplay {
    private int mLastState; 
    private long mLastStartTime; 
    private long mLastStopTime; 
    private String mLastDetails;
    private int mLastSyncSource; 
    protected static final int CALENDAR = 0;
    protected static final int GMAIL = 1;
    protected static final int FEEDS = 2;
    protected static final int CONTACTS = 3;
    protected static final int ERRORS = 4;
    protected static final int NUM_AUTHS = (CONTACTS + 1);
    protected static final String AUTH_NAMES[] = {"Calendar", "Gmail", "Feeds", "Contacts",
            "Errors"};
    protected static final Color AUTH_COLORS[] = {Color.MAGENTA, Color.GREEN, Color.BLUE,
            Color.ORANGE, Color.RED};
    final int EVENT_SYNC = 2720;
    final int EVENT_TICKLE = 2742;
    final int EVENT_SYNC_DETAILS = 2743;
    final int EVENT_CONTACTS_AGGREGATION = 2747;
    protected SyncCommon(String name) {
        super(name);
    }
    @Override
    void resetUI() {
        mLastStartTime = 0;
        mLastStopTime = 0;
        mLastState = -1;
        mLastSyncSource = -1;
        mLastDetails = "";
    }
    @Override
    void newEvent(EventContainer event, EventLogParser logParser) {
        try {
            if (event.mTag == EVENT_SYNC) {
                int state = Integer.parseInt(event.getValueAsString(1));
                if (state == 0) { 
                    mLastStartTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                    mLastState = 0;
                    mLastSyncSource = Integer.parseInt(event.getValueAsString(2));                    
                    mLastDetails = "";
                } else if (state == 1) { 
                    if (mLastState == 0) {
                        mLastStopTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                        if (mLastStartTime == 0) {
                            mLastStartTime = mLastStopTime;
                        }
                        int auth = getAuth(event.getValueAsString(0));
                        processSyncEvent(event, auth, mLastStartTime, mLastStopTime, mLastDetails,
                                true, mLastSyncSource);
                        mLastState = 1;
                    }
                }
            } else if (event.mTag == EVENT_SYNC_DETAILS) {
                mLastDetails = event.getValueAsString(3);
                if (mLastState != 0) { 
                    long updateTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                    if (updateTime - mLastStopTime <= 250) {
                        int auth = getAuth(event.getValueAsString(0));
                        processSyncEvent(event, auth, mLastStartTime, mLastStopTime, mLastDetails,
                                false, mLastSyncSource);
                    }
                }
            } else if (event.mTag == EVENT_CONTACTS_AGGREGATION) {
                long stopTime = (long) event.sec * 1000L + (event.nsec / 1000000L);
                long startTime = stopTime - Long.parseLong(event.getValueAsString(0));
                String details;
                int count = Integer.parseInt(event.getValueAsString(1));
                if (count < 0) {
                    details = "g" + (-count);
                } else {
                    details = "G" + count;
                }
                processSyncEvent(event, CONTACTS, startTime, stopTime, details,
                        true , mLastSyncSource);
            }
        } catch (InvalidTypeException e) {
        }
    }
    abstract void processSyncEvent(EventContainer event, int auth, long startTime, long stopTime,
            String details, boolean newEvent, int syncSource);
    protected int getAuth(String authname) throws InvalidTypeException {
        if ("calendar".equals(authname) || "cl".equals(authname) ||
                "com.android.calendar".equals(authname)) {
            return CALENDAR;
        } else if ("contacts".equals(authname) || "cp".equals(authname) ||
                "com.android.contacts".equals(authname)) {
            return CONTACTS;
        } else if ("subscribedfeeds".equals(authname)) {
            return FEEDS;
        } else if ("gmail-ls".equals(authname) || "mail".equals(authname)) {
            return GMAIL;
        } else if ("gmail-live".equals(authname)) {
            return GMAIL;
        } else if ("unknown".equals(authname)) {
            return -1; 
        } else {
            throw new InvalidTypeException("Unknown authname " + authname);
        }
    }
}
