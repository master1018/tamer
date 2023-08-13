public final class ArrayHelper {
    public static void swap32bitsToArray(int value, byte[] dest, int offset) {
        dest[offset] = (byte)(value & 0x000000FF);
        dest[offset + 1] = (byte)((value & 0x0000FF00) >> 8);
        dest[offset + 2] = (byte)((value & 0x00FF0000) >> 16);
        dest[offset + 3] = (byte)((value & 0xFF000000) >> 24);
    }
    public static int swap32bitFromArray(byte[] value, int offset) {
        int v = 0;
        v |= ((int)value[offset]) & 0x000000FF;
        v |= (((int)value[offset + 1]) & 0x000000FF) << 8;
        v |= (((int)value[offset + 2]) & 0x000000FF) << 16;
        v |= (((int)value[offset + 3]) & 0x000000FF) << 24;
        return v;
    }
    public static int swapU16bitFromArray(byte[] value, int offset) {
        int v = 0;
        v |= ((int)value[offset]) & 0x000000FF;
        v |= (((int)value[offset + 1]) & 0x000000FF) << 8;
        return v;
    }
    public static long swap64bitFromArray(byte[] value, int offset) {
        long v = 0;
        v |= ((long)value[offset]) & 0x00000000000000FFL;
        v |= (((long)value[offset + 1]) & 0x00000000000000FFL) << 8;
        v |= (((long)value[offset + 2]) & 0x00000000000000FFL) << 16;
        v |= (((long)value[offset + 3]) & 0x00000000000000FFL) << 24;
        v |= (((long)value[offset + 4]) & 0x00000000000000FFL) << 32;
        v |= (((long)value[offset + 5]) & 0x00000000000000FFL) << 40;
        v |= (((long)value[offset + 6]) & 0x00000000000000FFL) << 48;
        v |= (((long)value[offset + 7]) & 0x00000000000000FFL) << 56;
        return v;
    }
}
