public class KeyRep implements Serializable {
    private static final long serialVersionUID = -4757683898830641853L;
    private final Type type;
    private final String algorithm;
    private final String format;
    private byte[] encoded;
    public KeyRep(Type type,
            String algorithm, String format, byte[] encoded) {
        this.type = type;
        this.algorithm = algorithm;
        this.format = format;
        this.encoded = encoded;
        if(this.type == null) {
            throw new NullPointerException(Messages.getString("security.07")); 
        }
        if(this.algorithm == null) {
            throw new NullPointerException(Messages.getString("security.08")); 
        }
        if(this.format == null) {
            throw new NullPointerException(Messages.getString("security.09")); 
        }
        if(this.encoded == null) {
            throw new NullPointerException(Messages.getString("security.0A")); 
        }
    }
    protected Object readResolve() throws ObjectStreamException {
        switch (type) {
        case SECRET:
            if ("RAW".equals(format)) { 
                try {
                    return new SecretKeySpec(encoded, algorithm);
                } catch (IllegalArgumentException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0B", e)); 
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); 
        case PUBLIC:
            if ("X.509".equals(format)) { 
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    return kf.generatePublic(new X509EncodedKeySpec(encoded));
                } catch (NoSuchAlgorithmException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); 
                }
                catch (InvalidKeySpecException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); 
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); 
        case PRIVATE:
            if ("PKCS#8".equals(format)) { 
                try {
                    KeyFactory kf = KeyFactory.getInstance(algorithm);
                    return kf.generatePrivate(new PKCS8EncodedKeySpec(encoded));
                } catch (NoSuchAlgorithmException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); 
                }
                catch (InvalidKeySpecException e) {
                    throw new NotSerializableException(
                            Messages.getString("security.0D", e)); 
                }
            }
            throw new NotSerializableException(
                Messages.getString("security.0C", type, format)); 
        }
        throw new NotSerializableException(Messages.getString("security.0E", type)); 
    }
    private void readObject(ObjectInputStream is)
        throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        byte[] new_encoded = new byte[encoded.length];
        System.arraycopy(encoded, 0, new_encoded, 0, new_encoded.length);
        encoded = new_encoded;    
    }
    public static enum Type {
        SECRET,
        PUBLIC,
        PRIVATE
    }
}
