public class EncryptedPKInfoEqualsHashCode {
    public static void main(String[] args) throws Exception {
        EncryptedPrivateKeyInfo ev1;
        EncryptedPrivateKeyInfo ev2;
        AlgorithmId dh = AlgorithmId.get("DH");
        byte key1[] = {
            (byte)0xD4,(byte)0xA0,(byte)0xBA,(byte)0x02,
            (byte)0x50,(byte)0xB6,(byte)0xFD,(byte)0x2E,
            (byte)0xC6,(byte)0x26,(byte)0xE7,(byte)0xEF,
            (byte)0xD6,(byte)0x37,(byte)0xDF,(byte)0x76,
            (byte)0xC7,(byte)0x16,(byte)0xE2,(byte)0x2D,
            (byte)0x09,(byte)0x44,(byte)0xB8,(byte)0x8B,
        };
        ev1 = new  EncryptedPrivateKeyInfo(dh, key1);
        ev2 = new  EncryptedPrivateKeyInfo(dh, key1);
        if ( (ev1.equals(ev2)) == (ev1.hashCode()==ev2.hashCode()) )
            System.out.println("PASSED");
        else
            throw new Exception("Failed equals()/hashCode() contract");
    }
}
