public class KeyGeneratorThread extends TestThread {
    KeyGeneratorThread(String[] names) {
        super(names);
    }
    public void test() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(algName);
        Key k = kg.generateKey();
        if(kg.getAlgorithm().toLowerCase().equals(k.getAlgorithm().toLowerCase()) != true) {
            throw new Exception ("Algorithm names not matched for KeyGenerator" +
                    " and for Key objects");
        }
        if(kg.getAlgorithm().toLowerCase().equals(algName.toLowerCase()) != true) {
            throw new Exception ("Algorithm names not matched for KeyGenerator" +
            " and for Key objects");
        }
        byte[] array1 = k.getEncoded();
        k = kg.generateKey();
        byte[] array2 = k.getEncoded();
        int matches = 0; 
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] == array2[i]) {
                matches++;
            }
        }
        if (matches > array1.length / 2) {
            throw new Exception("Generated keys are simular");
        }
        SecureRandom random = new SecureRandom();
        kg.init(random);
        matches = 0; 
        k = kg.generateKey();
        array1 = k.getEncoded();
        random = new SecureRandom();
        kg.init(random);
        k = kg.generateKey();
        array2 = k.getEncoded();
        for (int i = 0; i < array1.length; i++) {
            if (array1[i] == array2[i]) {
                matches++;
            }
        }
    }
}
