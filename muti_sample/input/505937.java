public class Credentials {
    private static final String LOGTAG = "Credentials";
    public static final String UNLOCK_ACTION = "android.credentials.UNLOCK";
    public static final String INSTALL_ACTION = "android.credentials.INSTALL";
    public static final String SYSTEM_INSTALL_ACTION = "android.credentials.SYSTEM_INSTALL";
    public static final String CA_CERTIFICATE = "CACERT_";
    public static final String USER_CERTIFICATE = "USRCERT_";
    public static final String USER_PRIVATE_KEY = "USRPKEY_";
    public static final String VPN = "VPN_";
    public static final String WIFI = "WIFI_";
    public static final String PUBLIC_KEY = "KEY";
    public static final String PRIVATE_KEY = "PKEY";
    public static final String CERTIFICATE = "CERT";
    public static final String PKCS12 = "PKCS12";
    private static Credentials singleton;
    public static Credentials getInstance() {
        if (singleton == null) {
            singleton = new Credentials();
        }
        return singleton;
    }
    public void unlock(Context context) {
        try {
            Intent intent = new Intent(UNLOCK_ACTION);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(LOGTAG, e.toString());
        }
    }
    public void install(Context context, KeyPair pair) {
        try {
            Intent intent = new Intent(INSTALL_ACTION);
            intent.putExtra(PRIVATE_KEY, pair.getPrivate().getEncoded());
            intent.putExtra(PUBLIC_KEY, pair.getPublic().getEncoded());
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(LOGTAG, e.toString());
        }
    }
    public void install(Context context, String type, byte[] value) {
        try {
            Intent intent = new Intent(INSTALL_ACTION);
            intent.putExtra(type, value);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.w(LOGTAG, e.toString());
        }
    }
    public void installFromSdCard(Context context) {
        try {
            context.startActivity(new Intent(INSTALL_ACTION));
        } catch (ActivityNotFoundException e) {
            Log.w(LOGTAG, e.toString());
        }
    }
}
