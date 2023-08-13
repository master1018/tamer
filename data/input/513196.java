public class ProgressShower implements VCardEntryHandler {
    public static final String LOG_TAG = "vcard.ProgressShower"; 
    private final Context mContext;
    private final Handler mHandler;
    private final ProgressDialog mProgressDialog;
    private final String mProgressMessage;
    private long mTime;
    private class ShowProgressRunnable implements Runnable {
        private VCardEntry mContact;
        public ShowProgressRunnable(VCardEntry contact) {
            mContact = contact;
        }
        public void run() {
            mProgressDialog.setMessage( mProgressMessage + "\n" + 
                    mContact.getDisplayName());
            mProgressDialog.incrementProgressBy(1);
        }
    }
    public ProgressShower(ProgressDialog progressDialog,
            String progressMessage,
            Context context,
            Handler handler) {
        mContext = context;
        mHandler = handler;
        mProgressDialog = progressDialog;
        mProgressMessage = progressMessage;
    }
    public void onStart() {
    }
    public void onEntryCreated(VCardEntry contactStruct) {
        long start = System.currentTimeMillis();
        if (!contactStruct.isIgnorable()) {
            if (mProgressDialog != null && mProgressMessage != null) {
                if (mHandler != null) {
                    mHandler.post(new ShowProgressRunnable(contactStruct));
                } else {
                    mProgressDialog.setMessage(mContext.getString(R.string.progress_shower_message,
                            mProgressMessage, 
                            contactStruct.getDisplayName()));
                }
            }
        }
        mTime += System.currentTimeMillis() - start;
    }
    public void onEnd() {
        if (VCardConfig.showPerformanceLog()) {
            Log.d(LOG_TAG,
                    String.format("Time to progress a dialog: %d ms", mTime));
        }
    }
}
