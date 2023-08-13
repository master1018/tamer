public abstract class MessageDigest extends MessageDigestSpi {
    private String algorithm;
    private static final int INITIAL = 0;
    private static final int IN_PROGRESS = 1;
    private int state = INITIAL;
    private Provider provider;
    protected MessageDigest(String algorithm) {
        this.algorithm = algorithm;
    }
    public static MessageDigest getInstance(String algorithm)
    throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm, "MessageDigest",
                                             (String)null);
            if (objs[0] instanceof MessageDigest) {
                MessageDigest md = (MessageDigest)objs[0];
                md.provider = (Provider)objs[1];
                return md;
            } else {
                MessageDigest delegate =
                    new Delegate((MessageDigestSpi)objs[0], algorithm);
                delegate.provider = (Provider)objs[1];
                return delegate;
            }
        } catch(NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        }
    }
    public static MessageDigest getInstance(String algorithm, String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
        if (objs[0] instanceof MessageDigest) {
            MessageDigest md = (MessageDigest)objs[0];
            md.provider = (Provider)objs[1];
            return md;
        } else {
            MessageDigest delegate =
                new Delegate((MessageDigestSpi)objs[0], algorithm);
            delegate.provider = (Provider)objs[1];
            return delegate;
        }
    }
    public static MessageDigest getInstance(String algorithm,
                                            Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = Security.getImpl(algorithm, "MessageDigest", provider);
        if (objs[0] instanceof MessageDigest) {
            MessageDigest md = (MessageDigest)objs[0];
            md.provider = (Provider)objs[1];
            return md;
        } else {
            MessageDigest delegate =
                new Delegate((MessageDigestSpi)objs[0], algorithm);
            delegate.provider = (Provider)objs[1];
            return delegate;
        }
    }
    public final Provider getProvider() {
        return this.provider;
    }
    public void update(byte input) {
        engineUpdate(input);
        state = IN_PROGRESS;
    }
    public void update(byte[] input, int offset, int len) {
        if (input == null) {
            throw new IllegalArgumentException("No input buffer given");
        }
        if (input.length - offset < len) {
            throw new IllegalArgumentException("Input buffer too short");
        }
        engineUpdate(input, offset, len);
        state = IN_PROGRESS;
    }
    public void update(byte[] input) {
        engineUpdate(input, 0, input.length);
        state = IN_PROGRESS;
    }
    public final void update(ByteBuffer input) {
        if (input == null) {
            throw new NullPointerException();
        }
        engineUpdate(input);
        state = IN_PROGRESS;
    }
    public byte[] digest() {
        byte[] result = engineDigest();
        state = INITIAL;
        return result;
    }
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null) {
            throw new IllegalArgumentException("No output buffer given");
        }
        if (buf.length - offset < len) {
            throw new IllegalArgumentException
                ("Output buffer too small for specified offset and length");
        }
        int numBytes = engineDigest(buf, offset, len);
        state = INITIAL;
        return numBytes;
    }
    public byte[] digest(byte[] input) {
        update(input);
        return digest();
    }
    public String toString() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        p.print(algorithm+" Message Digest from "+provider.getName()+", ");
        switch (state) {
        case INITIAL:
            p.print("<initialized>");
            break;
        case IN_PROGRESS:
            p.print("<in progress>");
            break;
        }
        p.println();
        return (baos.toString());
    }
    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta.length != digestb.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < digesta.length; i++) {
            result |= digesta[i] ^ digestb[i];
        }
        return result == 0;
    }
    public void reset() {
        engineReset();
        state = INITIAL;
    }
    public final String getAlgorithm() {
        return this.algorithm;
    }
    public final int getDigestLength() {
        int digestLen = engineGetDigestLength();
        if (digestLen == 0) {
            try {
                MessageDigest md = (MessageDigest)clone();
                byte[] digest = md.digest();
                return digest.length;
            } catch (CloneNotSupportedException e) {
                return digestLen;
            }
        }
        return digestLen;
    }
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
    static class Delegate extends MessageDigest {
        private MessageDigestSpi digestSpi;
        public Delegate(MessageDigestSpi digestSpi, String algorithm) {
            super(algorithm);
            this.digestSpi = digestSpi;
        }
        public Object clone() throws CloneNotSupportedException {
            if (digestSpi instanceof Cloneable) {
                MessageDigestSpi digestSpiClone =
                    (MessageDigestSpi)digestSpi.clone();
                MessageDigest that =
                    new Delegate(digestSpiClone,
                                 ((MessageDigest)this).algorithm);
                that.provider = ((MessageDigest)this).provider;
                that.state = ((MessageDigest)this).state;
                return that;
            } else {
                throw new CloneNotSupportedException();
            }
        }
        protected int engineGetDigestLength() {
            return digestSpi.engineGetDigestLength();
        }
        protected void engineUpdate(byte input) {
            digestSpi.engineUpdate(input);
        }
        protected void engineUpdate(byte[] input, int offset, int len) {
            digestSpi.engineUpdate(input, offset, len);
        }
        protected void engineUpdate(ByteBuffer input) {
            digestSpi.engineUpdate(input);
        }
        protected byte[] engineDigest() {
            return digestSpi.engineDigest();
        }
        protected int engineDigest(byte[] buf, int offset, int len)
            throws DigestException {
                return digestSpi.engineDigest(buf, offset, len);
        }
        protected void engineReset() {
            digestSpi.engineReset();
        }
    }
}
