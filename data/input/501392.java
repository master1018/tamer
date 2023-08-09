public class MonkeySourceScript implements MonkeyEventSource {
    private int mEventCountInScript = 0; 
    private int mVerbose = 0;
    private double mSpeed = 1.0;
    private String mScriptFileName;
    private MonkeyEventQueue mQ;
    private static final String HEADER_COUNT = "count=";
    private static final String HEADER_SPEED = "speed=";
    private long mLastRecordedDownTimeKey = 0;
    private long mLastRecordedDownTimeMotion = 0;
    private long mLastExportDownTimeKey = 0;
    private long mLastExportDownTimeMotion = 0;
    private long mLastExportEventTime = -1;
    private long mLastRecordedEventTime = -1;
    private static final boolean THIS_DEBUG = false;
    private static final long SLEEP_COMPENSATE_DIFF = 16;
    private static final int MAX_ONE_TIME_READS = 100;
    private static final String EVENT_KEYWORD_POINTER = "DispatchPointer";
    private static final String EVENT_KEYWORD_TRACKBALL = "DispatchTrackball";
    private static final String EVENT_KEYWORD_KEY = "DispatchKey";
    private static final String EVENT_KEYWORD_FLIP = "DispatchFlip";
    private static final String EVENT_KEYWORD_KEYPRESS = "DispatchPress";
    private static final String EVENT_KEYWORD_ACTIVITY = "LaunchActivity";
    private static final String EVENT_KEYWORD_INSTRUMENTATION = "LaunchInstrumentation";
    private static final String EVENT_KEYWORD_WAIT = "UserWait";
    private static final String EVENT_KEYWORD_LONGPRESS = "LongPress";
    private static final String EVENT_KEYWORD_POWERLOG = "PowerLog";
    private static final String EVENT_KEYWORD_WRITEPOWERLOG = "WriteLog";
    private static final String STARTING_DATA_LINE = "start data >>";
    private boolean mFileOpened = false;
    private static int LONGPRESS_WAIT_TIME = 2000; 
    FileInputStream mFStream;
    DataInputStream mInputStream;
    BufferedReader mBufferedReader;
    public MonkeySourceScript(Random random, String filename, long throttle,
            boolean randomizeThrottle) {
        mScriptFileName = filename;
        mQ = new MonkeyEventQueue(random, throttle, randomizeThrottle);
    }
    private void resetValue() {
        mLastRecordedDownTimeKey = 0;
        mLastRecordedDownTimeMotion = 0;
        mLastRecordedEventTime = -1;
        mLastExportDownTimeKey = 0;
        mLastExportDownTimeMotion = 0;
        mLastExportEventTime = -1;
    }
    private boolean readHeader() throws IOException {
        mFileOpened = true;
        mFStream = new FileInputStream(mScriptFileName);
        mInputStream = new DataInputStream(mFStream);
        mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
        String line;
        while ((line = mBufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.indexOf(HEADER_COUNT) >= 0) {
                try {
                    String value = line.substring(HEADER_COUNT.length() + 1).trim();
                    mEventCountInScript = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    System.err.println(e);
                    return false;
                }
            } else if (line.indexOf(HEADER_SPEED) >= 0) {
                try {
                    String value = line.substring(HEADER_COUNT.length() + 1).trim();
                    mSpeed = Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    System.err.println(e);
                    return false;
                }
            } else if (line.indexOf(STARTING_DATA_LINE) >= 0) {
                return true;
            }
        }
        return false;
    }
    private int readLines() throws IOException {
        String line;
        for (int i = 0; i < MAX_ONE_TIME_READS; i++) {
            line = mBufferedReader.readLine();
            if (line == null) {
                return i;
            }
            line.trim();
            processLine(line);
        }
        return MAX_ONE_TIME_READS;
    }
    private void handleEvent(String s, String[] args) {
        if (s.indexOf(EVENT_KEYWORD_KEY) >= 0 && args.length == 8) {
            try {
                System.out.println(" old key\n");
                long downTime = Long.parseLong(args[0]);
                long eventTime = Long.parseLong(args[1]);
                int action = Integer.parseInt(args[2]);
                int code = Integer.parseInt(args[3]);
                int repeat = Integer.parseInt(args[4]);
                int metaState = Integer.parseInt(args[5]);
                int device = Integer.parseInt(args[6]);
                int scancode = Integer.parseInt(args[7]);
                MonkeyKeyEvent e = new MonkeyKeyEvent(downTime, eventTime, action, code, repeat,
                        metaState, device, scancode);
                System.out.println(" Key code " + code + "\n");
                mQ.addLast(e);
                System.out.println("Added key up \n");
            } catch (NumberFormatException e) {
            }
            return;
        }
        if ((s.indexOf(EVENT_KEYWORD_POINTER) >= 0 || s.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0)
                && args.length == 12) {
            try {
                long downTime = Long.parseLong(args[0]);
                long eventTime = Long.parseLong(args[1]);
                int action = Integer.parseInt(args[2]);
                float x = Float.parseFloat(args[3]);
                float y = Float.parseFloat(args[4]);
                float pressure = Float.parseFloat(args[5]);
                float size = Float.parseFloat(args[6]);
                int metaState = Integer.parseInt(args[7]);
                float xPrecision = Float.parseFloat(args[8]);
                float yPrecision = Float.parseFloat(args[9]);
                int device = Integer.parseInt(args[10]);
                int edgeFlags = Integer.parseInt(args[11]);
                int type = MonkeyEvent.EVENT_TYPE_TRACKBALL;
                if (s.indexOf("Pointer") > 0) {
                    type = MonkeyEvent.EVENT_TYPE_POINTER;
                }
                MonkeyMotionEvent e = new MonkeyMotionEvent(type, downTime, eventTime, action, x,
                        y, pressure, size, metaState, xPrecision, yPrecision, device, edgeFlags);
                mQ.addLast(e);
            } catch (NumberFormatException e) {
            }
            return;
        }
        if (s.indexOf(EVENT_KEYWORD_FLIP) >= 0 && args.length == 1) {
            boolean keyboardOpen = Boolean.parseBoolean(args[0]);
            MonkeyFlipEvent e = new MonkeyFlipEvent(keyboardOpen);
            mQ.addLast(e);
        }
        if (s.indexOf(EVENT_KEYWORD_ACTIVITY) >= 0 && args.length == 2) {
            String pkg_name = args[0];
            String cl_name = args[1];
            ComponentName mApp = new ComponentName(pkg_name, cl_name);
            MonkeyActivityEvent e = new MonkeyActivityEvent(mApp);
            mQ.addLast(e);
            return;
        }
        if (s.indexOf(EVENT_KEYWORD_INSTRUMENTATION) >= 0 && args.length == 2) {
            String test_name = args[0];
            String runner_name = args[1];
            MonkeyInstrumentationEvent e = new MonkeyInstrumentationEvent(test_name, runner_name);
            mQ.addLast(e);
            return;
        }
        if (s.indexOf(EVENT_KEYWORD_WAIT) >= 0 && args.length == 1) {
            try {
                long sleeptime = Integer.parseInt(args[0]);
                MonkeyWaitEvent e = new MonkeyWaitEvent(sleeptime);
                mQ.addLast(e);
            } catch (NumberFormatException e) {
            }
            return;
        }
        if (s.indexOf(EVENT_KEYWORD_KEYPRESS) >= 0 && args.length == 1) {
            String key_name = args[0];
            int keyCode = MonkeySourceRandom.getKeyCode(key_name);
            MonkeyKeyEvent e = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            mQ.addLast(e);
            e = new MonkeyKeyEvent(KeyEvent.ACTION_UP, keyCode);
            mQ.addLast(e);
            return;
        }
        if (s.indexOf(EVENT_KEYWORD_LONGPRESS) >= 0) {
            MonkeyKeyEvent e;
            e = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
            mQ.addLast(e);
            MonkeyWaitEvent we = new MonkeyWaitEvent(LONGPRESS_WAIT_TIME);
            mQ.addLast(we);
            e = new MonkeyKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER);
            mQ.addLast(e);
        }
        if (s.indexOf(EVENT_KEYWORD_POWERLOG) >= 0 && args.length > 0) {
            String power_log_type = args[0];
            String test_case_status;
            if (args.length == 1){
                MonkeyPowerEvent e = new MonkeyPowerEvent(power_log_type);
                mQ.addLast(e);
            } else if (args.length == 2){
                test_case_status = args[1];
                MonkeyPowerEvent e = new MonkeyPowerEvent(power_log_type, test_case_status);
                mQ.addLast(e);
            }
        }
        if (s.indexOf(EVENT_KEYWORD_WRITEPOWERLOG) >= 0) {
            MonkeyPowerEvent e = new MonkeyPowerEvent();
            mQ.addLast(e);
        }
    }
    private void processLine(String line) {
        int index1 = line.indexOf('(');
        int index2 = line.indexOf(')');
        if (index1 < 0 || index2 < 0) {
            return;
        }
        String[] args = line.substring(index1 + 1, index2).split(",");
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].trim();
        }
        handleEvent(line, args);
    }
    private void closeFile() throws IOException {
        mFileOpened = false;
        try {
            mFStream.close();
            mInputStream.close();
        } catch (NullPointerException e) {
        }
    }
    private void readNextBatch() throws IOException {
        int linesRead = 0;
        if (THIS_DEBUG) {
            System.out.println("readNextBatch(): reading next batch of events");
        }
        if (!mFileOpened) {
            resetValue();
            readHeader();
        }
        linesRead = readLines();
        if (linesRead == 0) {
            closeFile();
        }
    }
    private void needSleep(long time) {
        if (time < 1) {
            return;
        }
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }
    public boolean validate() {
        boolean validHeader;
        try {
            validHeader = readHeader();
            closeFile();
        } catch (IOException e) {
            return false;
        }
        if (mVerbose > 0) {
            System.out.println("Replaying " + mEventCountInScript + " events with speed " + mSpeed);
        }
        return validHeader;
    }
    public void setVerbose(int verbose) {
        mVerbose = verbose;
    }
    private void adjustKeyEventTime(MonkeyKeyEvent e) {
        if (e.getEventTime() < 0) {
            return;
        }
        long thisDownTime = 0;
        long thisEventTime = 0;
        long expectedDelay = 0;
        if (mLastRecordedEventTime <= 0) {
            thisDownTime = SystemClock.uptimeMillis();
            thisEventTime = thisDownTime;
        } else {
            if (e.getDownTime() != mLastRecordedDownTimeKey) {
                thisDownTime = e.getDownTime();
            } else {
                thisDownTime = mLastExportDownTimeKey;
            }
            expectedDelay = (long) ((e.getEventTime() - mLastRecordedEventTime) * mSpeed);
            thisEventTime = mLastExportEventTime + expectedDelay;
            needSleep(expectedDelay - SLEEP_COMPENSATE_DIFF);
        }
        mLastRecordedDownTimeKey = e.getDownTime();
        mLastRecordedEventTime = e.getEventTime();
        e.setDownTime(thisDownTime);
        e.setEventTime(thisEventTime);
        mLastExportDownTimeKey = thisDownTime;
        mLastExportEventTime = thisEventTime;
    }
    private void adjustMotionEventTime(MonkeyMotionEvent e) {
        if (e.getEventTime() < 0) {
            return;
        }
        long thisDownTime = 0;
        long thisEventTime = 0;
        long expectedDelay = 0;
        if (mLastRecordedEventTime <= 0) {
            thisDownTime = SystemClock.uptimeMillis();
            thisEventTime = thisDownTime;
        } else {
            if (e.getDownTime() != mLastRecordedDownTimeMotion) {
                thisDownTime = e.getDownTime();
            } else {
                thisDownTime = mLastExportDownTimeMotion;
            }
            expectedDelay = (long) ((e.getEventTime() - mLastRecordedEventTime) * mSpeed);
            thisEventTime = mLastExportEventTime + expectedDelay;
            needSleep(expectedDelay - SLEEP_COMPENSATE_DIFF);
        }
        mLastRecordedDownTimeMotion = e.getDownTime();
        mLastRecordedEventTime = e.getEventTime();
        e.setDownTime(thisDownTime);
        e.setEventTime(thisEventTime);
        mLastExportDownTimeMotion = thisDownTime;
        mLastExportEventTime = thisEventTime;
    }
    public MonkeyEvent getNextEvent() {
        long recordedEventTime = -1;
        MonkeyEvent ev;
        if (mQ.isEmpty()) {
            try {
                readNextBatch();
            } catch (IOException e) {
                return null;
            }
        }
        try {
            ev = mQ.getFirst();
            mQ.removeFirst();
        } catch (NoSuchElementException e) {
            return null;
        }
        if (ev.getEventType() == MonkeyEvent.EVENT_TYPE_KEY) {
            adjustKeyEventTime((MonkeyKeyEvent) ev);
        } else if (ev.getEventType() == MonkeyEvent.EVENT_TYPE_POINTER
                || ev.getEventType() == MonkeyEvent.EVENT_TYPE_TRACKBALL) {
            adjustMotionEventTime((MonkeyMotionEvent) ev);
        }
        return ev;
    }
}
