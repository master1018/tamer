public class ForwardService {
    private ForwardServer fs8000, fs8080, fs8443;
    private static ForwardService inst;
    private static final String LOGTAG = "ForwardService";
    private static final String DEFAULT_TEST_HOST = "android-browser-test.mtv.corp.google.com";
    private static final String FORWARD_HOST_CONF = "/sdcard/drt_forward_host.txt";
    private ForwardService() {
        int addr = getForwardHostAddr();
        if (addr != -1) {
            fs8000 = new ForwardServer(8000, addr, 8000);
            fs8080 = new ForwardServer(8080, addr, 8080);
            fs8443 = new ForwardServer(8443, addr, 8443);
        }
    }
    public static ForwardService getForwardService() {
        if (inst == null) {
            inst = new ForwardService();
        }
        return inst;
    }
    public void startForwardService() {
        try {
            if (fs8000 != null)
                fs8000.start();
            if (fs8080 != null)
                fs8080.start();
            if (fs8443 != null)
                fs8443.start();
        } catch (IOException ioe) {
            Log.w(LOGTAG, "failed to start forwarder. http tests will fail.", ioe);
            return;
        }
    }
    public void stopForwardService() {
        if (fs8000 != null) {
            fs8000.stop();
            fs8000 = null;
        }
        if (fs8080 != null) {
            fs8080.stop();
            fs8080 = null;
        }
        if (fs8443 != null) {
            fs8443.stop();
            fs8443 = null;
        }
        Log.v(LOGTAG, "forwarders stopped.");
    }
    private static int getForwardHostAddr() {
        int addr = -1;
        String host = null;
        File forwardHostConf = new File(FORWARD_HOST_CONF);
        if (forwardHostConf.isFile()) {
            BufferedReader hostReader = null;
            try {
                hostReader = new BufferedReader(new FileReader(forwardHostConf));
                host = hostReader.readLine();
                Log.v(LOGTAG, "read forward host from file: " + host);
            } catch (IOException ioe) {
                Log.v(LOGTAG, "cannot read forward host from file", ioe);
            } finally {
                if (hostReader != null) {
                    try {
                        hostReader.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }
        if (host == null || host.length() == 0)
            host = DEFAULT_TEST_HOST;
        try {
            addr = AdbUtils.resolve(host);
        } catch (IOException ioe) {
            Log.e(LOGTAG, "failed to resolve server address", ioe);
        }
        return addr;
    }
}
