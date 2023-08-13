public class GarbageCollectionNotifInfoCompositeData extends LazyCompositeData {
    private final GarbageCollectionNotificationInfo gcNotifInfo;
    public GarbageCollectionNotifInfoCompositeData(GarbageCollectionNotificationInfo info) {
        this.gcNotifInfo = info;
    }
    public GarbageCollectionNotificationInfo getGarbageCollectionNotifInfo() {
        return gcNotifInfo;
    }
    public static CompositeData toCompositeData(GarbageCollectionNotificationInfo info) {
        GarbageCollectionNotifInfoCompositeData gcnicd =
            new GarbageCollectionNotifInfoCompositeData(info);
        return gcnicd.getCompositeData();
    }
    private CompositeType getCompositeTypeByBuilder() {
        final GcInfoBuilder builder = AccessController.doPrivileged (new PrivilegedAction<GcInfoBuilder>() {
                public GcInfoBuilder run() {
                    try {
                        Class cl = Class.forName("com.sun.management.GcInfo");
                        Field f = cl.getDeclaredField("builder");
                        f.setAccessible(true);
                        return (GcInfoBuilder)f.get(gcNotifInfo.getGcInfo());
                    } catch(ClassNotFoundException e) {
                        return null;
                    } catch(NoSuchFieldException e) {
                        return null;
                    } catch(IllegalAccessException e) {
                        return null;
                    }
                }
            });
        CompositeType gict = null;
        synchronized(compositeTypeByBuilder) {
            gict = compositeTypeByBuilder.get(builder);
            if(gict == null) {
                OpenType[] gcNotifInfoItemTypes = new OpenType[] {
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    builder.getGcInfoCompositeType(),
                };
                try {
                    final String typeName =
                        "sun.management.GarbageCollectionNotifInfoCompositeType";
                    gict = new CompositeType(typeName,
                                             "CompositeType for GC notification info",
                                             gcNotifInfoItemNames,
                                             gcNotifInfoItemNames,
                                             gcNotifInfoItemTypes);
                    compositeTypeByBuilder.put(builder,gict);
                } catch (OpenDataException e) {
                    throw Util.newException(e);
                }
            }
        }
        return gict;
    }
    protected CompositeData getCompositeData() {
        final Object[] gcNotifInfoItemValues;
        gcNotifInfoItemValues = new Object[] {
            gcNotifInfo.getGcName(),
            gcNotifInfo.getGcAction(),
            gcNotifInfo.getGcCause(),
            GcInfoCompositeData.toCompositeData(gcNotifInfo.getGcInfo())
        };
        CompositeType gict = getCompositeTypeByBuilder();
        try {
            return new CompositeDataSupport(gict,
                                            gcNotifInfoItemNames,
                                            gcNotifInfoItemValues);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    private static final String GC_NAME = "gcName";
    private static final String GC_ACTION = "gcAction";
    private static final String GC_CAUSE = "gcCause";
    private static final String GC_INFO     = "gcInfo";
    private static final String[] gcNotifInfoItemNames = {
        GC_NAME,
        GC_ACTION,
        GC_CAUSE,
        GC_INFO
    };
    private static HashMap<GcInfoBuilder,CompositeType> compositeTypeByBuilder =
        new HashMap<GcInfoBuilder,CompositeType>();
    public static String getGcName(CompositeData cd) {
        String gcname = getString(cd, GC_NAME);
        if (gcname == null) {
            throw new IllegalArgumentException("Invalid composite data: " +
                "Attribute " + GC_NAME + " has null value");
        }
        return gcname;
    }
    public static String getGcAction(CompositeData cd) {
        String gcaction = getString(cd, GC_ACTION);
        if (gcaction == null) {
            throw new IllegalArgumentException("Invalid composite data: " +
                "Attribute " + GC_ACTION + " has null value");
        }
        return gcaction;
    }
    public static String getGcCause(CompositeData cd) {
        String gccause = getString(cd, GC_CAUSE);
        if (gccause == null) {
            throw new IllegalArgumentException("Invalid composite data: " +
                "Attribute " + GC_CAUSE + " has null value");
        }
        return gccause;
    }
    public static GcInfo getGcInfo(CompositeData cd) {
        CompositeData gcInfoData = (CompositeData) cd.get(GC_INFO);
        return GcInfo.from(gcInfoData);
    }
    public static void validateCompositeData(CompositeData cd) {
        if (cd == null) {
            throw new NullPointerException("Null CompositeData");
        }
        if (!isTypeMatched( getBaseGcNotifInfoCompositeType(), cd.getCompositeType())) {
            throw new IllegalArgumentException(
                "Unexpected composite type for GarbageCollectionNotificationInfo");
        }
    }
    private static CompositeType baseGcNotifInfoCompositeType = null;
    private static synchronized CompositeType getBaseGcNotifInfoCompositeType() {
        if (baseGcNotifInfoCompositeType == null) {
            try {
                OpenType[] baseGcNotifInfoItemTypes = new OpenType[] {
                    SimpleType.STRING,
                    SimpleType.STRING,
                    SimpleType.STRING,
                    GcInfoCompositeData.getBaseGcInfoCompositeType()
                };
                baseGcNotifInfoCompositeType =
                    new CompositeType("sun.management.BaseGarbageCollectionNotifInfoCompositeType",
                                      "CompositeType for Base GarbageCollectionNotificationInfo",
                                      gcNotifInfoItemNames,
                                      gcNotifInfoItemNames,
                                      baseGcNotifInfoItemTypes);
            } catch (OpenDataException e) {
                throw Util.newException(e);
            }
        }
        return baseGcNotifInfoCompositeType;
    }
    private static final long serialVersionUID = -1805123446483771292L;
}
