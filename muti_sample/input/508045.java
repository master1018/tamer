public class BluetoothOppShareInfo {
    public int mId;
    public String mUri;
    public String mHint;
    public String mFilename;
    public String mMimetype;
    public int mDirection;
    public String mDestination;
    public int mVisibility;
    public int mConfirm;
    public int mStatus;
    public int mTotalBytes;
    public int mCurrentBytes;
    public long mTimestamp;
    public boolean mMediaScanned;
    public BluetoothOppShareInfo(int id, String uri, String hint, String filename, String mimetype,
            int direction, String destination, int visibility, int confirm, int status,
            int totalBytes, int currentBytes, int timestamp, boolean mediaScanned) {
        mId = id;
        mUri = uri;
        mHint = hint;
        mFilename = filename;
        mMimetype = mimetype;
        mDirection = direction;
        mDestination = destination;
        mVisibility = visibility;
        mConfirm = confirm;
        mStatus = status;
        mTotalBytes = totalBytes;
        mCurrentBytes = currentBytes;
        mTimestamp = timestamp;
        mMediaScanned = mediaScanned;
    }
    public boolean isReadyToStart() {
        if (mDirection == BluetoothShare.DIRECTION_OUTBOUND) {
            if (mStatus == BluetoothShare.STATUS_PENDING && mUri != null) {
                return true;
            }
        } else if (mDirection == BluetoothShare.DIRECTION_INBOUND) {
            if (mStatus == BluetoothShare.STATUS_PENDING) {
                return true;
            }
        }
        return false;
    }
    public boolean hasCompletionNotification() {
        if (!BluetoothShare.isStatusCompleted(mStatus)) {
            return false;
        }
        if (mVisibility == BluetoothShare.VISIBILITY_VISIBLE) {
            return true;
        }
        return false;
    }
    public boolean isObsolete() {
        if (BluetoothShare.STATUS_RUNNING == mStatus) {
            return true;
        }
        return false;
    }
}
