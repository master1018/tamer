@TestTargetClass(SealedObject.class)
public class SealedObjectTest extends TestCase {
    class Mock_SealedObject extends SealedObject {
        public Mock_SealedObject(Serializable object, Cipher c)
                throws IOException, IllegalBlockSizeException {
            super(object, c);
        }
        public byte[] get_encodedParams() {
            return super.encodedParams;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "!Serialization",
        args = {}
    )
    public void testReadObject() throws Exception {
        String secret = "secret string";
        SealedObject so = new SealedObject(secret, new NullCipher());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(so);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                bos.toByteArray()));
        SealedObject so_des = (SealedObject) ois.readObject();
        assertEquals("The secret content of deserialized object "
                + "should be equal to the secret content of initial object",
                secret, so_des.getObject(new NullCipher()));
        assertEquals("The value returned by getAlgorithm() method of "
                + "deserialized object should be equal to the value returned "
                + "by getAlgorithm() method of initial object", so
                .getAlgorithm(), so_des.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "SealedObject",
        args = {java.io.Serializable.class, javax.crypto.Cipher.class}
    )
    public void testSealedObject1() throws Exception {
        String secret = "secret string";
        try {
            new SealedObject(secret, null);
            fail("NullPointerException should be thrown in the case "
                    + "of null cipher.");
        } catch (NullPointerException e) {
        }
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        Key key = kg.generateKey();
        IvParameterSpec ips = new IvParameterSpec(new byte[] {
                1, 2, 3, 4, 5, 6, 7, 8});
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        SealedObject so = new SealedObject(secret, cipher);
        cipher = Cipher.getInstance("DES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        try {
            new SealedObject(secret, cipher);
            fail("IllegalBlockSizeException expected");
        } catch (IllegalBlockSizeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SealedObject",
        args = {javax.crypto.SealedObject.class}
    )
    public void testSealedObject2() throws Exception {
        try {
            new SealedObject(null) {};
            fail("NullPointerException should be thrown in the case "
                    + "of null SealedObject.");
        } catch (NullPointerException e) {
        }
        String secret = "secret string";
        Cipher cipher = new NullCipher();
        SealedObject so1 = new SealedObject(secret, cipher);
        SealedObject so2 = new SealedObject(so1) {};
        assertEquals("The secret content of the object should equals "
                + "to the secret content of initial object.", secret, so2
                .getObject(cipher));
        assertEquals("The algorithm which was used to seal the object "
                + "should be the same as the algorithm used to seal the "
                + "initial object", so1.getAlgorithm(), so2.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void testGetAlgorithm() throws Exception {
        String secret = "secret string";
        String algorithm = "DES";
        KeyGenerator kg = KeyGenerator.getInstance(algorithm);
        Key key = kg.generateKey();
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        SealedObject so = new SealedObject(secret, cipher);
        assertEquals("The algorithm name should be the same as used "
                + "in cipher.", algorithm, so.getAlgorithm());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Not all exceptions can be checked.",
        method = "getObject",
        args = {java.security.Key.class}
    )
    public void testGetObject1() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        Key key = kg.generateKey();
        IvParameterSpec ips = new IvParameterSpec(new byte[] {
                1, 2, 3, 4, 5, 6, 7, 8});
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        String secret = "secret string";
        Mock_SealedObject so = new Mock_SealedObject(secret, cipher);
        assertEquals("The returned object does not equals to the "
                + "original object.", secret, so.getObject(key));
        assertTrue("The encodedParams field of SealedObject object "
                + "should contain the encoded algorithm parameters.", Arrays
                .equals(so.get_encodedParams(), cipher.getParameters()
                        .getEncoded()));
        try {
            so.getObject((Key)null);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Not all exceptions can be checked.",
        method = "getObject",
        args = {javax.crypto.Cipher.class}
    )
    public void testGetObject2() throws Exception {
        try {
            new SealedObject("secret string", new NullCipher())
                    .getObject((Cipher) null);
            fail("NullPointerException should be thrown in the case of "
                    + "null cipher.");
        } catch (NullPointerException e) {
        }
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        Key key = kg.generateKey();
        IvParameterSpec ips = new IvParameterSpec(new byte[] {
                1, 2, 3, 4, 5, 6, 7, 8});
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, ips);
        String secret = "secret string";
        SealedObject so = new SealedObject(secret, cipher);
        cipher.init(Cipher.DECRYPT_MODE, key, ips);
        assertEquals("The returned object does not equals to the "
                + "original object.", secret, so.getObject(cipher));
        try {
            so.getObject((Cipher)null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Not all exceptions can be checked.",
        method = "getObject",
        args = {java.security.Key.class, java.lang.String.class}
    )
    public void testGetObject3() throws Exception {
        try {
            new SealedObject("secret string", new NullCipher()).getObject(
                    new SecretKeySpec(new byte[] {0, 0, 0}, "algorithm"), null);
            fail("IllegalArgumentException should be thrown in the case of "
                    + "null provider.");
        } catch (IllegalArgumentException e) {
        }
        try {
            new SealedObject("secret string", new NullCipher()).getObject(
                    new SecretKeySpec(new byte[] {0, 0, 0}, "algorithm"), "");
            fail("IllegalArgumentException should be thrown in the case of "
                    + "empty provider.");
        } catch (IllegalArgumentException e) {
        }
        KeyGenerator kg = KeyGenerator.getInstance("DES");
        Key key = kg.generateKey();
        Cipher cipher = Cipher.getInstance("DES");
        String provider = cipher.getProvider().getName();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        String secret = "secret string";
        SealedObject so = new SealedObject(secret, cipher);
        cipher.init(Cipher.DECRYPT_MODE, key);
        assertEquals("The returned object does not equals to the "
                + "original object.", secret, so.getObject(key, provider));
        kg = KeyGenerator.getInstance("DESede");
        key = kg.generateKey();
        try {
            so.getObject(key, provider);
            fail("InvalidKeyException expected");
        } catch (InvalidKeyException e) {
        }
        try {
            so.getObject(key, "Wrong provider name");
            fail("NoSuchProviderException expected");
        } catch (NoSuchProviderException e) {
        }
    }
}
