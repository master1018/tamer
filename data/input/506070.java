final public class Proxy {
    static final private boolean DEBUG = false;
    static final public String PROXY_CHANGE_ACTION =
        "android.intent.action.PROXY_CHANGE";
    static final public String getHost(Context ctx) {
        ContentResolver contentResolver = ctx.getContentResolver();
        Assert.assertNotNull(contentResolver);
        String host = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.HTTP_PROXY);
        if (host != null) {
            int i = host.indexOf(':');
            if (i == -1) {
                if (DEBUG) {
                    Assert.assertTrue(host.length() == 0);
                }
                return null;
            }
            return host.substring(0, i);
        }
        return getDefaultHost();
    }
    static final public int getPort(Context ctx) {
        ContentResolver contentResolver = ctx.getContentResolver();
        Assert.assertNotNull(contentResolver);
        String host = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.HTTP_PROXY);
        if (host != null) {
            int i = host.indexOf(':');
            if (i == -1) {
                if (DEBUG) {
                    Assert.assertTrue(host.length() == 0);
                }
                return -1;
            }
            if (DEBUG) {
                Assert.assertTrue(i < host.length());
            }
            return Integer.parseInt(host.substring(i+1));
        }
        return getDefaultPort();
    }
    static final public String getDefaultHost() {
        String host = SystemProperties.get("net.gprs.http-proxy");
        if (host != null) {
            Uri u = Uri.parse(host);
            host = u.getHost();
            return host;
        } else {
            return null;
        }
    }
    static final public int getDefaultPort() {
        String host = SystemProperties.get("net.gprs.http-proxy");
        if (host != null) {
            Uri u = Uri.parse(host);
            return u.getPort();
        } else {
            return -1;
        }
    }
};
