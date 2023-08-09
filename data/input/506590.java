public class BandMode extends Activity {
    private static final String LOG_TAG = "phone";
    private static final boolean DBG = false;
    private static final int EVENT_BAND_SCAN_COMPLETED = 100;
    private static final int EVENT_BAND_SELECTION_DONE = 200;
    private static final String[] BAND_NAMES = new String[] {
            "Automatic",
            "EURO Band",
            "USA Band",
            "JAPAN Band",
            "AUS Band",
            "AUS2 Band"
    };
    private ListView mBandList;
    private ArrayAdapter mBandListAdapter;
    private BandListItem mTargetBand = null;
    private DialogInterface mProgressPanel;
    private Phone mPhone = null;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.band_mode);
        setTitle(getString(R.string.band_mode_title));
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);
        mPhone = PhoneFactory.getDefaultPhone();
        mBandList = (ListView) findViewById(R.id.band);
        mBandListAdapter = new ArrayAdapter<BandListItem>(this,
                android.R.layout.simple_list_item_1);
        mBandList.setAdapter(mBandListAdapter);
        mBandList.setOnItemClickListener(mBandSelectionHandler);
        loadBandList();
    }
    private AdapterView.OnItemClickListener mBandSelectionHandler =
            new AdapterView.OnItemClickListener () {
                public void onItemClick(AdapterView parent, View v,
                        int position, long id) {
                    getWindow().setFeatureInt(
                            Window.FEATURE_INDETERMINATE_PROGRESS,
                            Window.PROGRESS_VISIBILITY_ON);
                    mTargetBand = (BandListItem) parent.getAdapter().getItem(position);
                    if (DBG) log("Select band : " + mTargetBand.toString());
                    Message msg =
                            mHandler.obtainMessage(EVENT_BAND_SELECTION_DONE);
                    mPhone.setBandMode(mTargetBand.getBand(), msg);
                }
            };
    static private class BandListItem {
        private int mBandMode = Phone.BM_UNSPECIFIED;
        public BandListItem(int bm) {
            mBandMode = bm;
        }
        public int getBand() {
            return mBandMode;
        }
        public String toString() {
            return BAND_NAMES[mBandMode];
        }
    }
    private void loadBandList() {
        String str = getString(R.string.band_mode_loading);
        if (DBG) log(str);
        mProgressPanel = new AlertDialog.Builder(this)
            .setMessage(str)
            .show();
        Message msg = mHandler.obtainMessage(EVENT_BAND_SCAN_COMPLETED);
        mPhone.queryAvailableBandMode(msg);
    }
    private void bandListLoaded(AsyncResult result) {
        if (DBG) log("network list loaded");
        if (mProgressPanel != null) mProgressPanel.dismiss();
        clearList();
        boolean addBandSuccess = false;
        BandListItem item;
        if (result.result != null) {
            int bands[] = (int[])result.result;
            int size = bands[0];
            if (size > 0) {
                for (int i=1; i<size; i++) {
                    item = new BandListItem(bands[i]);
                    mBandListAdapter.add(item);
                    if (DBG) log("Add " + item.toString());
                }
                addBandSuccess = true;
            }
        }
        if (addBandSuccess == false) {
            if (DBG) log("Error in query, add default list");
            for (int i=0; i<Phone.BM_BOUNDARY; i++) {
                item = new BandListItem(i);
                mBandListAdapter.add(item);
                if (DBG) log("Add default " + item.toString());
            }
        }
        mBandList.requestFocus();
    }
    private void displayBandSelectionResult(Throwable ex) {
        String status = getString(R.string.band_mode_set)
                +" [" + mTargetBand.toString() + "] ";
        if (ex != null) {
            status = status + getString(R.string.band_mode_failed);
        } else {
            status = status + getString(R.string.band_mode_succeeded);
        }
        mProgressPanel = new AlertDialog.Builder(this)
            .setMessage(status)
            .setPositiveButton(android.R.string.ok, null).show();
    }
    private void clearList() {
        while(mBandListAdapter.getCount() > 0) {
            mBandListAdapter.remove(
                    mBandListAdapter.getItem(0));
        }
    }
    private void log(String msg) {
        Log.d(LOG_TAG, "[BandsList] " + msg);
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            switch (msg.what) {
                case EVENT_BAND_SCAN_COMPLETED:
                    ar = (AsyncResult) msg.obj;
                    bandListLoaded(ar);
                    break;
                case EVENT_BAND_SELECTION_DONE:
                    ar = (AsyncResult) msg.obj;
                    getWindow().setFeatureInt(
                            Window.FEATURE_INDETERMINATE_PROGRESS,
                            Window.PROGRESS_VISIBILITY_OFF);
                    displayBandSelectionResult(ar.exception);
                    break;
            }
        }
    };
}
