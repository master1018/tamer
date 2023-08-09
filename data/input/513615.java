public class MyEncodedKeySpec extends EncodedKeySpec {
    public MyEncodedKeySpec(byte[] encodedKey) {
        super(encodedKey);
    }
    public String getFormat() {
        return "My";
    }
}
