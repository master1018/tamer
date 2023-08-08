public class StoredKeyPair extends AbstractStoredKeyPair {
    public StoredKeyPair(String name) {
        super(name);
    }
    public String getName() {
        return name;
    }
    public byte[] getDecryptionKey() {
        AbstractKeyPair keyPair = KeyStorage.get(getName());
        if (keyPair != null) return keyPair.getDecryptionKey();
        Log.warn("StoredKeyPair.getDecryptionKey: Named Key couldn't be found in the KeyStorage.");
        return null;
    }
    public byte[] getEncryptionKey() {
        AbstractKeyPair keyPair = KeyStorage.get(getName());
        if (keyPair != null) return keyPair.getEncryptionKey();
        Log.warn("StoredKeyPair.getDecryptionKey: Named Key couldn't be found in the KeyStorage.");
        return null;
    }
}
