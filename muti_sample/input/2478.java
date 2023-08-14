public class Indefinite {
    public static void main(String[] args) throws Exception {
        byte[] input = {
            4, (byte)0x80, 4, 2, 'a', 'b', 4, 2, 'c', 'd', 0, 0,
            0, 0, 0, 0
        };
        new DerValue(new ByteArrayInputStream(input));
    }
}
