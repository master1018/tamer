public class CipherRSAThread extends CipherThread {
    CipherRSAThread(String name, int[] keys, String[] modes, String[] paddings) {
        super(name, keys, modes, paddings);
    }
    @Override
    public void crypt() throws Exception {
        byte[] output = new byte[256];
        byte[] decrypted = new byte[256];
        int dataBlock = 20;
        byte[] input  =  getData().substring(0, dataBlock).getBytes();
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(getKeyLength());
        KeyPair kp = kpg.generateKeyPair();
        Cipher cip = Cipher.getInstance(getAlgName() + "/" + getMode() + "/" +
                getPadding());
        cip.init(Cipher.ENCRYPT_MODE, kp.getPublic());
        cip.doFinal(input, 0, input.length, output);
        int outputSize = cip.getOutputSize(input.length);
        cip.init(Cipher.DECRYPT_MODE, kp.getPrivate());
        cip.doFinal(output, 0, outputSize, decrypted);
        checkEncodedData(input, decrypted);
    }
}
