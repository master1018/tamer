public class SealedObject implements Serializable {
    private static final long serialVersionUID = 4482838265551344752L;
    protected byte[] encodedParams;
    private byte[] encryptedContent;
    private String sealAlg;
    private String paramsAlg;
    private void readObject(ObjectInputStream s)
                throws IOException, ClassNotFoundException {
        encodedParams = (byte []) s.readUnshared();
        encryptedContent = (byte []) s.readUnshared();
        sealAlg = (String) s.readUnshared();
        paramsAlg = (String) s.readUnshared();
    }
    public SealedObject(Serializable object, Cipher c)
                throws IOException, IllegalBlockSizeException {
        if (c == null) {
            throw new NullPointerException(Messages.getString("crypto.13")); 
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            oos.flush();
            AlgorithmParameters ap = c.getParameters();
            this.encodedParams = (ap == null) ? null : ap.getEncoded();
            this.paramsAlg = (ap == null) ? null : ap.getAlgorithm();
            this.sealAlg = c.getAlgorithm();
            this.encryptedContent = c.doFinal(bos.toByteArray());
        } catch (BadPaddingException e) {
            throw new IOException(e.toString());
        }
    }
    protected SealedObject(SealedObject so) {
        if (so == null) {
            throw new NullPointerException(Messages.getString("crypto.14")); 
        }
        this.encryptedContent = so.encryptedContent;
        this.encodedParams = so.encodedParams;
        this.sealAlg = so.sealAlg;
        this.paramsAlg = so.paramsAlg;
    }
    public final String getAlgorithm() {
        return sealAlg;
    }
    public final Object getObject(Key key)
                throws IOException, ClassNotFoundException,
                       NoSuchAlgorithmException, InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException(
                    Messages.getString("crypto.05"));
        }
        try {
            Cipher cipher = Cipher.getInstance(sealAlg);
            if ((paramsAlg != null) && (paramsAlg.length() != 0)) {
                AlgorithmParameters params =
                    AlgorithmParameters.getInstance(paramsAlg);
                params.init(encodedParams);
                cipher.init(Cipher.DECRYPT_MODE, key, params);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            byte[] serialized = cipher.doFinal(encryptedContent);
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new ByteArrayInputStream(serialized));
            return ois.readObject();
        } catch (NoSuchPaddingException e)  {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (InvalidAlgorithmParameterException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (IllegalBlockSizeException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (BadPaddingException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (IllegalStateException  e) {
            throw new NoSuchAlgorithmException(e.toString());
        }
    }
    public final Object getObject(Cipher c)
                throws IOException, ClassNotFoundException,
                       IllegalBlockSizeException, BadPaddingException {
        if (c == null) {
            throw new NullPointerException(Messages.getString("crypto.13")); 
        }
        byte[] serialized = c.doFinal(encryptedContent);
        ObjectInputStream ois =
                new ObjectInputStream(
                        new ByteArrayInputStream(serialized));
        return ois.readObject();
    }
    public final Object getObject(Key key, String provider)
                throws IOException, ClassNotFoundException,
                       NoSuchAlgorithmException, NoSuchProviderException,
                       InvalidKeyException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(
                    Messages.getString("crypto.15")); 
        }
        try {
            Cipher cipher = Cipher.getInstance(sealAlg, provider);
            if ((paramsAlg != null) && (paramsAlg.length() != 0)) {
                AlgorithmParameters params =
                    AlgorithmParameters.getInstance(paramsAlg);
                params.init(encodedParams);
                cipher.init(Cipher.DECRYPT_MODE, key, params);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            byte[] serialized = cipher.doFinal(encryptedContent);
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new ByteArrayInputStream(serialized));
            return ois.readObject();
        } catch (NoSuchPaddingException e)  {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (InvalidAlgorithmParameterException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (IllegalBlockSizeException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (BadPaddingException e) {
            throw new NoSuchAlgorithmException(e.toString());
        } catch (IllegalStateException  e) {
            throw new NoSuchAlgorithmException(e.toString());
        }
    }
}
