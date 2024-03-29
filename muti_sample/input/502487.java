public class SyncStats implements Parcelable {
    public long numAuthExceptions;
    public long numIoExceptions;
    public long numParseExceptions;
    public long numConflictDetectedExceptions;
    public long numInserts;
    public long numUpdates;
    public long numDeletes;
    public long numEntries;
    public long numSkippedEntries;
    public SyncStats() {
        numAuthExceptions = 0;
        numIoExceptions = 0;
        numParseExceptions = 0;
        numConflictDetectedExceptions = 0;
        numInserts = 0;
        numUpdates = 0;
        numDeletes = 0;
        numEntries = 0;
        numSkippedEntries = 0;
    }
    public SyncStats(Parcel in) {
        numAuthExceptions = in.readLong();
        numIoExceptions = in.readLong();
        numParseExceptions = in.readLong();
        numConflictDetectedExceptions = in.readLong();
        numInserts = in.readLong();
        numUpdates = in.readLong();
        numDeletes = in.readLong();
        numEntries = in.readLong();
        numSkippedEntries = in.readLong();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" stats [");
        if (numAuthExceptions > 0) sb.append(" numAuthExceptions: ").append(numAuthExceptions);
        if (numIoExceptions > 0) sb.append(" numIoExceptions: ").append(numIoExceptions);
        if (numParseExceptions > 0) sb.append(" numParseExceptions: ").append(numParseExceptions);
        if (numConflictDetectedExceptions > 0)
            sb.append(" numConflictDetectedExceptions: ").append(numConflictDetectedExceptions);
        if (numInserts > 0) sb.append(" numInserts: ").append(numInserts);
        if (numUpdates > 0) sb.append(" numUpdates: ").append(numUpdates);
        if (numDeletes > 0) sb.append(" numDeletes: ").append(numDeletes);
        if (numEntries > 0) sb.append(" numEntries: ").append(numEntries);
        if (numSkippedEntries > 0) sb.append(" numSkippedEntries: ").append(numSkippedEntries);
        sb.append("]");
        return sb.toString();
    }
    public void clear() {
        numAuthExceptions = 0;
        numIoExceptions = 0;
        numParseExceptions = 0;
        numConflictDetectedExceptions = 0;
        numInserts = 0;
        numUpdates = 0;
        numDeletes = 0;
        numEntries = 0;
        numSkippedEntries = 0;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(numAuthExceptions);
        dest.writeLong(numIoExceptions);
        dest.writeLong(numParseExceptions);
        dest.writeLong(numConflictDetectedExceptions);
        dest.writeLong(numInserts);
        dest.writeLong(numUpdates);
        dest.writeLong(numDeletes);
        dest.writeLong(numEntries);
        dest.writeLong(numSkippedEntries);
    }
    public static final Creator<SyncStats> CREATOR = new Creator<SyncStats>() {
        public SyncStats createFromParcel(Parcel in) {
            return new SyncStats(in);
        }
        public SyncStats[] newArray(int size) {
            return new SyncStats[size];
        }
    };
}
