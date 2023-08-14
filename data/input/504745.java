public class CipherWrapThread extends CipherThread {
    CipherWrapThread(String name, int[] keys, String[] modes, String[] paddings) {
        super(name, keys, modes, paddings);
    }
    @Override
    public void crypt() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(getAlgName().replace("Wrap", ""));
        kg.init(getKeyLength(), new SecureRandom());
        Key key = kg.generateKey();
        Cipher cip = Cipher.getInstance(getAlgName());
        cip.init(Cipher.WRAP_MODE, key);
        byte[] output = cip.wrap(key);
        cip.init(Cipher.UNWRAP_MODE, key);
        Key decrypted = cip.unwrap(output, getAlgName(), Cipher.SECRET_KEY);
        checkEncodedData(key.getFormat().getBytes(), decrypted.getFormat().getBytes());
        checkEncodedData(key.getEncoded(), decrypted.getEncoded());
    }
}
