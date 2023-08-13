public class SignatureFile {
    static final boolean debug = false;
    private Vector entries = new Vector();
    static final String[] hashes = {"SHA"};
    static final void debug(String s) {
        if (debug)
            System.out.println("sig> " + s);
    }
    private Manifest manifest;
    private String rawName;
    private PKCS7 signatureBlock;
    private SignatureFile(String name) throws JarException {
        entries = new Vector();
        if (name != null) {
            if (name.length() > 8 || name.indexOf('.') != -1) {
                throw new JarException("invalid file name");
            }
            rawName = name.toUpperCase(Locale.ENGLISH);
        }
    }
    private SignatureFile(String name, boolean newFile)
    throws JarException {
        this(name);
        if (newFile) {
            MessageHeader globals = new MessageHeader();
            globals.set("Signature-Version", "1.0");
            entries.addElement(globals);
        }
    }
    public SignatureFile(Manifest manifest, String name)
    throws JarException {
        this(name, true);
        this.manifest = manifest;
        Enumeration enum_ = manifest.entries();
        while (enum_.hasMoreElements()) {
            MessageHeader mh = (MessageHeader)enum_.nextElement();
            String entryName = mh.findValue("Name");
            if (entryName != null) {
                add(entryName);
            }
        }
    }
    public SignatureFile(Manifest manifest, String[] entries,
                         String filename)
    throws JarException {
        this(filename, true);
        this.manifest = manifest;
        add(entries);
    }
    public SignatureFile(InputStream is, String filename)
    throws IOException {
        this(filename);
        while (is.available() > 0) {
            MessageHeader m = new MessageHeader(is);
            entries.addElement(m);
        }
    }
    public SignatureFile(InputStream is) throws IOException {
        this(is, null);
    }
    public SignatureFile(byte[] bytes) throws IOException {
        this(new ByteArrayInputStream(bytes));
    }
    public String getName() {
        return "META-INF/" + rawName + ".SF";
    }
    public String getBlockName() {
        String suffix = "DSA";
        if (signatureBlock != null) {
            SignerInfo info = signatureBlock.getSignerInfos()[0];
            suffix = info.getDigestEncryptionAlgorithmId().getName();
            String temp = AlgorithmId.getEncAlgFromSigAlg(suffix);
            if (temp != null) suffix = temp;
        }
        return "META-INF/" + rawName + "." + suffix;
    }
    public PKCS7 getBlock() {
        return signatureBlock;
    }
    public void setBlock(PKCS7 block) {
        this.signatureBlock = block;
    }
    public void add(String[] entries) throws JarException {
        for (int i = 0; i < entries.length; i++) {
            add (entries[i]);
        }
    }
    public void add(String entry) throws JarException {
        MessageHeader mh = manifest.getEntry(entry);
        if (mh == null) {
            throw new JarException("entry " + entry + " not in manifest");
        }
        MessageHeader smh;
        try {
            smh = computeEntry(mh);
        } catch (IOException e) {
            throw new JarException(e.getMessage());
        }
        entries.addElement(smh);
    }
    public MessageHeader getEntry(String name) {
        Enumeration enum_ = entries();
        while(enum_.hasMoreElements()) {
            MessageHeader mh = (MessageHeader)enum_.nextElement();
            if (name.equals(mh.findValue("Name"))) {
                return mh;
            }
        }
        return null;
    }
    public MessageHeader entryAt(int n) {
        return (MessageHeader) entries.elementAt(n);
    }
    public Enumeration entries() {
        return entries.elements();
    }
    private MessageHeader computeEntry(MessageHeader mh) throws IOException {
        MessageHeader smh = new MessageHeader();
        String name = mh.findValue("Name");
        if (name == null) {
            return null;
        }
        smh.set("Name", name);
        BASE64Encoder encoder = new BASE64Encoder();
        try {
            for (int i = 0; i < hashes.length; ++i) {
                MessageDigest dig = getDigest(hashes[i]);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                mh.print(ps);
                byte[] headerBytes = baos.toByteArray();
                byte[] digest = dig.digest(headerBytes);
                smh.set(hashes[i] + "-Digest", encoder.encode(digest));
            }
            return smh;
        } catch (NoSuchAlgorithmException e) {
            throw new JarException(e.getMessage());
        }
    }
    private Hashtable digests = new Hashtable();
    private MessageDigest getDigest(String algorithm)
    throws NoSuchAlgorithmException {
        MessageDigest dig = (MessageDigest)digests.get(algorithm);
        if (dig == null) {
            dig = MessageDigest.getInstance(algorithm);
            digests.put(algorithm, dig);
        }
        dig.reset();
        return dig;
    }
    public void stream(OutputStream os) throws IOException {
        MessageHeader globals = (MessageHeader) entries.elementAt(0);
        if (globals.findValue("Signature-Version") == null) {
            throw new JarException("Signature file requires " +
                            "Signature-Version: 1.0 in 1st header");
        }
        PrintStream ps = new PrintStream(os);
        globals.print(ps);
        for (int i = 1; i < entries.size(); ++i) {
            MessageHeader mh = (MessageHeader) entries.elementAt(i);
            mh.print(ps);
        }
    }
}
