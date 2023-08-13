public class CipherPBEThread extends CipherThread {
    CipherPBEThread(String name, int[] keys, String[] modes, String[] paddings) {
        super(name, keys, modes, paddings);
    }
    @Override
    public void crypt() throws Exception {
        byte[] output = new byte[128];
        byte[] decrypted = new byte[128];
        byte[] input  =  getData().getBytes();
        byte[] salt = new byte[8];
        SecureRandom sr = new SecureRandom();
        PBEKeySpec keySpec = new PBEKeySpec("top sicret password".toCharArray());
        SecretKeyFactory skf = SecretKeyFactory.getInstance(getAlgName()); 
        SecretKey key = skf.generateSecret(keySpec);
        Cipher cip = Cipher.getInstance(getAlgName() + "/" + getMode() + "/" +
                getPadding());
        sr.nextBytes(salt);
        PBEParameterSpec parSpec = new PBEParameterSpec(salt, getKeyLength());
        cip.init(Cipher.ENCRYPT_MODE, key, parSpec);
        cip.doFinal(input, 0, input.length, output);
        int outputSize = cip.getOutputSize(input.length);
        cip.init(Cipher.DECRYPT_MODE, key, parSpec);
        cip.doFinal(output, 0, outputSize, decrypted);
        checkEncodedData(getData().getBytes(), decrypted);
    }
}
