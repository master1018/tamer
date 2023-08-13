public class MonkeyPowerEvent extends MonkeyEvent {
    private static final String TAG = "PowerTester";
    private static final String LOG_FILE = "/sdcard/autotester.log";
    private static ArrayList<ContentValues> mLogEvents = new ArrayList<ContentValues>();
    private static final String TEST_SEQ_BEGIN = "AUTOTEST_SEQUENCE_BEGIN";
    private static final String TEST_STARTED = "AUTOTEST_TEST_BEGIN";
    private static final String TEST_ENDED = "AUTOTEST_TEST_SUCCESS";
    private static final String TEST_IDLE_ENDED = "AUTOTEST_IDLE_SUCCESS";
    private static long mTestStartTime;
    private String mPowerLogTag;
    private String mTestResult;
    public MonkeyPowerEvent(String powerLogTag, String powerTestResult) {
        super(EVENT_TYPE_ACTIVITY);
        mPowerLogTag = powerLogTag;
        mTestResult = powerTestResult;
    }
    public MonkeyPowerEvent(String powerLogTag) {
        super(EVENT_TYPE_ACTIVITY);
        mPowerLogTag = powerLogTag;
        mTestResult = null;
    }
    public MonkeyPowerEvent() {
        super(EVENT_TYPE_ACTIVITY);
        mPowerLogTag = null;
        mTestResult = null;
    }
    private void bufferLogEvent(String tag, String value) {
        long tagTime = System.currentTimeMillis();
        if (tag.compareTo(TEST_STARTED) == 0) {
            mTestStartTime = tagTime;
        } else if (tag.compareTo(TEST_IDLE_ENDED) == 0) {
            long lagTime = Long.parseLong(value);
            tagTime = mTestStartTime + lagTime;
            tag = TEST_ENDED;
        }
        ContentValues event = new ContentValues();
        event.put("date", tagTime);
        event.put("tag", tag);
        if (value != null) {
            event.put("value", value);
        }
        mLogEvents.add(event);
    }
    private void writeLogEvents() {
        ContentValues[] events;
        events = mLogEvents.toArray(new ContentValues[0]);
        mLogEvents.clear();
        FileWriter writer = null;
        try {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < events.length; ++i) {
                ContentValues event = events[i];
                buffer.append(MonkeyUtils.toCalendarTime(event.getAsLong("date")));
                buffer.append(event.getAsString("tag"));
                if (event.containsKey("value")) {
                    String value = event.getAsString("value");
                    buffer.append(" ");
                    buffer.append(value.replace('\n', '/'));
                }
                buffer.append("\n");
            }
            writer = new FileWriter(LOG_FILE, true); 
            writer.write(buffer.toString());
        } catch (IOException e) {
            Log.w(TAG, "Can't write sdcard log file", e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
            }
        }
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (mPowerLogTag != null) {
            if (mPowerLogTag.compareTo(TEST_SEQ_BEGIN) == 0) {
                bufferLogEvent(mPowerLogTag, Build.FINGERPRINT);
            } else if (mTestResult != null) {
                bufferLogEvent(mPowerLogTag, mTestResult);
            }
        } else {
            writeLogEvents();
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
