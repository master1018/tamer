public final class Leb128Utils {
    private Leb128Utils() {
    }
    public static int unsignedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        while (remaining != 0) {
            remaining >>= 7;
            count++;
        }
        return count + 1;
    }
    public static int signedLeb128Size(int value) {
        int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;
        while (hasMore) {
            hasMore = (remaining != end)
                || ((remaining & 1) != ((value >> 6) & 1));
            value = remaining;
            remaining >>= 7;
            count++;
        }
        return count;
    }
}
