public class BluetoothOppBatch {
    private static final String TAG = "BtOppBatch";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    public int mId;
    public int mStatus;
    public final long mTimestamp;
    public final int mDirection;
    public final BluetoothDevice mDestination;
    private BluetoothOppBatchListener mListener;
    private final ArrayList<BluetoothOppShareInfo> mShares;
    private final Context mContext;
    public interface BluetoothOppBatchListener {
        public void onShareAdded(int id);
        public void onShareDeleted(int id);
        public void onBatchCanceled();
    }
    public BluetoothOppBatch(Context context, BluetoothOppShareInfo info) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mShares = Lists.newArrayList();
        mTimestamp = info.mTimestamp;
        mDirection = info.mDirection;
        mDestination = adapter.getRemoteDevice(info.mDestination);
        mStatus = Constants.BATCH_STATUS_PENDING;
        mShares.add(info);
        if (V) Log.v(TAG, "New Batch created for info " + info.mId);
    }
    public void addShare(BluetoothOppShareInfo info) {
        mShares.add(info);
        if (mListener != null) {
            mListener.onShareAdded(info.mId);
        }
    }
    public void deleteShare(BluetoothOppShareInfo info) {
        if (info.mStatus == BluetoothShare.STATUS_RUNNING) {
            info.mStatus = BluetoothShare.STATUS_CANCELED;
            if (info.mDirection == BluetoothShare.DIRECTION_INBOUND && info.mFilename != null) {
                new File(info.mFilename).delete();
            }
        }
        if (mListener != null) {
            mListener.onShareDeleted(info.mId);
        }
    }
    public void cancelBatch() {
        if (V) Log.v(TAG, "batch " + this.mId + " is canceled");
        if (mListener != null) {
            mListener.onBatchCanceled();
        }
        for (int i = mShares.size() - 1; i >= 0; i--) {
            BluetoothOppShareInfo info = mShares.get(i);
            if (info.mStatus < 200) {
                if (info.mDirection == BluetoothShare.DIRECTION_INBOUND && info.mFilename != null) {
                    new File(info.mFilename).delete();
                }
                if (V) Log.v(TAG, "Cancel batch for info " + info.mId);
                Constants.updateShareStatus(mContext, info.mId, BluetoothShare.STATUS_CANCELED);
            }
        }
        mShares.clear();
    }
    public boolean hasShare(BluetoothOppShareInfo info) {
        return mShares.contains(info);
    }
    public boolean isEmpty() {
        return (mShares.size() == 0);
    }
    public void registerListern(BluetoothOppBatchListener listener) {
        mListener = listener;
    }
    public BluetoothOppShareInfo getPendingShare() {
        for (int i = 0; i < mShares.size(); i++) {
            BluetoothOppShareInfo share = mShares.get(i);
            if (share.mStatus == BluetoothShare.STATUS_PENDING) {
                return share;
            }
        }
        return null;
    }
}
