public class KeyString {
    public static void main(String[] args) throws Exception {
        KeyTabEntry e = new KeyTabEntry(null, null, null, 1, 1, new byte[8]);
        if (e.getKeyString().length() != 18) {
            throw new Exception("key bytes length not correct");
        }
    }
}
