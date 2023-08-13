public final class ViewIdGenerator implements Parcelable {
    private static final int INVALID_VIEW_ID = 0;
    private static final int INITIAL_VIEW_ID = 1;
    public static final int NO_VIEW_INDEX = -1;
    private int mNextId;
    private Bundle mIdMap = new Bundle();
    private static final char KEY_SEPARATOR = '*';
    private final static StringBuilder sWorkStringBuilder = new StringBuilder();
    public ViewIdGenerator() {
        mNextId = INITIAL_VIEW_ID;
    }
    public int describeContents() {
        return 0;
    }
    public int getId(EntityDelta entity, DataKind kind, ValuesDelta values,
            int viewIndex) {
        final String k = getMapKey(entity, kind, values, viewIndex);
        int id = mIdMap.getInt(k, INVALID_VIEW_ID);
        if (id == INVALID_VIEW_ID) {
            id = (mNextId++) & 0xFFFF;
            mIdMap.putInt(k, id);
        }
        return id;
    }
    private static String getMapKey(EntityDelta entity, DataKind kind, ValuesDelta values,
            int viewIndex) {
        sWorkStringBuilder.setLength(0);
        if (entity != null) {
            sWorkStringBuilder.append(entity.getValues().getId());
            if (kind != null) {
                sWorkStringBuilder.append(KEY_SEPARATOR);
                sWorkStringBuilder.append(kind.mimeType);
                if (values != null) {
                    sWorkStringBuilder.append(KEY_SEPARATOR);
                    sWorkStringBuilder.append(values.getId());
                    if (viewIndex != NO_VIEW_INDEX) {
                        sWorkStringBuilder.append(KEY_SEPARATOR);
                        sWorkStringBuilder.append(viewIndex);
                    }
                }
            }
        }
        return sWorkStringBuilder.toString();
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mNextId);
        dest.writeBundle(mIdMap);
    }
    private void readFromParcel(Parcel src) {
        mNextId = src.readInt();
        mIdMap = src.readBundle();
    }
    public static final Parcelable.Creator<ViewIdGenerator> CREATOR =
            new Parcelable.Creator<ViewIdGenerator>() {
        public ViewIdGenerator createFromParcel(Parcel in) {
            final ViewIdGenerator vig = new ViewIdGenerator();
            vig.readFromParcel(in);
            return vig;
        }
        public ViewIdGenerator[] newArray(int size) {
            return new ViewIdGenerator[size];
        }
    };
}
