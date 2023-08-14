public class AccessibilityServiceInfo implements Parcelable {
    public static final int FEEDBACK_SPOKEN = 0x0000001;
    public static final int FEEDBACK_HAPTIC =  0x0000002;
    public static final int FEEDBACK_AUDIBLE = 0x0000004;
    public static final int FEEDBACK_VISUAL = 0x0000008;
    public static final int FEEDBACK_GENERIC = 0x0000010;
    public static final int DEFAULT = 0x0000001;
    public int eventTypes;
    public String[] packageNames;
    public int feedbackType;
    public long notificationTimeout;
    public int flags;
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel parcel, int flagz) {
        parcel.writeInt(eventTypes);
        parcel.writeStringArray(packageNames);
        parcel.writeInt(feedbackType);
        parcel.writeLong(notificationTimeout);
        parcel.writeInt(flags);
    }
    public static final Parcelable.Creator<AccessibilityServiceInfo> CREATOR =
            new Parcelable.Creator<AccessibilityServiceInfo>() {
        public AccessibilityServiceInfo createFromParcel(Parcel parcel) {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.eventTypes = parcel.readInt();
            info.packageNames = parcel.readStringArray();
            info.feedbackType = parcel.readInt();
            info.notificationTimeout = parcel.readLong();
            info.flags = parcel.readInt();
            return info;
        }
        public AccessibilityServiceInfo[] newArray(int size) {
            return new AccessibilityServiceInfo[size];
        }
    };
}
