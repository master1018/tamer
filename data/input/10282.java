public class Correctness {
    public static void main(String[] args) throws Exception {
        String SIGALG = "SHA1withRSA";
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair kp = kpg.generateKeyPair();
        SignedObject so1 = new SignedObject("Hello", kp.getPrivate(),
                Signature.getInstance(SIGALG));
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(so1);
        out.close();
        byte[] data = byteOut.toByteArray();
        SignedObject so2 = (SignedObject)new ObjectInputStream(
                new ByteArrayInputStream(data)).readObject();
        if (!so2.getObject().equals("Hello")) {
            throw new Exception("Content changed");
        }
        if (!so2.getAlgorithm().equals(SIGALG)) {
            throw new Exception("Signature algorithm unknown");
        }
        if (!so2.verify(kp.getPublic(), Signature.getInstance(SIGALG))) {
            throw new Exception("Not verified");
        }
    }
}
