public class VpnServiceBinder extends Service {
    private static final String TAG = VpnServiceBinder.class.getSimpleName();
    private static final boolean DBG = true;
    private static final String STATES_FILE_RELATIVE_PATH = "/misc/vpn/.states";
    private VpnService<? extends VpnProfile> mService;
    private static String getStateFilePath() {
	return Environment.getDataDirectory().getPath() + STATES_FILE_RELATIVE_PATH;
    }
    private final IBinder mBinder = new IVpnService.Stub() {
        public boolean connect(VpnProfile p, String username, String password) {
            return VpnServiceBinder.this.connect(p, username, password);
        }
        public void disconnect() {
            VpnServiceBinder.this.disconnect();
        }
        public void checkStatus(VpnProfile p) {
            VpnServiceBinder.this.checkStatus(p);
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        checkSavedStates();
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    void saveStates() throws IOException {
        if (DBG) Log.d("VpnServiceBinder", "     saving states");
        ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(getStateFilePath()));
        oos.writeObject(mService);
        oos.close();
    }
    void removeStates() {
        try {
            File f = new File(getStateFilePath());
            if (f.exists()) f.delete();
        } catch (Throwable e) {
            if (DBG) Log.d("VpnServiceBinder", "     remove states: " + e);
        }
    }
    private synchronized boolean connect(final VpnProfile p,
            final String username, final String password) {
        if (mService != null) return false;
        final VpnService s = mService = createService(p);
        new Thread(new Runnable() {
            public void run() {
                s.onConnect(username, password);
            }
        }).start();
        return true;
    }
    private synchronized void disconnect() {
        if (mService == null) return;
        final VpnService s = mService;
        new Thread(new Runnable() {
            public void run() {
                s.onDisconnect();
            }
        }).start();
    }
    private synchronized void checkStatus(VpnProfile p) {
        if ((mService == null)
                || (!p.getName().equals(mService.mProfile.getName()))) {
            broadcastConnectivity(p.getName(), VpnState.IDLE);
        } else {
            broadcastConnectivity(p.getName(), mService.getState());
        }
    }
    private void checkSavedStates() {
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
                    getStateFilePath()));
            mService = (VpnService<? extends VpnProfile>) ois.readObject();
            mService.recover(this);
            ois.close();
        } catch (FileNotFoundException e) {
        } catch (Throwable e) {
            Log.i("VpnServiceBinder", "recovery error, remove states: " + e);
            removeStates();
        }
    }
    private VpnService<? extends VpnProfile> createService(VpnProfile p) {
        switch (p.getType()) {
            case L2TP:
                L2tpService l2tp = new L2tpService();
                l2tp.setContext(this, (L2tpProfile) p);
                return l2tp;
            case PPTP:
                PptpService pptp = new PptpService();
                pptp.setContext(this, (PptpProfile) p);
                return pptp;
            case L2TP_IPSEC_PSK:
                L2tpIpsecPskService psk = new L2tpIpsecPskService();
                psk.setContext(this, (L2tpIpsecPskProfile) p);
                return psk;
            case L2TP_IPSEC:
                L2tpIpsecService l2tpIpsec = new L2tpIpsecService();
                l2tpIpsec.setContext(this, (L2tpIpsecProfile) p);
                return l2tpIpsec;
            default:
                return null;
        }
    }
    private void broadcastConnectivity(String name, VpnState s) {
        new VpnManager(this).broadcastConnectivity(name, s);
    }
}
