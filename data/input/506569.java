public class VendorPolicyLoader {
    private static final String POLICY_PACKAGE = "com.android.email.policy";
    private static final String POLICY_CLASS = POLICY_PACKAGE + ".EmailPolicy";
    private static final String GET_POLICY_METHOD = "getPolicy";
    private static final Class<?>[] ARGS = new Class<?>[] {String.class, Bundle.class};
    private static final String USE_ALTERNATE_EXCHANGE_STRINGS = "useAlternateExchangeStrings";
    private static final String GET_IMAP_ID = "getImapId";
    private static final String GET_IMAP_ID_USER = "getImapId.user";
    private static final String GET_IMAP_ID_HOST = "getImapId.host";
    private static final String GET_IMAP_ID_CAPA = "getImapId.capabilities";
    private static final String FIND_PROVIDER = "findProvider";
    private static final String FIND_PROVIDER_IN_URI = "findProvider.inUri";
    private static final String FIND_PROVIDER_IN_USER = "findProvider.inUser";
    private static final String FIND_PROVIDER_OUT_URI = "findProvider.outUri";
    private static final String FIND_PROVIDER_OUT_USER = "findProvider.outUser";
    private static final String FIND_PROVIDER_NOTE = "findProvider.note";
    private static VendorPolicyLoader sInstance;
    private final Method mPolicyMethod;
    public static VendorPolicyLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new VendorPolicyLoader(context);
        }
        return sInstance;
    }
    private VendorPolicyLoader(Context context) {
        this(context, POLICY_PACKAGE, POLICY_CLASS, false);
    }
     VendorPolicyLoader(Context context, String packageName, String className,
            boolean allowNonSystemApk) {
        if (!allowNonSystemApk && !isSystemPackage(context, packageName)) {
            mPolicyMethod = null;
            return;
        }
        Class<?> clazz = null;
        Method method = null;
        try {
            final Context policyContext = context.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            final ClassLoader classLoader = policyContext.getClassLoader();
            clazz = classLoader.loadClass(className);
            method = clazz.getMethod(GET_POLICY_METHOD, ARGS);
        } catch (NameNotFoundException ignore) {
        } catch (ClassNotFoundException e) {
            Log.w(Email.LOG_TAG, "VendorPolicyLoader: " + e);
        } catch (NoSuchMethodException e) {
            Log.w(Email.LOG_TAG, "VendorPolicyLoader: " + e);
        }
        mPolicyMethod = method;
    }
     static boolean isSystemPackage(Context context, String packageName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NameNotFoundException e) {
            return false; 
        }
    }
     Bundle getPolicy(String policy, Bundle args) {
        Bundle ret = null;
        if (mPolicyMethod != null) {
            try {
                ret = (Bundle) mPolicyMethod.invoke(null, policy, args);
            } catch (Exception e) {
                Log.w(Email.LOG_TAG, "VendorPolicyLoader", e);
            }
        }
        return (ret != null) ? ret : Bundle.EMPTY;
    }
    public boolean useAlternateExchangeStrings() {
        return getPolicy(USE_ALTERNATE_EXCHANGE_STRINGS, null)
                .getBoolean(USE_ALTERNATE_EXCHANGE_STRINGS, false);
    }
    public String getImapIdValues(String userName, String host, String capabilities) {
        Bundle params = new Bundle();
        params.putString(GET_IMAP_ID_USER, userName);
        params.putString(GET_IMAP_ID_HOST, host);
        params.putString(GET_IMAP_ID_CAPA, capabilities);
        String result = getPolicy(GET_IMAP_ID, params).getString(GET_IMAP_ID);
        return result;
    }
    public Provider findProviderForDomain(String domain) {
        Bundle params = new Bundle();
        params.putString(FIND_PROVIDER, domain);
        Bundle out = getPolicy(FIND_PROVIDER, params);
        if (out != null && !out.isEmpty()) {
            try {
                Provider p = new Provider();
                p.id = null;
                p.label = null;
                p.domain = domain;
                p.incomingUriTemplate = new URI(out.getString(FIND_PROVIDER_IN_URI));
                p.incomingUsernameTemplate = out.getString(FIND_PROVIDER_IN_USER);
                p.outgoingUriTemplate = new URI(out.getString(FIND_PROVIDER_OUT_URI));
                p.outgoingUsernameTemplate = out.getString(FIND_PROVIDER_OUT_USER);
                p.note = out.getString(FIND_PROVIDER_NOTE);
                return p;
            } catch (URISyntaxException e) {
                Log.d(Email.LOG_TAG, "uri exception while vendor policy loads " + domain);
            }
        }
        return null;
    }
}
