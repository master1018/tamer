public final class JceKeyStore extends KeyStoreSpi {
    private static final int JCEKS_MAGIC = 0xcececece;
    private static final int JKS_MAGIC = 0xfeedfeed;
    private static final int VERSION_1 = 0x01;
    private static final int VERSION_2 = 0x02;
    private static final class PrivateKeyEntry {
        Date date; 
        byte[] protectedKey;
        Certificate chain[];
    };
    private static final class SecretKeyEntry {
        Date date; 
        SealedObject sealedKey;
    }
    private static final class TrustedCertEntry {
        Date date; 
        Certificate cert;
    };
    private Hashtable entries = new Hashtable();
    public Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        Key key = null;
        Object entry = entries.get(alias.toLowerCase());
        if (!((entry instanceof PrivateKeyEntry) ||
              (entry instanceof SecretKeyEntry))) {
            return null;
        }
        KeyProtector keyProtector = new KeyProtector(password);
        if (entry instanceof PrivateKeyEntry) {
            byte[] encrBytes = ((PrivateKeyEntry)entry).protectedKey;
            EncryptedPrivateKeyInfo encrInfo;
            try {
                encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
            } catch (IOException ioe) {
                throw new UnrecoverableKeyException("Private key not stored "
                                                    + "as PKCS #8 " +
                                                    "EncryptedPrivateKeyInfo");
            }
            key = keyProtector.recover(encrInfo);
        } else {
            key =
                keyProtector.unseal(((SecretKeyEntry)entry).sealedKey);
        }
        return key;
    }
    public Certificate[] engineGetCertificateChain(String alias)
    {
        Certificate[] chain = null;
        Object entry = entries.get(alias.toLowerCase());
        if ((entry instanceof PrivateKeyEntry)
            && (((PrivateKeyEntry)entry).chain != null)) {
            chain = (Certificate[])((PrivateKeyEntry)entry).chain.clone();
        }
        return chain;
    }
    public Certificate engineGetCertificate(String alias) {
        Certificate cert = null;
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                cert = ((TrustedCertEntry)entry).cert;
            } else if ((entry instanceof PrivateKeyEntry) &&
                       (((PrivateKeyEntry)entry).chain != null)) {
                cert = ((PrivateKeyEntry)entry).chain[0];
            }
        }
        return cert;
    }
    public Date engineGetCreationDate(String alias) {
        Date date = null;
        Object entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry instanceof TrustedCertEntry) {
                date = new Date(((TrustedCertEntry)entry).date.getTime());
            } else if (entry instanceof PrivateKeyEntry) {
                date = new Date(((PrivateKeyEntry)entry).date.getTime());
            } else {
                date = new Date(((SecretKeyEntry)entry).date.getTime());
            }
        }
        return date;
    }
    public void engineSetKeyEntry(String alias, Key key, char[] password,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        synchronized(entries) {
            try {
                KeyProtector keyProtector = new KeyProtector(password);
                if (key instanceof PrivateKey) {
                    PrivateKeyEntry entry = new PrivateKeyEntry();
                    entry.date = new Date();
                    entry.protectedKey = keyProtector.protect((PrivateKey)key);
                    if ((chain != null) &&
                        (chain.length !=0)) {
                        entry.chain = (Certificate[])chain.clone();
                    } else {
                        entry.chain = null;
                    }
                    entries.put(alias.toLowerCase(), entry);
                } else {
                    SecretKeyEntry entry = new SecretKeyEntry();
                    entry.date = new Date();
                    entry.sealedKey = keyProtector.seal(key);
                    entries.put(alias.toLowerCase(), entry);
                }
            } catch (Exception e) {
                throw new KeyStoreException(e.getMessage());
            }
        }
    }
    public void engineSetKeyEntry(String alias, byte[] key,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        synchronized(entries) {
            PrivateKeyEntry entry = new PrivateKeyEntry();
            entry.date = new Date();
            entry.protectedKey = (byte[])key.clone();
            if ((chain != null) &&
                (chain.length != 0)) {
                entry.chain = (Certificate[])chain.clone();
            } else {
                entry.chain = null;
            }
            entries.put(alias.toLowerCase(), entry);
        }
    }
    public void engineSetCertificateEntry(String alias, Certificate cert)
        throws KeyStoreException
    {
        synchronized(entries) {
            Object entry = entries.get(alias.toLowerCase());
            if (entry != null) {
                if (entry instanceof PrivateKeyEntry) {
                    throw new KeyStoreException("Cannot overwrite own "
                                                + "certificate");
                } else if (entry instanceof SecretKeyEntry) {
                    throw new KeyStoreException("Cannot overwrite secret key");
                }
            }
            TrustedCertEntry trustedCertEntry = new TrustedCertEntry();
            trustedCertEntry.cert = cert;
            trustedCertEntry.date = new Date();
            entries.put(alias.toLowerCase(), trustedCertEntry);
        }
    }
    public void engineDeleteEntry(String alias)
        throws KeyStoreException
    {
        synchronized(entries) {
            entries.remove(alias.toLowerCase());
        }
    }
    public Enumeration engineAliases() {
        return entries.keys();
    }
    public boolean engineContainsAlias(String alias) {
        return entries.containsKey(alias.toLowerCase());
    }
    public int engineSize() {
        return entries.size();
    }
    public boolean engineIsKeyEntry(String alias) {
        boolean isKey = false;
        Object entry = entries.get(alias.toLowerCase());
        if ((entry instanceof PrivateKeyEntry)
            || (entry instanceof SecretKeyEntry)) {
            isKey = true;
        }
        return isKey;
    }
    public boolean engineIsCertificateEntry(String alias) {
        boolean isCert = false;
        Object entry = entries.get(alias.toLowerCase());
        if (entry instanceof TrustedCertEntry) {
            isCert = true;
        }
        return isCert;
    }
    public String engineGetCertificateAlias(Certificate cert) {
        Certificate certElem;
        Enumeration e = entries.keys();
        while (e.hasMoreElements()) {
            String alias = (String)e.nextElement();
            Object entry = entries.get(alias);
            if (entry instanceof TrustedCertEntry) {
                certElem = ((TrustedCertEntry)entry).cert;
            } else if ((entry instanceof PrivateKeyEntry) &&
                       (((PrivateKeyEntry)entry).chain != null)) {
                certElem = ((PrivateKeyEntry)entry).chain[0];
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
            ObjectOutputStream oos = null;
            try {
                dos.writeInt(JCEKS_MAGIC);
                dos.writeInt(VERSION_2); 
                dos.writeInt(entries.size());
                Enumeration e = entries.keys();
                while (e.hasMoreElements()) {
                    String alias = (String)e.nextElement();
                    Object entry = entries.get(alias);
                    if (entry instanceof PrivateKeyEntry) {
                        PrivateKeyEntry pentry = (PrivateKeyEntry)entry;
                        dos.writeInt(1);
                        dos.writeUTF(alias);
                        dos.writeLong(pentry.date.getTime());
                        dos.writeInt(pentry.protectedKey.length);
                        dos.write(pentry.protectedKey);
                        int chainLen;
                        if (pentry.chain == null) {
                            chainLen = 0;
                        } else {
                            chainLen = pentry.chain.length;
                        }
                        dos.writeInt(chainLen);
                        for (int i = 0; i < chainLen; i++) {
                            encoded = pentry.chain[i].getEncoded();
                            dos.writeUTF(pentry.chain[i].getType());
                            dos.writeInt(encoded.length);
                            dos.write(encoded);
                        }
                    } else if (entry instanceof TrustedCertEntry) {
                        dos.writeInt(2);
                        dos.writeUTF(alias);
                        dos.writeLong(((TrustedCertEntry)entry).date.getTime());
                        encoded = ((TrustedCertEntry)entry).cert.getEncoded();
                        dos.writeUTF(((TrustedCertEntry)entry).cert.getType());
                        dos.writeInt(encoded.length);
                        dos.write(encoded);
                    } else {
                        dos.writeInt(3);
                        dos.writeUTF(alias);
                        dos.writeLong(((SecretKeyEntry)entry).date.getTime());
                        oos = new ObjectOutputStream(dos);
                        oos.writeObject(((SecretKeyEntry)entry).sealedKey);
                    }
                }
                byte digest[] = md.digest();
                dos.write(digest);
                dos.flush();
            } finally {
                if (oos != null) {
                    oos.close();
                } else {
                    dos.close();
                }
            }
        }
    }
    public void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        synchronized(entries) {
            DataInputStream dis;
            MessageDigest md = null;
            CertificateFactory cf = null;
            Hashtable cfs = null;
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
            ObjectInputStream ois = null;
            try {
                int xMagic = dis.readInt();
                int xVersion = dis.readInt();
                if (((xMagic != JCEKS_MAGIC) && (xMagic != JKS_MAGIC)) ||
                    ((xVersion != VERSION_1) && (xVersion != VERSION_2))) {
                    throw new IOException("Invalid keystore format");
                }
                if (xVersion == VERSION_1) {
                    cf = CertificateFactory.getInstance("X509");
                } else {
                    cfs = new Hashtable(3);
                }
                entries.clear();
                int count = dis.readInt();
                for (int i = 0; i < count; i++) {
                    int tag;
                    String alias;
                    tag = dis.readInt();
                    if (tag == 1) { 
                        PrivateKeyEntry entry = new PrivateKeyEntry();
                        alias = dis.readUTF();
                        entry.date = new Date(dis.readLong());
                        try {
                            entry.protectedKey = new byte[dis.readInt()];
                        } catch (OutOfMemoryError e) {
                            throw new IOException("Keysize too big");
                        }
                        dis.readFully(entry.protectedKey);
                        int numOfCerts = dis.readInt();
                        try {
                            if (numOfCerts > 0) {
                                entry.chain = new Certificate[numOfCerts];
                            }
                        } catch (OutOfMemoryError e) {
                            throw new IOException("Too many certificates in "
                                                  + "chain");
                        }
                        for (int j = 0; j < numOfCerts; j++) {
                            if (xVersion == 2) {
                                String certType = dis.readUTF();
                                if (cfs.containsKey(certType)) {
                                    cf = (CertificateFactory)cfs.get(certType);
                                } else {
                                    cf = CertificateFactory.getInstance(
                                        certType);
                                    cfs.put(certType, cf);
                                }
                            }
                            try {
                                encoded = new byte[dis.readInt()];
                            } catch (OutOfMemoryError e) {
                                throw new IOException("Certificate too big");
                            }
                            dis.readFully(encoded);
                            bais = new ByteArrayInputStream(encoded);
                            entry.chain[j] = cf.generateCertificate(bais);
                        }
                        entries.put(alias, entry);
                    } else if (tag == 2) { 
                        TrustedCertEntry entry = new TrustedCertEntry();
                        alias = dis.readUTF();
                        entry.date = new Date(dis.readLong());
                        if (xVersion == 2) {
                            String certType = dis.readUTF();
                            if (cfs.containsKey(certType)) {
                                cf = (CertificateFactory)cfs.get(certType);
                            } else {
                                cf = CertificateFactory.getInstance(certType);
                                cfs.put(certType, cf);
                            }
                        }
                        try {
                            encoded = new byte[dis.readInt()];
                        } catch (OutOfMemoryError e) {
                            throw new IOException("Certificate too big");
                        }
                        dis.readFully(encoded);
                        bais = new ByteArrayInputStream(encoded);
                        entry.cert = cf.generateCertificate(bais);
                        entries.put(alias, entry);
                    } else if (tag == 3) { 
                        SecretKeyEntry entry = new SecretKeyEntry();
                        alias = dis.readUTF();
                        entry.date = new Date(dis.readLong());
                        try {
                            ois = new ObjectInputStream(dis);
                            entry.sealedKey = (SealedObject)ois.readObject();
                        } catch (ClassNotFoundException cnfe) {
                            throw new IOException(cnfe.getMessage());
                        }
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
                            throw new IOException(
                                "Keystore was tampered with, or "
                                + "password was incorrect");
                        }
                    }
                }
            }  finally {
                if (ois != null) {
                    ois.close();
                } else {
                    dis.close();
                }
            }
        }
    }
    private MessageDigest getPreKeyedHash(char[] password)
    throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
