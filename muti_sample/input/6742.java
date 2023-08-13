public class ByteArrayLexOrder implements Comparator<byte[]> {
    public final int compare( byte[] bytes1, byte[] bytes2) {
        int diff;
        for (int i = 0; i < bytes1.length && i < bytes2.length; i++) {
            diff = (bytes1[i] & 0xFF) - (bytes2[i] & 0xFF);
            if (diff != 0) {
                return diff;
            }
        }
        return bytes1.length - bytes2.length;
    }
}
