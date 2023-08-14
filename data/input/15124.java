public class GarbageCollectionNotificationInfo implements  CompositeDataView {
    private final String gcName;
    private final String gcAction;
    private final String gcCause;
    private final GcInfo gcInfo;
    private final CompositeData cdata;
    public static final String GARBAGE_COLLECTION_NOTIFICATION =
        "com.sun.management.gc.notification";
    public GarbageCollectionNotificationInfo(String gcName,
                                             String gcAction,
                                             String gcCause,
                                             GcInfo gcInfo)  {
        if (gcName == null) {
            throw new NullPointerException("Null gcName");
        }
        if (gcAction == null) {
            throw new NullPointerException("Null gcAction");
        }
        if (gcCause == null) {
            throw new NullPointerException("Null gcCause");
        }
        this.gcName = gcName;
        this.gcAction = gcAction;
        this.gcCause = gcCause;
        this.gcInfo = gcInfo;
        this.cdata = new GarbageCollectionNotifInfoCompositeData(this);
    }
    GarbageCollectionNotificationInfo(CompositeData cd) {
        GarbageCollectionNotifInfoCompositeData.validateCompositeData(cd);
        this.gcName = GarbageCollectionNotifInfoCompositeData.getGcName(cd);
        this.gcAction = GarbageCollectionNotifInfoCompositeData.getGcAction(cd);
        this.gcCause = GarbageCollectionNotifInfoCompositeData.getGcCause(cd);
        this.gcInfo = GarbageCollectionNotifInfoCompositeData.getGcInfo(cd);
        this.cdata = cd;
    }
    public String getGcName() {
        return gcName;
    }
    public String getGcAction() {
        return gcAction;
    }
    public String getGcCause() {
        return gcCause;
    }
    public GcInfo getGcInfo() {
        return gcInfo;
    }
    public static GarbageCollectionNotificationInfo from(CompositeData cd) {
        if (cd == null) {
            return null;
        }
        if (cd instanceof GarbageCollectionNotifInfoCompositeData) {
            return ((GarbageCollectionNotifInfoCompositeData) cd).getGarbageCollectionNotifInfo();
        } else {
            return new GarbageCollectionNotificationInfo(cd);
        }
    }
    public CompositeData toCompositeData(CompositeType ct) {
        return cdata;
    }
}
