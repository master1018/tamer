public class ArrayUtils
{
    private static Object[] EMPTY = new Object[0];
    private static final int CACHE_SIZE = 73;
    private static Object[] sCache = new Object[CACHE_SIZE];
    private ArrayUtils() {  }
    public static int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++)
            if (need <= (1 << i) - 12)
                return (1 << i) - 12;
        return need;
    }
    public static int idealBooleanArraySize(int need) {
        return idealByteArraySize(need);
    }
    public static int idealShortArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }
    public static int idealCharArraySize(int need) {
        return idealByteArraySize(need * 2) / 2;
    }
    public static int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    public static int idealFloatArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    public static int idealObjectArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }
    public static int idealLongArraySize(int need) {
        return idealByteArraySize(need * 8) / 8;
    }
    public static boolean equals(byte[] array1, byte[] array2, int length) {
        if (array1 == array2) {
            return true;
        }
        if (array1 == null || array2 == null || array1.length < length || array2.length < length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }
    public static <T> T[] emptyArray(Class<T> kind) {
        if (kind == Object.class) {
            return (T[]) EMPTY;
        }
        int bucket = ((System.identityHashCode(kind) / 8) & 0x7FFFFFFF) % CACHE_SIZE;
        Object cache = sCache[bucket];
        if (cache == null || cache.getClass().getComponentType() != kind) {
            cache = Array.newInstance(kind, 0);
            sCache[bucket] = cache;
        }
        return (T[]) cache;
    }
    public static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element == null) {
                if (value == null) return true;
            } else {
                if (value != null && element.equals(value)) return true;
            }
        }
        return false;
    }
}
