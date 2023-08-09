public final class SignedObject implements Serializable {
    private static final long serialVersionUID = 720502720485447167L;
    private byte[] content;
    private byte[] signature;
    private String thealgorithm;
    private void readObject(ObjectInputStream s) throws IOException,
            ClassNotFoundException {
        s.defaultReadObject();
        byte[] tmp = new byte[content.length];
        System.arraycopy(content, 0, tmp, 0, content.length);
        content = tmp;
        tmp = new byte[signature.length];
        System.arraycopy(signature, 0, tmp, 0, signature.length);
        signature = tmp;
    }
    public SignedObject(Serializable object, PrivateKey signingKey,
            Signature signingEngine) throws IOException, InvalidKeyException,
            SignatureException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(object);
            oos.flush();
        } finally {
            oos.close();
        }
        content = baos.toByteArray();
        signingEngine.initSign(signingKey);
        thealgorithm = signingEngine.getAlgorithm();
        signingEngine.update(content);
        signature = signingEngine.sign();
    }
    public Object getObject() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                content));
        try {
            return ois.readObject();
        } finally {
            ois.close();
        }
    }
    public byte[] getSignature() {
        byte[] sig = new byte[signature.length];
        System.arraycopy(signature, 0, sig, 0, signature.length);
        return sig;
    }
    public String getAlgorithm() {
        return thealgorithm;
    }
    public boolean verify(PublicKey verificationKey,
            Signature verificationEngine) throws InvalidKeyException,
            SignatureException {
        verificationEngine.initVerify(verificationKey);
        verificationEngine.update(content);
        return verificationEngine.verify(signature);
    }
}
