public class MemoryUsageCompositeData extends LazyCompositeData {
    private final MemoryUsage usage;
    private MemoryUsageCompositeData(MemoryUsage u) {
        this.usage = u;
    }
    public MemoryUsage getMemoryUsage() {
        return usage;
    }
    public static CompositeData toCompositeData(MemoryUsage u) {
        MemoryUsageCompositeData mucd = new MemoryUsageCompositeData(u);
        return mucd.getCompositeData();
    }
    protected CompositeData getCompositeData() {
        final Object[] memoryUsageItemValues = {
            new Long(usage.getInit()),
            new Long(usage.getUsed()),
            new Long(usage.getCommitted()),
            new Long(usage.getMax()),
        };
        try {
            return new CompositeDataSupport(memoryUsageCompositeType,
                                            memoryUsageItemNames,
                                            memoryUsageItemValues);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    private static final CompositeType memoryUsageCompositeType;
    static {
        try {
            memoryUsageCompositeType = (CompositeType)
                MappedMXBeanType.toOpenType(MemoryUsage.class);
        } catch (OpenDataException e) {
            throw new AssertionError(e);
        }
    }
    static CompositeType getMemoryUsageCompositeType() {
        return memoryUsageCompositeType;
    }
    private static final String INIT      = "init";
    private static final String USED      = "used";
    private static final String COMMITTED = "committed";
    private static final String MAX       = "max";
    private static final String[] memoryUsageItemNames = {
        INIT,
        USED,
        COMMITTED,
        MAX,
    };
    public static long getInit(CompositeData cd) {
        return getLong(cd, INIT);
    }
    public static long getUsed(CompositeData cd) {
        return getLong(cd, USED);
    }
    public static long getCommitted(CompositeData cd) {
        return getLong(cd, COMMITTED);
    }
    public static long getMax(CompositeData cd) {
        return getLong(cd, MAX);
    }
    public static void validateCompositeData(CompositeData cd) {
        if (cd == null) {
            throw new NullPointerException("Null CompositeData");
        }
        if (!isTypeMatched(memoryUsageCompositeType, cd.getCompositeType())) {
            throw new IllegalArgumentException(
                "Unexpected composite type for MemoryUsage");
        }
    }
    private static final long serialVersionUID = -8504291541083874143L;
}
