public class XPropertyCache {
    static class PropertyCacheEntry {
        private final int format;
        private final int numberOfItems;
        private final long bytesAfter;
        private final long data;
        private final int dataLength;
        public PropertyCacheEntry(int format, int numberOfItems, long bytesAfter, long data, int dataLength) {
            this.format = format;
            this.numberOfItems = numberOfItems;
            this.bytesAfter = bytesAfter;
            this.data = XlibWrapper.unsafe.allocateMemory(dataLength);
            this.dataLength = dataLength;
            XlibWrapper.memcpy(this.data, data, dataLength);
        }
        public int getFormat() {
            return format;
        }
        public int getNumberOfItems() {
            return numberOfItems;
        }
        public long getBytesAfter() {
            return bytesAfter;
        }
        public long getData() {
            return data;
        }
        public int getDataLength() {
            return dataLength;
        }
    }
    private static Map<Long, Map<XAtom, PropertyCacheEntry>> windowToMap = new HashMap<Long, Map<XAtom, PropertyCacheEntry>>();
    public static boolean isCached(long window, XAtom property) {
        Map<XAtom, PropertyCacheEntry> entryMap = windowToMap.get(window);
        if (entryMap != null) {
            return entryMap.containsKey(property);
        } else {
            return false;
        }
    }
    public static PropertyCacheEntry getCacheEntry(long window, XAtom property) {
        Map<XAtom, PropertyCacheEntry> entryMap = windowToMap.get(window);
        if (entryMap != null) {
            return entryMap.get(property);
        } else {
            return null;
        }
    }
    public static void storeCache(PropertyCacheEntry entry, long window, XAtom property) {
        Map<XAtom, PropertyCacheEntry> entryMap = windowToMap.get(window);
        if (entryMap == null) {
            entryMap = new HashMap<XAtom, PropertyCacheEntry>();
            windowToMap.put(window, entryMap);
        }
        entryMap.put(property, entry);
    }
    public static void clearCache(long window) {
        windowToMap.remove(window);
    }
    public static void clearCache(long window, XAtom property) {
        Map<XAtom, PropertyCacheEntry> entryMap = windowToMap.get(window);
        if (entryMap != null) {
            entryMap.remove(property);
        }
    }
    public static boolean isCachingSupported() {
        return false;
    }
}
