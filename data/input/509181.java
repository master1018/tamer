public class SignedJarBuilder {
    private static final String DIGEST_ALGORITHM = "SHA1";
    private static final String DIGEST_ATTR = "SHA1-Digest";
    private static final String DIGEST_MANIFEST_ATTR = "SHA1-Digest-Manifest";
    private static class SignatureOutputStream extends FilterOutputStream {
        private Signature mSignature;
        public SignatureOutputStream(OutputStream out, Signature sig) {
            super(out);
            mSignature = sig;
        }
        @Override
        public void write(int b) throws IOException {
            try {
                mSignature.update((byte) b);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            super.write(b);
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            try {
                mSignature.update(b, off, len);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            super.write(b, off, len);
        }
    }
    private JarOutputStream mOutputJar;
    private PrivateKey mKey;
    private X509Certificate mCertificate;
    private Manifest mManifest;
    private BASE64Encoder mBase64Encoder;
    private MessageDigest mMessageDigest;
    private byte[] mBuffer = new byte[4096];
    public interface IZipEntryFilter {
        public boolean checkEntry(String name);
    }
    public SignedJarBuilder(OutputStream out, PrivateKey key, X509Certificate certificate)
            throws IOException, NoSuchAlgorithmException {
        mOutputJar = new JarOutputStream(out);
        mOutputJar.setLevel(9);
        mKey = key;
        mCertificate = certificate;
        if (mKey != null && mCertificate != null) {
            mManifest = new Manifest();
            Attributes main = mManifest.getMainAttributes();
            main.putValue("Manifest-Version", "1.0");
            main.putValue("Created-By", "1.0 (Android)");
            mBase64Encoder = new BASE64Encoder();
            mMessageDigest = MessageDigest.getInstance(DIGEST_ALGORITHM);
        }
    }
    public void writeFile(File inputFile, String jarPath) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        try {
            JarEntry entry = new JarEntry(jarPath);
            entry.setTime(inputFile.lastModified());
            writeEntry(fis, entry);
        } finally {
            fis.close();
        }
    }
    public void writeZip(InputStream input, IZipEntryFilter filter) throws IOException {
        ZipInputStream zis = new ZipInputStream(input);
        try {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (entry.isDirectory() || name.startsWith("META-INF/")) {
                    continue;
                }
                if (filter != null && filter.checkEntry(name) == false) {
                    continue;
                }
                JarEntry newEntry;
                if (entry.getMethod() == JarEntry.STORED) {
                    newEntry = new JarEntry(entry);
                } else {
                    newEntry = new JarEntry(name);
                }
                writeEntry(zis, newEntry);
                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }
    public void close() throws IOException, GeneralSecurityException {
        if (mManifest != null) {
            mOutputJar.putNextEntry(new JarEntry(JarFile.MANIFEST_NAME));
            mManifest.write(mOutputJar);
            Signature signature = Signature.getInstance("SHA1with" + mKey.getAlgorithm());
            signature.initSign(mKey);
            mOutputJar.putNextEntry(new JarEntry("META-INF/CERT.SF"));
            writeSignatureFile(new SignatureOutputStream(mOutputJar, signature));
            mOutputJar.putNextEntry(new JarEntry("META-INF/CERT." + mKey.getAlgorithm()));
            writeSignatureBlock(signature, mCertificate, mKey);
        }
        mOutputJar.close();
    }
    private void writeEntry(InputStream input, JarEntry entry) throws IOException {
        mOutputJar.putNextEntry(entry);
        int count; 
        while ((count = input.read(mBuffer)) != -1) {
            mOutputJar.write(mBuffer, 0, count);
            if (mMessageDigest != null) {
                mMessageDigest.update(mBuffer, 0, count);
            }
        }
        mOutputJar.closeEntry();
        if (mManifest != null) {
            Attributes attr = mManifest.getAttributes(entry.getName());
            if (attr == null) {
                attr = new Attributes();
                mManifest.getEntries().put(entry.getName(), attr);
            }
            attr.putValue(DIGEST_ATTR, mBase64Encoder.encode(mMessageDigest.digest()));
        }
    }
    private void writeSignatureFile(OutputStream out)
            throws IOException, GeneralSecurityException {
        Manifest sf = new Manifest();
        Attributes main = sf.getMainAttributes();
        main.putValue("Signature-Version", "1.0");
        main.putValue("Created-By", "1.0 (Android)");
        BASE64Encoder base64 = new BASE64Encoder();
        MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
        PrintStream print = new PrintStream(
                new DigestOutputStream(new ByteArrayOutputStream(), md),
                true, "UTF-8");
        mManifest.write(print);
        print.flush();
        main.putValue(DIGEST_MANIFEST_ATTR, base64.encode(md.digest()));
        Map<String, Attributes> entries = mManifest.getEntries();
        for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
            print.print("Name: " + entry.getKey() + "\r\n");
            for (Map.Entry<Object, Object> att : entry.getValue().entrySet()) {
                print.print(att.getKey() + ": " + att.getValue() + "\r\n");
            }
            print.print("\r\n");
            print.flush();
            Attributes sfAttr = new Attributes();
            sfAttr.putValue(DIGEST_ATTR, base64.encode(md.digest()));
            sf.getEntries().put(entry.getKey(), sfAttr);
        }
        sf.write(out);
    }
    private void writeSignatureBlock(Signature signature, X509Certificate publicKey,
            PrivateKey privateKey)
            throws IOException, GeneralSecurityException {
        SignerInfo signerInfo = new SignerInfo(
                new X500Name(publicKey.getIssuerX500Principal().getName()),
                publicKey.getSerialNumber(),
                AlgorithmId.get(DIGEST_ALGORITHM),
                AlgorithmId.get(privateKey.getAlgorithm()),
                signature.sign());
        PKCS7 pkcs7 = new PKCS7(
                new AlgorithmId[] { AlgorithmId.get(DIGEST_ALGORITHM) },
                new ContentInfo(ContentInfo.DATA_OID, null),
                new X509Certificate[] { publicKey },
                new SignerInfo[] { signerInfo });
        pkcs7.encodeSignedData(mOutputJar);
    }
}
