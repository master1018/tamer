public class SealedObject implements Serializable {
    static final long serialVersionUID = 4482838265551344752L;
    private byte[] encryptedContent = null;
    private String sealAlg = null;
    private String paramsAlg = null;
    protected byte[] encodedParams = null;
    public SealedObject(Serializable object, Cipher c) throws IOException,
        IllegalBlockSizeException
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutput a = new ObjectOutputStream(b);
        byte[] content;
        try {
            a.writeObject(object);
            a.flush();
            content = b.toByteArray();
        } finally {
            a.close();
        }
        try {
            this.encryptedContent = c.doFinal(content);
        }
        catch (BadPaddingException ex) {
        }
        if (c.getParameters() != null) {
            this.encodedParams = c.getParameters().getEncoded();
            this.paramsAlg = c.getParameters().getAlgorithm();
        }
        this.sealAlg = c.getAlgorithm();
    }
    protected SealedObject(SealedObject so) {
        this.encryptedContent = (byte[]) so.encryptedContent.clone();
        this.sealAlg = so.sealAlg;
        this.paramsAlg = so.paramsAlg;
        if (so.encodedParams != null) {
            this.encodedParams = (byte[]) so.encodedParams.clone();
        } else {
            this.encodedParams = null;
        }
    }
    public final String getAlgorithm() {
        return this.sealAlg;
    }
    public final Object getObject(Key key)
        throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
            InvalidKeyException
    {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            return unseal(key, null);
        } catch (NoSuchProviderException nspe) {
            throw new NoSuchAlgorithmException("algorithm not found");
        } catch (IllegalBlockSizeException ibse) {
            throw new InvalidKeyException(ibse.getMessage());
        } catch (BadPaddingException bpe) {
            throw new InvalidKeyException(bpe.getMessage());
        }
    }
    public final Object getObject(Cipher c)
        throws IOException, ClassNotFoundException, IllegalBlockSizeException,
            BadPaddingException
    {
        byte[] content = c.doFinal(this.encryptedContent);
        ByteArrayInputStream b = new ByteArrayInputStream(content);
        ObjectInput a = new extObjectInputStream(b);
        try {
            Object obj = a.readObject();
            return obj;
        } finally {
            a.close();
        }
    }
    public final Object getObject(Key key, String provider)
        throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeyException
    {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        try {
            return unseal(key, provider);
        } catch (IllegalBlockSizeException ibse) {
            throw new InvalidKeyException(ibse.getMessage());
        } catch (BadPaddingException bpe) {
            throw new InvalidKeyException(bpe.getMessage());
        }
    }
    private Object unseal(Key key, String provider)
        throws IOException, ClassNotFoundException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException
    {
        AlgorithmParameters params = null;
        if (this.encodedParams != null) {
            try {
                if (provider != null)
                    params = AlgorithmParameters.getInstance(this.paramsAlg,
                                                             provider);
                else
                    params = AlgorithmParameters.getInstance(this.paramsAlg);
            } catch (NoSuchProviderException nspe) {
                if (provider == null) {
                    throw new NoSuchAlgorithmException(this.paramsAlg
                                                       + " not found");
                } else {
                    throw new NoSuchProviderException(nspe.getMessage());
                }
            }
            params.init(this.encodedParams);
        }
        Cipher c;
        try {
            if (provider != null)
                c = Cipher.getInstance(this.sealAlg, provider);
            else
                c = Cipher.getInstance(this.sealAlg);
        } catch (NoSuchPaddingException nspe) {
            throw new NoSuchAlgorithmException("Padding that was used in "
                                               + "sealing operation not "
                                               + "available");
        } catch (NoSuchProviderException nspe) {
            if (provider == null) {
                throw new NoSuchAlgorithmException(this.sealAlg+" not found");
            } else {
                throw new NoSuchProviderException(nspe.getMessage());
            }
        }
        try {
            if (params != null)
                c.init(Cipher.DECRYPT_MODE, key, params);
            else
                c.init(Cipher.DECRYPT_MODE, key);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new RuntimeException(iape.getMessage());
        }
        byte[] content = c.doFinal(this.encryptedContent);
        ByteArrayInputStream b = new ByteArrayInputStream(content);
        ObjectInput a = new extObjectInputStream(b);
        try {
            Object obj = a.readObject();
            return obj;
        } finally {
            a.close();
        }
    }
    private void readObject(java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        if (encryptedContent != null)
            encryptedContent = (byte[])encryptedContent.clone();
        if (encodedParams != null)
            encodedParams = (byte[])encodedParams.clone();
    }
}
final class extObjectInputStream extends ObjectInputStream {
    private static ClassLoader systemClassLoader = null;
    extObjectInputStream(InputStream in)
        throws IOException, StreamCorruptedException {
        super(in);
    }
    protected Class resolveClass(ObjectStreamClass v)
        throws IOException, ClassNotFoundException
    {
        try {
            return super.resolveClass(v);
        } catch (ClassNotFoundException cnfe) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                if (systemClassLoader == null) {
                    systemClassLoader = ClassLoader.getSystemClassLoader();
                }
                loader = systemClassLoader;
                if (loader == null) {
                    throw new ClassNotFoundException(v.getName());
                }
            }
            return Class.forName(v.getName(), false, loader);
        }
    }
}
