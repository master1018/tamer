public final class AccessibilityEvent implements Parcelable {
    public static final int INVALID_POSITION = -1;
    public static final int MAX_TEXT_LENGTH = 500;
    public static final int TYPE_VIEW_CLICKED = 0x00000001;
    public static final int TYPE_VIEW_LONG_CLICKED = 0x00000002;
    public static final int TYPE_VIEW_SELECTED = 0x00000004;
    public static final int TYPE_VIEW_FOCUSED = 0x00000008;
    public static final int TYPE_VIEW_TEXT_CHANGED = 0x00000010;
    public static final int TYPE_WINDOW_STATE_CHANGED = 0x00000020;
    public static final int TYPE_NOTIFICATION_STATE_CHANGED = 0x00000040;
    public static final int TYPES_ALL_MASK = 0xFFFFFFFF;
    private static final int MAX_POOL_SIZE = 2;
    private static final Object mPoolLock = new Object();
    private static AccessibilityEvent sPool;
    private static int sPoolSize;
    private static final int CHECKED = 0x00000001;
    private static final int ENABLED = 0x00000002;
    private static final int PASSWORD = 0x00000004;
    private static final int FULL_SCREEN = 0x00000080;
    private AccessibilityEvent mNext;
    private int mEventType;
    private int mBooleanProperties;
    private int mCurrentItemIndex;
    private int mItemCount;
    private int mFromIndex;
    private int mAddedCount;
    private int mRemovedCount;
    private long mEventTime;
    private CharSequence mClassName;
    private CharSequence mPackageName;
    private CharSequence mContentDescription;
    private CharSequence mBeforeText;
    private Parcelable mParcelableData;
    private final List<CharSequence> mText = new ArrayList<CharSequence>();
    private boolean mIsInPool;
    private AccessibilityEvent() {
        mCurrentItemIndex = INVALID_POSITION;
    }
    public boolean isChecked() {
        return getBooleanProperty(CHECKED);
    }
    public void setChecked(boolean isChecked) {
        setBooleanProperty(CHECKED, isChecked);
    }
    public boolean isEnabled() {
        return getBooleanProperty(ENABLED);
    }
    public void setEnabled(boolean isEnabled) {
        setBooleanProperty(ENABLED, isEnabled);
    }
    public boolean isPassword() {
        return getBooleanProperty(PASSWORD);
    }
    public void setPassword(boolean isPassword) {
        setBooleanProperty(PASSWORD, isPassword);
    }
    public void setFullScreen(boolean isFullScreen) {
        setBooleanProperty(FULL_SCREEN, isFullScreen);
    }
    public boolean isFullScreen() {
        return getBooleanProperty(FULL_SCREEN);
    }
    public int getEventType() {
        return mEventType;
    }
    public void setEventType(int eventType) {
        mEventType = eventType;
    }
    public int getItemCount() {
        return mItemCount;
    }
    public void setItemCount(int itemCount) {
        mItemCount = itemCount;
    }
    public int getCurrentItemIndex() {
        return mCurrentItemIndex;
    }
    public void setCurrentItemIndex(int currentItemIndex) {
        mCurrentItemIndex = currentItemIndex;
    }
    public int getFromIndex() {
        return mFromIndex;
    }
    public void setFromIndex(int fromIndex) {
        mFromIndex = fromIndex;
    }
    public int getAddedCount() {
        return mAddedCount;
    }
    public void setAddedCount(int addedCount) {
        mAddedCount = addedCount;
    }
    public int getRemovedCount() {
        return mRemovedCount;
    }
    public void setRemovedCount(int removedCount) {
        mRemovedCount = removedCount;
    }
    public long getEventTime() {
        return mEventTime;
    }
    public void setEventTime(long eventTime) {
        mEventTime = eventTime;
    }
    public CharSequence getClassName() {
        return mClassName;
    }
    public void setClassName(CharSequence className) {
        mClassName = className;
    }
    public CharSequence getPackageName() {
        return mPackageName;
    }
    public void setPackageName(CharSequence packageName) {
        mPackageName = packageName;
    }
    public List<CharSequence> getText() {
        return mText;
    }
    public CharSequence getBeforeText() {
        return mBeforeText;
    }
    public void setBeforeText(CharSequence beforeText) {
        mBeforeText = beforeText;
    }
    public CharSequence getContentDescription() {
        return mContentDescription;
    }
    public void setContentDescription(CharSequence contentDescription) {
        mContentDescription = contentDescription;
    }
    public Parcelable getParcelableData() {
        return mParcelableData;
    }
    public void setParcelableData(Parcelable parcelableData) {
        mParcelableData = parcelableData;
    }
    public static AccessibilityEvent obtain(int eventType) {
        AccessibilityEvent event = AccessibilityEvent.obtain();
        event.setEventType(eventType);
        return event;
    }
    public static AccessibilityEvent obtain() {
        synchronized (mPoolLock) {
            if (sPool != null) {
                AccessibilityEvent event = sPool;
                sPool = sPool.mNext;
                sPoolSize--;
                event.mNext = null;
                event.mIsInPool = false;
                return event;
            }
            return new AccessibilityEvent();
        }
    }
    public void recycle() {
        if (mIsInPool) {
            return;
        }
        clear();
        synchronized (mPoolLock) {
            if (sPoolSize <= MAX_POOL_SIZE) {
                mNext = sPool;
                sPool = this;
                mIsInPool = true;
                sPoolSize++;
            }
        }
    }
    private void clear() {
        mEventType = 0;
        mBooleanProperties = 0;
        mCurrentItemIndex = INVALID_POSITION;
        mItemCount = 0;
        mFromIndex = 0;
        mAddedCount = 0;
        mRemovedCount = 0;
        mEventTime = 0;
        mClassName = null;
        mPackageName = null;
        mContentDescription = null;
        mBeforeText = null;
        mText.clear();
    }
    private boolean getBooleanProperty(int property) {
        return (mBooleanProperties & property) == property;
    }
    private void setBooleanProperty(int property, boolean value) {
        if (value) {
            mBooleanProperties |= property;
        } else {
            mBooleanProperties &= ~property;
        }
    }
    public void initFromParcel(Parcel parcel) {
        mEventType = parcel.readInt();
        mBooleanProperties = parcel.readInt();
        mCurrentItemIndex = parcel.readInt();
        mItemCount = parcel.readInt();
        mFromIndex = parcel.readInt();
        mAddedCount = parcel.readInt();
        mRemovedCount = parcel.readInt();
        mEventTime = parcel.readLong();
        mClassName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        mPackageName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        mBeforeText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel);
        mParcelableData = parcel.readParcelable(null);
        parcel.readList(mText, null);
    }
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(mEventType);
        parcel.writeInt(mBooleanProperties);
        parcel.writeInt(mCurrentItemIndex);
        parcel.writeInt(mItemCount);
        parcel.writeInt(mFromIndex);
        parcel.writeInt(mAddedCount);
        parcel.writeInt(mRemovedCount);
        parcel.writeLong(mEventTime);
        TextUtils.writeToParcel(mClassName, parcel, 0);
        TextUtils.writeToParcel(mPackageName, parcel, 0);
        TextUtils.writeToParcel(mContentDescription, parcel, 0);
        TextUtils.writeToParcel(mBeforeText, parcel, 0);
        parcel.writeParcelable(mParcelableData, flags);
        parcel.writeList(mText);
    }
    public int describeContents() {
        return 0;
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append("; EventType: " + mEventType);
        builder.append("; EventTime: " + mEventTime);
        builder.append("; ClassName: " + mClassName);
        builder.append("; PackageName: " + mPackageName);
        builder.append("; Text: " + mText);
        builder.append("; ContentDescription: " + mContentDescription);
        builder.append("; ItemCount: " + mItemCount);
        builder.append("; CurrentItemIndex: " + mCurrentItemIndex);
        builder.append("; IsEnabled: " + isEnabled());
        builder.append("; IsPassword: " + isPassword());
        builder.append("; IsChecked: " + isChecked());
        builder.append("; IsFullScreen: " + isFullScreen());
        builder.append("; BeforeText: " + mBeforeText);
        builder.append("; FromIndex: " + mFromIndex);
        builder.append("; AddedCount: " + mAddedCount);
        builder.append("; RemovedCount: " + mRemovedCount);
        builder.append("; ParcelableData: " + mParcelableData);
        return builder.toString();
    }
    public static final Parcelable.Creator<AccessibilityEvent> CREATOR =
            new Parcelable.Creator<AccessibilityEvent>() {
        public AccessibilityEvent createFromParcel(Parcel parcel) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.initFromParcel(parcel);
            return event;
        }
        public AccessibilityEvent[] newArray(int size) {
            return new AccessibilityEvent[size];
        }
    };
}
