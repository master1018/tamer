public class PlatformAddressFactory {
    private final static int CACHE_SIZE = 1<<8;
    private final static int CACHE_MASK = CACHE_SIZE - 1;
    private final static int MAX_PROBES = 5;
    private static int replacementIndex = 0;
    private static PlatformAddress[] cache = new PlatformAddress[CACHE_SIZE];
    private static PlatformAddress make(int value, long size) {
        if (value == 0) {
            return PlatformAddress.NULL;
        }
        return new PlatformAddress(value, size);
    }
    public synchronized static PlatformAddress on(int value, long size) {
        if (value == 0) {
            return PlatformAddress.NULL;
        }
        int idx = value >> 5;
        for (int probe = 0; probe < MAX_PROBES; probe++) {
            PlatformAddress cachedObj = cache[(idx + probe) & CACHE_MASK];
            if (cachedObj == null) {
                return cache[(idx + probe) & CACHE_MASK] =
                    new PlatformAddress(value, size);
            }
            if (cachedObj.osaddr == value && cachedObj.size == size) {
                return cachedObj;
            }
        }
        replacementIndex = (replacementIndex + 1) % MAX_PROBES;
        return cache[(idx + replacementIndex) & CACHE_MASK] =
            new PlatformAddress(value, size);
    }
    public static PlatformAddress on(int value) {
        return PlatformAddressFactory.on(value, PlatformAddress.UNKNOWN);
    }
    public static MappedPlatformAddress mapOn(int value, long size) {
        MappedPlatformAddress addr = new MappedPlatformAddress(value, size);
        return addr;
    }
    public static PlatformAddress allocMap(int fd, long start, long size, int mode) throws IOException {
        if (size == 0) {
            return mapOn(0, 0);
        }
        int osAddress = PlatformAddress.osMemory.mmap(fd, start, size, mode);
        PlatformAddress newMemory = mapOn(osAddress, size);
        PlatformAddress.memorySpy.alloc(newMemory);
        return newMemory;
    }
    public static PlatformAddress alloc(int size) {
        int osAddress = PlatformAddress.osMemory.malloc(size);
        PlatformAddress newMemory = make(osAddress, size);
        PlatformAddress.memorySpy.alloc(newMemory);
        return newMemory;
    }
    public static PlatformAddress alloc(int size, byte init) {
        int osAddress = PlatformAddress.osMemory.malloc(size);
        PlatformAddress.osMemory.memset(osAddress, init, size);
        PlatformAddress newMemory = make(osAddress, size);
        PlatformAddress.memorySpy.alloc(newMemory);
        return newMemory;
    }
}
