public class GcInfoBuilder {
    private final GarbageCollectorMXBean gc;
    private final String[] poolNames;
    private String[] allItemNames;
    private CompositeType gcInfoCompositeType;
    private final int gcExtItemCount;
    private final String[] gcExtItemNames;
    private final String[] gcExtItemDescs;
    private final char[] gcExtItemTypes;
    GcInfoBuilder(GarbageCollectorMXBean gc, String[] poolNames) {
        this.gc = gc;
        this.poolNames = poolNames;
        this.gcExtItemCount = getNumGcExtAttributes(gc);
        this.gcExtItemNames = new String[gcExtItemCount];
        this.gcExtItemDescs = new String[gcExtItemCount];
        this.gcExtItemTypes = new char[gcExtItemCount];
        fillGcAttributeInfo(gc, gcExtItemCount, gcExtItemNames,
                            gcExtItemTypes, gcExtItemDescs);
        this.gcInfoCompositeType = null;
    }
    GcInfo getLastGcInfo() {
        MemoryUsage[] usageBeforeGC = new MemoryUsage[poolNames.length];
        MemoryUsage[] usageAfterGC = new MemoryUsage[poolNames.length];
        Object[] values = new Object[gcExtItemCount];
        return getLastGcInfo0(gc, gcExtItemCount, values, gcExtItemTypes,
                              usageBeforeGC, usageAfterGC);
    }
    public String[] getPoolNames() {
        return poolNames;
    }
    int getGcExtItemCount() {
        return gcExtItemCount;
    }
    synchronized CompositeType getGcInfoCompositeType() {
        if (gcInfoCompositeType != null)
            return gcInfoCompositeType;
        String[] gcInfoItemNames = GcInfoCompositeData.getBaseGcInfoItemNames();
        OpenType[] gcInfoItemTypes = GcInfoCompositeData.getBaseGcInfoItemTypes();
        int numGcInfoItems = gcInfoItemNames.length;
        int itemCount = numGcInfoItems + gcExtItemCount;
        allItemNames = new String[itemCount];
        String[] allItemDescs = new String[itemCount];
        OpenType[] allItemTypes = new OpenType[itemCount];
        System.arraycopy(gcInfoItemNames, 0, allItemNames, 0, numGcInfoItems);
        System.arraycopy(gcInfoItemNames, 0, allItemDescs, 0, numGcInfoItems);
        System.arraycopy(gcInfoItemTypes, 0, allItemTypes, 0, numGcInfoItems);
        if (gcExtItemCount > 0) {
            fillGcAttributeInfo(gc, gcExtItemCount, gcExtItemNames,
                                gcExtItemTypes, gcExtItemDescs);
            System.arraycopy(gcExtItemNames, 0, allItemNames,
                             numGcInfoItems, gcExtItemCount);
            System.arraycopy(gcExtItemDescs, 0, allItemDescs,
                             numGcInfoItems, gcExtItemCount);
            for (int i = numGcInfoItems, j = 0; j < gcExtItemCount; i++, j++) {
                switch (gcExtItemTypes[j]) {
                    case 'Z':
                        allItemTypes[i] = SimpleType.BOOLEAN;
                        break;
                    case 'B':
                        allItemTypes[i] = SimpleType.BYTE;
                        break;
                    case 'C':
                        allItemTypes[i] = SimpleType.CHARACTER;
                        break;
                    case 'S':
                        allItemTypes[i] = SimpleType.SHORT;
                        break;
                    case 'I':
                        allItemTypes[i] = SimpleType.INTEGER;
                        break;
                    case 'J':
                        allItemTypes[i] = SimpleType.LONG;
                        break;
                    case 'F':
                        allItemTypes[i] = SimpleType.FLOAT;
                        break;
                    case 'D':
                        allItemTypes[i] = SimpleType.DOUBLE;
                        break;
                    default:
                        throw new AssertionError(
                            "Unsupported type [" + gcExtItemTypes[i] + "]");
                }
            }
        }
        CompositeType gict = null;
        try {
            final String typeName =
                "sun.management." + gc.getName() + ".GcInfoCompositeType";
            gict = new CompositeType(typeName,
                                     "CompositeType for GC info for " +
                                         gc.getName(),
                                     allItemNames,
                                     allItemDescs,
                                     allItemTypes);
        } catch (OpenDataException e) {
            throw Util.newException(e);
        }
        gcInfoCompositeType = gict;
        return gcInfoCompositeType;
    }
    synchronized String[] getItemNames() {
        if (allItemNames == null) {
            getGcInfoCompositeType();
        }
        return allItemNames;
    }
    private native int getNumGcExtAttributes(GarbageCollectorMXBean gc);
    private native void fillGcAttributeInfo(GarbageCollectorMXBean gc,
                                            int numAttributes,
                                            String[] attributeNames,
                                            char[] types,
                                            String[] descriptions);
    private native GcInfo getLastGcInfo0(GarbageCollectorMXBean gc,
                                         int numExtAtts,
                                         Object[] extAttValues,
                                         char[] extAttTypes,
                                         MemoryUsage[] before,
                                         MemoryUsage[] after);
}
