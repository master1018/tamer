abstract class JavaKeyStore extends KeyStoreSpi {
    public static final class JKS extends JavaKeyStore {
        String convertAlias(String alias) {
            return alias.toLowerCase();
        }
    }
    public static final class CaseExactJKS extends JavaKeyStore {
        String convertAlias(String alias) {
            return alias;
        }
    }
    private static final int MAGIC = 0xfeedfeed;
    private static final int VERSION_1 = 0x01;
    private static final int VERSION_2 = 0x02;
    private static class KeyEntry {
        Date date; 
        byte[] protectedPrivKey;
        Certificate chain[];
    };
    private static class TrustedCertEntry {
        Date date; 
        Certificate cert;
    };
    private final Hashtable<String, Object> entries;
    JavaKeyStore() {
        entries = new Hashtable<String, Object>();
    }
    abstract String convertAlias(String alias);
    public Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        Object entry = entries.get(convertAlias(alias));
        if (entry == null || !(entry instanceof KeyEntry)) {
            return null;
        }
        if (password == null) {
            throw new UnrecoverableKeyException("Password must not be null");
        }
        KeyProtector keyProtector = new KeyProtector(password);
        byte[] encrBytes = ((KeyEntry)entry).protectedPrivKey;
        EncryptedPrivateKeyInfo encrInfo;
        byte[] plain;
        try {
            encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
        } catch (IOException ioe) {
            throw new UnrecoverableKeyException("Private key not stored as "
                                                + "PKCS #8 "
                                                + "EncryptedPrivateKeyInfo");
        }
        return keyProtector.recover(encrInfo);
    }
    public Certificate[] engineGetCertificateChain(String alias) {
        Object entry = entries.get(convertAlias(alias));
        if (entry != null && entry instanceof KeyEntry) {
            if (((KeyEntry)entry).chain == null) {
                return null;
            } else {
                return ((KeyEntry)entry).chain.clone();
            }
        } else {
            return null;
        }
    }
    public Certificate engineGetCertificate(String alias) {
        Object entry = entries.get(convertAlias(alias));
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return ((TrustedCertEntry)entry).cert;
            } else {
                if (((KeyEntry)entry).chain == null) {
                    return null;
                } else {
                    return ((KeyEntry)entry).chain[0];
                }
            }
        } else {
            return null;
        }
    }
    public Date engineGetCreationDate(String alias) {
        Object entry = entries.get(convertAlias(alias));
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                return new Date(((TrustedCertEntry)entry).date.getTime());
            } else {
                return new Date(((KeyEntry)entry).date.getTime());
            }
        } else {
            return null;
        }
    }
    public void engineSetKeyEntry(String alias, Key key, char[] password,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        KeyProtector keyProtector = null;
        if (!(key instanceof java.security.PrivateKey)) {
            throw new KeyStoreException("Cannot store non-PrivateKeys");
        }
        try {
            synchronized(entries) {
                KeyEntry entry = new KeyEntry();
                entry.date = new Date();
                keyProtector = new KeyProtector(password);
                entry.protectedPrivKey = keyProtector.protect(key);
                if ((chain != null) &&
                    (chain.length != 0)) {
                    entry.chain = chain.clone();
                } else {
                    entry.chain = null;
                }
                entries.put(convertAlias(alias), entry);
            }
        } catch (NoSuchAlgorithmException nsae) {
            throw new KeyStoreException("Key protection algorithm not found");
        } finally {
            keyProtector = null;
        }
    }
    public void engineSetKeyEntry(String alias, byte[] key,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        synchronized(entries) {
            try {
                new EncryptedPrivateKeyInfo(key);
            } catch (IOException ioe) {
                throw new KeyStoreException("key is not encoded as "
                                            + "EncryptedPrivateKeyInfo");
            }
            KeyEntry entry = new KeyEntry();
            entry.date = new Date();
            entry.protectedPrivKey = key.clone();
            if ((chain != null) &&
                (chain.length != 0)) {
                entry.chain = chain.clone();
            } else {
                entry.chain = null;
            }
            entries.put(convertAlias(alias), entry);
        }
    }
    public void engineSetCertificateEntry(String alias, Certificate cert)
        throws KeyStoreException
    {
        synchronized(entries) {
            Object entry = entries.get(convertAlias(alias));
            if ((entry != null) && (entry instanceof KeyEntry)) {
                throw new KeyStoreException
                    ("Cannot overwrite own certificate");
            }
            TrustedCertEntry trustedCertEntry = new TrustedCertEntry();
            trustedCertEntry.cert = cert;
            trustedCertEntry.date = new Date();
            entries.put(convertAlias(alias), trustedCertEntry);
        }
    }
    public void engineDeleteEntry(String alias)
        throws KeyStoreException
    {
        synchronized(entries) {
            entries.remove(convertAlias(alias));
        }
    }
    public Enumeration<String> engineAliases() {
        return entries.keys();
    }
    public boolean engineContainsAlias(String alias) {
        return entries.containsKey(convertAlias(alias));
    }
    public int engineSize() {
        return entries.size();
    }
    public boolean engineIsKeyEntry(String alias) {
        Object entry = entries.get(convertAlias(alias));
        if ((entry != null) && (entry instanceof KeyEntry)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean engineIsCertificateEntry(String alias) {
        Object entry = entries.get(convertAlias(alias));
        if ((entry != null) && (entry instanceof TrustedCertEntry)) {
            return true;
        } else {
            return false;
        }
    }
    public String engineGetCertificateAlias(Certificate cert) {
        Certificate certElem;
        for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            Object entry = entries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                certElem = ((TrustedCertEntry)entry).cert;
            } else if (((KeyEntry)entry).chain != null) {
                certElem = ((KeyEntry)entry).chain[0];
            } else {
                continue;
            }
            if (certElem.equals(cert)) {
                return alias;
            }
        }
        return null;
    }
    public void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        synchronized(entries) {
            if (password == null) {
                throw new IllegalArgumentException("password can't be null");
            }
            byte[] encoded; 
            MessageDigest md = getPreKeyedHash(password);
            DataOutputStream dos
                = new DataOutputStream(new DigestOutputStream(stream, md));
            dos.writeInt(MAGIC);
            dos.writeInt(VERSION_2);
            dos.writeInt(entries.size());
            for (Enumeration<String> e = entries.keys(); e.hasMoreElements();) {
                String alias = e.nextElement();
                Object entry = entries.get(alias);
                if (entry instanceof KeyEntry) {
                    dos.writeInt(1);
                    dos.writeUTF(alias);
                    dos.writeLong(((KeyEntry)entry).date.getTime());
                    dos.writeInt(((KeyEntry)entry).protectedPrivKey.length);
                    dos.write(((KeyEntry)entry).protectedPrivKey);
                    int chainLen;
                    if (((KeyEntry)entry).chain == null) {
                        chainLen = 0;
                    } else {
                        chainLen = ((KeyEntry)entry).chain.length;
                    }
                    dos.writeInt(chainLen);
                    for (int i = 0; i < chainLen; i++) {
                        encoded = ((KeyEntry)entry).chain[i].getEncoded();
                        dos.writeUTF(((KeyEntry)entry).chain[i].getType());
                        dos.writeInt(encoded.length);
                        dos.write(encoded);
                    }
                } else {
                    dos.writeInt(2);
                    dos.writeUTF(alias);
                    dos.writeLong(((TrustedCertEntry)entry).date.getTime());
                    encoded = ((TrustedCertEntry)entry).cert.getEncoded();
                    dos.writeUTF(((TrustedCertEntry)entry).cert.getType());
                    dos.writeInt(encoded.length);
                    dos.write(encoded);
                }
            }
            byte digest[] = md.digest();
            dos.write(digest);
            dos.flush();
        }
    }
    public void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        synchronized(entries) {
            DataInputStream dis;
            MessageDigest md = null;
            CertificateFactory cf = null;
            Hashtable<String, CertificateFactory> cfs = null;
            ByteArrayInputStream bais = null;
            byte[] encoded = null;
            if (stream == null)
                return;
            if (password != null) {
                md = getPreKeyedHash(password);
                dis = new DataInputStream(new DigestInputStream(stream, md));
            } else {
                dis = new DataInputStream(stream);
            }
            int xMagic = dis.readInt();
            int xVersion = dis.readInt();
            if (xMagic!=MAGIC ||
                (xVersion!=VERSION_1 && xVersion!=VERSION_2)) {
                throw new IOException("Invalid keystore format");
            }
            if (xVersion == VERSION_1) {
                cf = CertificateFactory.getInstance("X509");
            } else {
                cfs = new Hashtable<String, CertificateFactory>(3);
            }
            entries.clear();
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                int tag;
                String alias;
                tag = dis.readInt();
                if (tag == 1) { 
                    KeyEntry entry = new KeyEntry();
                    alias = dis.readUTF();
                    entry.date = new Date(dis.readLong());
                    entry.protectedPrivKey =
                            IOUtils.readFully(dis, dis.readInt(), true);
                    int numOfCerts = dis.readInt();
                    if (numOfCerts > 0) {
                        List<Certificate> certs = new ArrayList<>(
                                numOfCerts > 10 ? 10 : numOfCerts);
                        for (int j = 0; j < numOfCerts; j++) {
                            if (xVersion == 2) {
                                String certType = dis.readUTF();
                                if (cfs.containsKey(certType)) {
                                    cf = cfs.get(certType);
                                } else {
                                    cf = CertificateFactory.getInstance(certType);
                                    cfs.put(certType, cf);
                                }
                            }
                            encoded = IOUtils.readFully(dis, dis.readInt(), true);
                            bais = new ByteArrayInputStream(encoded);
                            certs.add(cf.generateCertificate(bais));
                            bais.close();
                        }
                        entry.chain = certs.toArray(new Certificate[numOfCerts]);
                    }
                    entries.put(alias, entry);
                } else if (tag == 2) { 
                    TrustedCertEntry entry = new TrustedCertEntry();
                    alias = dis.readUTF();
                    entry.date = new Date(dis.readLong());
                    if (xVersion == 2) {
                        String certType = dis.readUTF();
                        if (cfs.containsKey(certType)) {
                            cf = cfs.get(certType);
                        } else {
                            cf = CertificateFactory.getInstance(certType);
                            cfs.put(certType, cf);
                        }
                    }
                    encoded = IOUtils.readFully(dis, dis.readInt(), true);
                    bais = new ByteArrayInputStream(encoded);
                    entry.cert = cf.generateCertificate(bais);
                    bais.close();
                    entries.put(alias, entry);
                } else {
                    throw new IOException("Unrecognized keystore entry");
                }
            }
            if (password != null) {
                byte computed[], actual[];
                computed = md.digest();
                actual = new byte[computed.length];
                dis.readFully(actual);
                for (int i = 0; i < computed.length; i++) {
                    if (computed[i] != actual[i]) {
                        Throwable t = new UnrecoverableKeyException
                            ("Password verification failed");
                        throw (IOException)new IOException
                            ("Keystore was tampered with, or "
                            + "password was incorrect").initCause(t);
                    }
                }
            }
        }
    }
    private MessageDigest getPreKeyedHash(char[] password)
        throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        int i, j;
        MessageDigest md = MessageDigest.getInstance("SHA");
        byte[] passwdBytes = new byte[password.length * 2];
        for (i=0, j=0; i<password.length; i++) {
            passwdBytes[j++] = (byte)(password[i] >> 8);
            passwdBytes[j++] = (byte)password[i];
        }
        md.update(passwdBytes);
        for (i=0; i<passwdBytes.length; i++)
            passwdBytes[i] = 0;
        md.update("Mighty Aphrodite".getBytes("UTF8"));
        return md;
    }
}
