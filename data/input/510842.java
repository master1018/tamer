public class SystemKeyStore {
    private static final String SYSTEM_KEYSTORE_DIRECTORY = "misc/systemkeys";
    private static final String KEY_FILE_EXTENSION = ".sks";
    private static SystemKeyStore mInstance = new SystemKeyStore();
    private SystemKeyStore() { }
    public static SystemKeyStore getInstance() {
        return mInstance;
    }
    public static String toHexString(byte[] keyData) {
        if (keyData == null) {
            return null;
        }
        int keyLen = keyData.length;
        int expectedStringLen = keyData.length * 2;
        StringBuilder sb = new StringBuilder(expectedStringLen);
        for (int i = 0; i < keyData.length; i++) {
            String hexStr = Integer.toString(keyData[i] & 0x00FF, 16);
            if (hexStr.length() == 1) {
                hexStr = "0" + hexStr;
            }
            sb.append(hexStr);
        }
        return sb.toString();
    }
    public String generateNewKeyHexString(int numBits, String algName, String keyName)
            throws NoSuchAlgorithmException {
        return toHexString(generateNewKey(numBits, algName, keyName));
    }
    public byte[] generateNewKey(int numBits, String algName, String keyName)
            throws NoSuchAlgorithmException {
        File keyFile = getKeyFile(keyName);
        if (keyFile.exists()) {
            throw new IllegalArgumentException();
        }
        KeyGenerator skg = KeyGenerator.getInstance(algName);
        SecureRandom srng = SecureRandom.getInstance("SHA1PRNG");
        skg.init(numBits, srng);
        SecretKey sk = skg.generateKey();
        byte[] retKey = sk.getEncoded();
        try {
            if (!keyFile.createNewFile()) {
                throw new IllegalArgumentException();
            }
            FileOutputStream fos = new FileOutputStream(keyFile);
            fos.write(retKey);
            fos.flush();
            fos.close();
            FileUtils.setPermissions(keyFile.getName(), (FileUtils.S_IRUSR | FileUtils.S_IWUSR),
                -1, -1);
        } catch (IOException ioe) {
            return null;
        }
        return retKey;
    }
    private File getKeyFile(String keyName) {
        File sysKeystoreDir = new File(Environment.getDataDirectory(),
                SYSTEM_KEYSTORE_DIRECTORY);
        File keyFile = new File(sysKeystoreDir, keyName + KEY_FILE_EXTENSION);
        return keyFile;
    }
    public String retrieveKeyHexString(String keyName) {
        return toHexString(retrieveKey(keyName));
    }
    public byte[] retrieveKey(String keyName) {
        File keyFile = getKeyFile(keyName);
        if (!keyFile.exists()) {
            return null;
        }
        try {
            FileInputStream fis = new FileInputStream(keyFile);
            int keyLen = fis.available();
            byte[] retKey = new byte[keyLen];
            fis.read(retKey);
            fis.close();
            return retKey;
        } catch (IOException ioe) { }
        throw new IllegalArgumentException();
    }
    public void deleteKey(String keyName) {
        File keyFile = getKeyFile(keyName);
        if (!keyFile.exists()) {
            throw new IllegalArgumentException();
        }
        keyFile.delete();
    }
}
