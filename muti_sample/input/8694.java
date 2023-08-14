public class ByteArrayTagOrder implements Comparator<byte[]> {
    public final int compare(byte[] bytes1, byte[] bytes2) {
        return (bytes1[0] | 0x20) - (bytes2[0] | 0x20);
    }
}
