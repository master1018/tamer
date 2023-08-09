public class NetworkSetting extends PreferenceActivity
        implements DialogInterface.OnCancelListener {
    private static final String LOG_TAG = "phone";
    private static final boolean DBG = false;
    private static final int EVENT_NETWORK_SCAN_COMPLETED = 100;
    private static final int EVENT_NETWORK_SELECTION_DONE = 200;
    private static final int EVENT_AUTO_SELECT_DONE = 300;
    private static final int DIALOG_NETWORK_SELECTION = 100;
    private static final int DIALOG_NETWORK_LIST_LOAD = 200;
    private static final int DIALOG_NETWORK_AUTO_SELECT = 300;
    private static final String LIST_NETWORKS_KEY = "list_networks_key";
    private static final String BUTTON_SRCH_NETWRKS_KEY = "button_srch_netwrks_key";
    private static final String BUTTON_AUTO_SELECT_KEY = "button_auto_select_key";
    private HashMap<Preference, NetworkInfo> mNetworkMap;
    Phone mPhone;
    protected boolean mIsForeground = false;
    String mNetworkSelectMsg;
    private PreferenceGroup mNetworkList;
    private Preference mSearchButton;
    private Preference mAutoSelect;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_NETWORK_SCAN_COMPLETED:
                    networksListLoaded ((List<NetworkInfo>) msg.obj, msg.arg1);
                    break;
                case EVENT_NETWORK_SELECTION_DONE:
                    if (DBG) log("hideProgressPanel");
                    removeDialog(DIALOG_NETWORK_SELECTION);
                    getPreferenceScreen().setEnabled(true);
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        if (DBG) log("manual network selection: failed!");
                        displayNetworkSelectionFailed(ar.exception);
                    } else {
                        if (DBG) log("manual network selection: succeeded!");
                        displayNetworkSelectionSucceeded();
                    }
                    break;
                case EVENT_AUTO_SELECT_DONE:
                    if (DBG) log("hideProgressPanel");
                    if (mIsForeground) {
                        dismissDialog(DIALOG_NETWORK_AUTO_SELECT);
                    }
                    getPreferenceScreen().setEnabled(true);
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        if (DBG) log("automatic network selection: failed!");
                        displayNetworkSelectionFailed(ar.exception);
                    } else {
                        if (DBG) log("automatic network selection: succeeded!");
                        displayNetworkSelectionSucceeded();
                    }
                    break;
            }
            return;
        }
    };
    private INetworkQueryService mNetworkQueryService = null;
    private final ServiceConnection mNetworkQueryServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (DBG) log("connection created, binding local service.");
            mNetworkQueryService = ((NetworkQueryService.LocalBinder) service).getService();
            loadNetworksList();
        }
        public void onServiceDisconnected(ComponentName className) {
            if (DBG) log("connection disconnected, cleaning local binding.");
            mNetworkQueryService = null;
        }
    };
    private final INetworkQueryServiceCallback mCallback = new INetworkQueryServiceCallback.Stub() {
        public void onQueryComplete(List<NetworkInfo> networkInfoArray, int status) {
            if (DBG) log("notifying message loop of query completion.");
            Message msg = mHandler.obtainMessage(EVENT_NETWORK_SCAN_COMPLETED,
                    status, 0, networkInfoArray);
            msg.sendToTarget();
        }
    };
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean handled = false;
        if (preference == mSearchButton) {
            loadNetworksList();
            handled = true;
        } else if (preference == mAutoSelect) {
            selectNetworkAutomatic();
            handled = true;
        } else {
            Preference selectedCarrier = preference;
            String networkStr = selectedCarrier.getTitle().toString();
            if (DBG) log("selected network: " + networkStr);
            Message msg = mHandler.obtainMessage(EVENT_NETWORK_SELECTION_DONE);
            mPhone.selectNetworkManually(mNetworkMap.get(selectedCarrier), msg);
            displayNetworkSeletionInProgress(networkStr);
            handled = true;
        }
        return handled;
    }
    public void onCancel(DialogInterface dialog) {
        try {
            mNetworkQueryService.stopNetworkQuery(mCallback);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        finish();
    }
    public String getNormalizedCarrierName(NetworkInfo ni) {
        if (ni != null) {
            return ni.getOperatorAlphaLong() + " (" + ni.getOperatorNumeric() + ")";
        }
        return null;
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.carrier_select);
        mPhone = PhoneApp.getInstance().phone;
        mNetworkList = (PreferenceGroup) getPreferenceScreen().findPreference(LIST_NETWORKS_KEY);
        mNetworkMap = new HashMap<Preference, NetworkInfo>();
        mSearchButton = getPreferenceScreen().findPreference(BUTTON_SRCH_NETWRKS_KEY);
        mAutoSelect = getPreferenceScreen().findPreference(BUTTON_AUTO_SELECT_KEY);
        startService (new Intent(this, NetworkQueryService.class));
        bindService (new Intent(this, NetworkQueryService.class), mNetworkQueryServiceConnection,
                Context.BIND_AUTO_CREATE);
    }
    @Override
    public void onResume() {
        super.onResume();
        mIsForeground = true;
    }
    @Override
    public void onPause() {
        super.onPause();
        mIsForeground = false;
    }
    @Override
    protected void onDestroy() {
        unbindService(mNetworkQueryServiceConnection);
        super.onDestroy();
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        if ((id == DIALOG_NETWORK_SELECTION) || (id == DIALOG_NETWORK_LIST_LOAD) ||
                (id == DIALOG_NETWORK_AUTO_SELECT)) {
            ProgressDialog dialog = new ProgressDialog(this);
            switch (id) {
                case DIALOG_NETWORK_SELECTION:
                    dialog.setMessage(mNetworkSelectMsg);
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    break;
                case DIALOG_NETWORK_AUTO_SELECT:
                    dialog.setMessage(getResources().getString(R.string.register_automatically));
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    break;
                case DIALOG_NETWORK_LIST_LOAD:
                default:
                    dialog.setMessage(getResources().getString(R.string.load_networks_progress));
                    dialog.setCancelable(true);
                    dialog.setOnCancelListener(this);
                    break;
            }
            return dialog;
        }
        return null;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if ((id == DIALOG_NETWORK_SELECTION) || (id == DIALOG_NETWORK_LIST_LOAD) ||
                (id == DIALOG_NETWORK_AUTO_SELECT)) {
            getPreferenceScreen().setEnabled(false);
        }
    }
    private void displayEmptyNetworkList(boolean flag) {
        mNetworkList.setTitle(flag ? R.string.empty_networks_list : R.string.label_available);
    }
    private void displayNetworkSeletionInProgress(String networkStr) {
        mNetworkSelectMsg = getResources().getString(R.string.register_on_network, networkStr);
        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_SELECTION);
        }
    }
    private void displayNetworkQueryFailed(int error) {
        String status = getResources().getString(R.string.network_query_error);
        NotificationMgr.getDefault().postTransientNotification(
                        NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);
    }
    private void displayNetworkSelectionFailed(Throwable ex) {
        String status;
        if ((ex != null && ex instanceof CommandException) &&
                ((CommandException)ex).getCommandError()
                  == CommandException.Error.ILLEGAL_SIM_OR_ME)
        {
            status = getResources().getString(R.string.not_allowed);
        } else {
            status = getResources().getString(R.string.connect_later);
        }
        NotificationMgr.getDefault().postTransientNotification(
                        NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);
    }
    private void displayNetworkSelectionSucceeded() {
        String status = getResources().getString(R.string.registration_done);
        NotificationMgr.getDefault().postTransientNotification(
                        NotificationMgr.NETWORK_SELECTION_NOTIFICATION, status);
        mHandler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 3000);
    }
    private void loadNetworksList() {
        if (DBG) log("load networks list...");
        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_LIST_LOAD);
        }
        try {
            mNetworkQueryService.startNetworkQuery(mCallback);
        } catch (RemoteException e) {
        }
        displayEmptyNetworkList(false);
    }
    private void networksListLoaded(List<NetworkInfo> result, int status) {
        if (DBG) log("networks list loaded");
        if (DBG) log("hideProgressPanel");
        if (mIsForeground) {
            dismissDialog(DIALOG_NETWORK_LIST_LOAD);
        }
        getPreferenceScreen().setEnabled(true);
        clearList();
        if (status != NetworkQueryService.QUERY_OK) {
            if (DBG) log("error while querying available networks");
            displayNetworkQueryFailed(status);
            displayEmptyNetworkList(true);
        } else {
            if (result != null){
                displayEmptyNetworkList(false);
                for (NetworkInfo ni : result) {
                    Preference carrier = new Preference(this, null);
                    carrier.setTitle(ni.getOperatorAlphaLong());
                    carrier.setPersistent(false);
                    mNetworkList.addPreference(carrier);
                    mNetworkMap.put(carrier, ni);
                    if (DBG) log("  " + ni);
                }
            } else {
                displayEmptyNetworkList(true);
            }
        }
    }
    private void clearList() {
        for (Preference p : mNetworkMap.keySet()) {
            mNetworkList.removePreference(p);
        }
        mNetworkMap.clear();
    }
    private void selectNetworkAutomatic() {
        if (DBG) log("select network automatically...");
        if (mIsForeground) {
            showDialog(DIALOG_NETWORK_AUTO_SELECT);
        }
        Message msg = mHandler.obtainMessage(EVENT_AUTO_SELECT_DONE);
        mPhone.setNetworkSelectionModeAutomatic(msg);
    }
    private void log(String msg) {
        Log.d(LOG_TAG, "[NetworksList] " + msg);
    }
}
