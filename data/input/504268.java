public class SignatureTestActivity extends Activity {
    static final String BUNDLE_EXTRA_SIG_TEST = "sigtest";
    static final String BUNDLE_KEY_RESULT = "result";
    static final String BUNDLE_KEY_MISSING_CLASS = "missing_class";
    static final String BUNDLE_KEY_MISSING_INTERFACE = "missing_interface";
    static final String BUNDLE_KEY_MISSING_METHOD = "missing_method";
    static final String BUNDLE_KEY_MISSING_FIELD = "missing_field";
    static final String BUNDLE_KEY_MISMATCH_CLASS = "mismatch_class_signature";
    static final String BUNDLE_KEY_MISMATCH_INTERFACE = "mismatch_interface_signature";
    static final String BUNDLE_KEY_MISMATCH_METHOD = "mismatch_method_signature";
    static final String BUNDLE_KEY_MISMATCH_FIELD = "mismatch_field_signature";
    static final String BUNDLE_KEY_CAUGHT_EXCEPTION = "caught_exception";
    static final int GET_SIG_TEST_RESULT_TRANSACTION = 101;
    private DeviceResultObserver mResultObserver;
    public static enum FAILURE_TYPE {
        MISSING_CLASS,
        MISSING_INTERFACE,
        MISSING_METHOD,
        MISSING_FIELD,
        MISMATCH_CLASS,
        MISMATCH_INTERFACE,
        MISMATCH_METHOD,
        MISMATCH_FIELD,
        CAUGHT_EXCEPTION,
    }
    static final HashMap<FAILURE_TYPE, String> FAILURE_TYPE_TO_KEY =
            new HashMap<FAILURE_TYPE, String>();
    static {
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISSING_CLASS, BUNDLE_KEY_MISSING_CLASS);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISSING_INTERFACE, BUNDLE_KEY_MISSING_INTERFACE);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISSING_METHOD, BUNDLE_KEY_MISSING_METHOD);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISSING_FIELD, BUNDLE_KEY_MISSING_FIELD);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISMATCH_CLASS, BUNDLE_KEY_MISMATCH_CLASS);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISMATCH_INTERFACE, BUNDLE_KEY_MISMATCH_INTERFACE);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISMATCH_METHOD, BUNDLE_KEY_MISMATCH_METHOD);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.MISMATCH_FIELD, BUNDLE_KEY_MISMATCH_FIELD);
        FAILURE_TYPE_TO_KEY.put(FAILURE_TYPE.CAUGHT_EXCEPTION, BUNDLE_KEY_CAUGHT_EXCEPTION);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResultObserver = new DeviceResultObserver();
        start();
        mResultObserver.sendResult(getIntent());
    }
    @SuppressWarnings("unchecked")
    private void start() {
        SignatureTest sigTest = new SignatureTest(mResultObserver);
        HashSet<String> excludeSet = getExcludedSet();
        Resources r = getResources();
        Class rClass = R.xml.class;
        Field[]fs = rClass.getFields();
        for (Field f : fs) {
            if (!excludeSet.contains(f.getName())) {
                try {
                    sigTest.start(r.getXml(f.getInt(rClass)));
                } catch (Exception e) {
                    mResultObserver.notifyFailure(FAILURE_TYPE.CAUGHT_EXCEPTION, e.getMessage(),
                            e.getMessage());
                }
            }
        }
    }
    private HashSet<String> getExcludedSet() {
        HashSet<String> excludeSet = new HashSet<String>();
        Resources r = getResources();
        InputStream excludepackage = r.openRawResource(R.raw.excludepackages);
        BufferedReader reader = new BufferedReader(new InputStreamReader(excludepackage));
        try {
            String p = null;
            while (true) {
                p = reader.readLine();
                if (p == null || p.equals("")) {
                    break;
                }
                excludeSet.add(p);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return excludeSet;
    }
    Bundle mBundle;
    final class DeviceResultObserver implements ResultObserver {
        DeviceResultObserver() {
            mBundle = new Bundle();
            mBundle.putStringArrayList(BUNDLE_KEY_MISSING_FIELD, new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISSING_METHOD, new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISMATCH_CLASS,
                    new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISMATCH_FIELD,
                    new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISMATCH_METHOD,
                    new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISSING_CLASS, new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISSING_INTERFACE,
                    new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_MISMATCH_INTERFACE,
                    new ArrayList<String>());
            mBundle.putStringArrayList(BUNDLE_KEY_CAUGHT_EXCEPTION,
                    new ArrayList<String>());
        }
        @SuppressWarnings("deprecation")
        public void sendResult(Intent i) {
            SignatureTestLog.d("Send result");
            if ((mBundle.getStringArrayList(BUNDLE_KEY_MISSING_FIELD).size() == 0)
                    && (mBundle.getStringArrayList(BUNDLE_KEY_MISSING_CLASS).size() == 0)
                    && (mBundle.getStringArrayList(BUNDLE_KEY_MISSING_METHOD).size() == 0)
                    && (mBundle.getStringArrayList(BUNDLE_KEY_MISSING_INTERFACE).size() == 0)
                    && (mBundle.getStringArrayList(
                            BUNDLE_KEY_MISMATCH_CLASS).size() == 0)
                    && (mBundle.getStringArrayList(
                            BUNDLE_KEY_MISMATCH_FIELD).size() == 0)
                    && (mBundle.getStringArrayList(
                            BUNDLE_KEY_MISMATCH_INTERFACE).size() == 0)
                    && (mBundle.getStringArrayList(
                            BUNDLE_KEY_MISMATCH_METHOD).size() == 0)
                    && (mBundle.getStringArrayList(
                            BUNDLE_KEY_CAUGHT_EXCEPTION).size() == 0)) {
                SignatureTestLog.d("PASS");
                mBundle.putBoolean(BUNDLE_KEY_RESULT, true);
            } else {
                SignatureTestLog.d("FAIL: " + mBundle.size());
                mBundle.putBoolean(BUNDLE_KEY_RESULT, false);
            }
        }
        public void notifyFailure(FAILURE_TYPE type,
                                  String name,
                                  String errorMessage) {
            SignatureTestLog.d("Failure: ");
            SignatureTestLog.d("   Type: " + type);
            SignatureTestLog.d("   Name: " + name);
            SignatureTestLog.d("   Why : " + errorMessage);
            mBundle.getStringArrayList(SignatureTestActivity.FAILURE_TYPE_TO_KEY.get(type))
                    .add(name);
        }
    }
}
