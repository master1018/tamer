class AddressCache {
    private static final int MAX_ENTRIES = 512;
    private static final float DEFAULT_LOAD_FACTOR = .75F;
    private static final long DEFAULT_POSITIVE_TTL_NANOS = 600 * 1000000000L;
    private static final long DEFAULT_NEGATIVE_TTL_NANOS = 10 * 1000000000L;
    private static final InetAddress[] NO_ADDRESSES = new InetAddress[0];
    private final Map<String, AddressCacheEntry> map;
    static class AddressCacheEntry {
        InetAddress[] addresses;
        long expiryNanos;
        AddressCacheEntry(InetAddress[] addresses, long expiryNanos) {
            this.addresses = addresses;
            this.expiryNanos = expiryNanos;
        }
    }
    public AddressCache() {
        map = new LinkedHashMap<String, AddressCacheEntry>(0, DEFAULT_LOAD_FACTOR, true) {
            @Override protected boolean removeEldestEntry(Entry<String, AddressCacheEntry> eldest) {
                return size() == MAX_ENTRIES;
            }
        };
    }
    public InetAddress[] get(String hostname) {
        AddressCacheEntry entry;
        synchronized (map) {
            entry = map.get(hostname);
        }
        if (entry != null && entry.expiryNanos >= System.nanoTime()) {
            return entry.addresses;
        }
        return null;
    }
    public void put(String hostname, InetAddress[] addresses) {
        boolean isPositive = (addresses.length > 0);
        String propertyName = isPositive ? "networkaddress.cache.ttl" : "networkaddress.cache.negative.ttl";
        long defaultTtlNanos = isPositive ? DEFAULT_POSITIVE_TTL_NANOS : DEFAULT_NEGATIVE_TTL_NANOS;
        long expiryNanos = System.nanoTime() + defaultTtlNanos;
        if (System.getSecurityManager() != null || System.getProperty(propertyName, null) != null) {
            expiryNanos = customTtl(propertyName, defaultTtlNanos);
            if (expiryNanos == Long.MIN_VALUE) {
                return;
            }
        }
        synchronized (map) {
            map.put(hostname, new AddressCacheEntry(addresses, expiryNanos));
        }
    }
    public void putUnknownHost(String hostname) {
        put(hostname, NO_ADDRESSES);
    }
    private long customTtl(String propertyName, long defaultTtlNanos) {
        String ttlString = AccessController.doPrivileged(new PriviAction<String>(propertyName, null));
        if (ttlString == null) {
            return System.nanoTime() + defaultTtlNanos;
        }
        try {
            long ttlS = Long.parseLong(ttlString);
            if (ttlS == -1) {
                return Long.MAX_VALUE;
            } else if (ttlS == 0) {
                return Long.MIN_VALUE;
            } else {
                return System.nanoTime() + ttlS * 1000000000L;
            }
        } catch (NumberFormatException ex) {
            return System.nanoTime() + defaultTtlNanos;
        }
    }
}
