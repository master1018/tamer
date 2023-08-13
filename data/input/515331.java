class JarVerifier {
    private final String jarName;
    private Manifest man;
    private HashMap<String, byte[]> metaEntries = new HashMap<String, byte[]>(5);
    private final Hashtable<String, HashMap<String, Attributes>> signatures = new Hashtable<String, HashMap<String, Attributes>>(
            5);
    private final Hashtable<String, Certificate[]> certificates = new Hashtable<String, Certificate[]>(
            5);
    private final Hashtable<String, Certificate[]> verifiedEntries = new Hashtable<String, Certificate[]>();
    int mainAttributesEnd;
    class VerifierEntry extends OutputStream {
        private String name;
        private MessageDigest digest;
        private byte[] hash;
        private Certificate[] certificates;
        VerifierEntry(String name, MessageDigest digest, byte[] hash,
                Certificate[] certificates) {
            this.name = name;
            this.digest = digest;
            this.hash = hash;
            this.certificates = certificates;
        }
        @Override
        public void write(int value) {
            digest.update((byte) value);
        }
        @Override
        public void write(byte[] buf, int off, int nbytes) {
            digest.update(buf, off, nbytes);
        }
        void verify() {
            byte[] d = digest.digest();
            if (!MessageDigest.isEqual(d, Base64.decode(hash))) {
                throw new SecurityException(Messages.getString(
                        "archive.32", new Object[] { 
                        JarFile.MANIFEST_NAME, name, jarName }));
            }
            verifiedEntries.put(name, certificates);
        }
    }
    JarVerifier(String name) {
        jarName = name;
    }
    VerifierEntry initEntry(String name) {
        if (man == null || signatures.size() == 0) {
            return null;
        }
        Attributes attributes = man.getAttributes(name);
        if (attributes == null) {
            return null;
        }
        Vector<Certificate> certs = new Vector<Certificate>();
        Iterator<Map.Entry<String, HashMap<String, Attributes>>> it = signatures
                .entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, HashMap<String, Attributes>> entry = it.next();
            HashMap<String, Attributes> hm = entry.getValue();
            if (hm.get(name) != null) {
                String signatureFile = entry.getKey();
                Vector<Certificate> newCerts = getSignerCertificates(
                        signatureFile, certificates);
                Iterator<Certificate> iter = newCerts.iterator();
                while (iter.hasNext()) {
                    certs.add(iter.next());
                }
            }
        }
        if (certs.size() == 0) {
            return null;
        }
        Certificate[] certificatesArray = new Certificate[certs.size()];
        certs.toArray(certificatesArray);
        String algorithms = attributes.getValue("Digest-Algorithms"); 
        if (algorithms == null) {
            algorithms = "SHA SHA1"; 
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + "-Digest"); 
            if (hash == null) {
                continue;
            }
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO-8859-1"); 
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }
            try {
                return new VerifierEntry(name, OpenSSLMessageDigestJDK.getInstance(algorithm),
                        hashBytes, certificatesArray);
            } catch (NoSuchAlgorithmException e) {
            }
        }
        return null;
    }
    void addMetaEntry(String name, byte[] buf) {
        metaEntries.put(Util.toASCIIUpperCase(name), buf);
    }
    synchronized boolean readCertificates() {
        if (metaEntries == null) {
            return false;
        }
        Iterator<String> it = metaEntries.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.endsWith(".DSA") || key.endsWith(".RSA")) { 
                verifyCertificate(key);
                if (metaEntries == null) {
                    return false;
                }
                it.remove();
            }
        }
        return true;
    }
    private void verifyCertificate(String certFile) {
        String signatureFile = certFile.substring(0, certFile.lastIndexOf('.'))
                + ".SF"; 
        byte[] sfBytes = metaEntries.get(signatureFile);
        if (sfBytes == null) {
            return;
        }
        byte[] manifest = metaEntries.get(JarFile.MANIFEST_NAME);
        if (manifest == null) {
            return;
        }
        byte[] sBlockBytes = metaEntries.get(certFile);
        try {
            Certificate[] signerCertChain = JarUtils.verifySignature(
                    new ByteArrayInputStream(sfBytes),
                    new ByteArrayInputStream(sBlockBytes));
            if (null == metaEntries) {
                return;
            }
            if (signerCertChain != null) {
                certificates.put(signatureFile, signerCertChain);
            }
        } catch (IOException e) {
            return;
        } catch (GeneralSecurityException e) {
            throw new SecurityException(Messages.getString(
                    "archive.31", jarName, signatureFile)); 
        }
        Attributes attributes = new Attributes();
        HashMap<String, Attributes> entries = new HashMap<String, Attributes>();
        try {
            InitManifest im = new InitManifest(sfBytes, attributes, Attributes.Name.SIGNATURE_VERSION);
            im.initEntries(entries, null);
        } catch (IOException e) {
            return;
        }
        boolean createdBySigntool = false;
        String createdBy = attributes.getValue("Created-By"); 
        if (createdBy != null) {
            createdBySigntool = createdBy.indexOf("signtool") != -1; 
        }
        if (mainAttributesEnd > 0 && !createdBySigntool) {
            String digestAttribute = "-Digest-Manifest-Main-Attributes"; 
            if (!verify(attributes, digestAttribute, manifest, 0,
                    mainAttributesEnd, false, true)) {
                throw new SecurityException(Messages.getString(
                        "archive.31", jarName, signatureFile)); 
            }
        }
        String digestAttribute = createdBySigntool ? "-Digest" 
                : "-Digest-Manifest"; 
        if (!verify(attributes, digestAttribute, manifest, 0, manifest.length,
                false, false)) {
            Iterator<Map.Entry<String, Attributes>> it = entries.entrySet()
                    .iterator();
            while (it.hasNext()) {
                Map.Entry<String, Attributes> entry = it.next();
                Manifest.Chunk chunk = man.getChunk(entry.getKey());
                if (chunk == null) {
                    return;
                }
                if (!verify(entry.getValue(), "-Digest", manifest, 
                        chunk.start, chunk.end, createdBySigntool, false)) {
                    throw new SecurityException(Messages.getString(
                            "archive.32", 
                            new Object[] { signatureFile, entry.getKey(),
                                    jarName }));
                }
            }
        }
        metaEntries.put(signatureFile, null);
        signatures.put(signatureFile, entries);
    }
    void setManifest(Manifest mf) {
        man = mf;
    }
    boolean isSignedJar() {
        return certificates.size() > 0;
    }
    private boolean verify(Attributes attributes, String entry, byte[] data,
            int start, int end, boolean ignoreSecondEndline, boolean ignorable) {
        String algorithms = attributes.getValue("Digest-Algorithms"); 
        if (algorithms == null) {
            algorithms = "SHA SHA1"; 
        }
        StringTokenizer tokens = new StringTokenizer(algorithms);
        while (tokens.hasMoreTokens()) {
            String algorithm = tokens.nextToken();
            String hash = attributes.getValue(algorithm + entry);
            if (hash == null) {
                continue;
            }
            MessageDigest md;
            try {
                md = OpenSSLMessageDigestJDK.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
            if (ignoreSecondEndline && data[end - 1] == '\n'
                    && data[end - 2] == '\n') {
                md.update(data, start, end - 1 - start);
            } else {
                md.update(data, start, end - start);
            }
            byte[] b = md.digest();
            byte[] hashBytes;
            try {
                hashBytes = hash.getBytes("ISO-8859-1"); 
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e.toString());
            }
            return MessageDigest.isEqual(b, Base64.decode(hashBytes));
        }
        return ignorable;
    }
    Certificate[] getCertificates(String name) {
        Certificate[] verifiedCerts = verifiedEntries.get(name);
        if (verifiedCerts == null) {
            return null;
        }
        return verifiedCerts.clone();
    }
    void removeMetaEntries() {
        metaEntries = null;
    }
    public static Vector<Certificate> getSignerCertificates(
            String signatureFileName, Map<String, Certificate[]> certificates) {
        Vector<Certificate> result = new Vector<Certificate>();
        Certificate[] certChain = certificates.get(signatureFileName);
        if (certChain != null) {
            for (Certificate element : certChain) {
                result.add(element);
            }
        }
        return result;
    }
}
