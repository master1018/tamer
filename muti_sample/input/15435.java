public final class PKCS12KeyStore extends KeyStoreSpi {
    public static final int VERSION_3 = 3;
    private static final int keyBag[]  = {1, 2, 840, 113549, 1, 12, 10, 1, 2};
    private static final int certBag[] = {1, 2, 840, 113549, 1, 12, 10, 1, 3};
    private static final int pkcs9Name[]  = {1, 2, 840, 113549, 1, 9, 20};
    private static final int pkcs9KeyId[] = {1, 2, 840, 113549, 1, 9, 21};
    private static final int pkcs9certType[] = {1, 2, 840, 113549, 1, 9, 22, 1};
    private static final int pbeWithSHAAnd40BitRC2CBC[] =
                                        {1, 2, 840, 113549, 1, 12, 1, 6};
    private static final int pbeWithSHAAnd3KeyTripleDESCBC[] =
                                        {1, 2, 840, 113549, 1, 12, 1, 3};
    private static ObjectIdentifier PKCS8ShroudedKeyBag_OID;
    private static ObjectIdentifier CertBag_OID;
    private static ObjectIdentifier PKCS9FriendlyName_OID;
    private static ObjectIdentifier PKCS9LocalKeyId_OID;
    private static ObjectIdentifier PKCS9CertType_OID;
    private static ObjectIdentifier pbeWithSHAAnd40BitRC2CBC_OID;
    private static ObjectIdentifier pbeWithSHAAnd3KeyTripleDESCBC_OID;
    private int counter = 0;
    private static final int iterationCount = 1024;
    private static final int SALT_LEN = 20;
    private int privateKeyCount = 0;
    private SecureRandom random;
    static {
        try {
            PKCS8ShroudedKeyBag_OID = new ObjectIdentifier(keyBag);
            CertBag_OID = new ObjectIdentifier(certBag);
            PKCS9FriendlyName_OID = new ObjectIdentifier(pkcs9Name);
            PKCS9LocalKeyId_OID = new ObjectIdentifier(pkcs9KeyId);
            PKCS9CertType_OID = new ObjectIdentifier(pkcs9certType);
            pbeWithSHAAnd40BitRC2CBC_OID =
                        new ObjectIdentifier(pbeWithSHAAnd40BitRC2CBC);
            pbeWithSHAAnd3KeyTripleDESCBC_OID =
                        new ObjectIdentifier(pbeWithSHAAnd3KeyTripleDESCBC);
        } catch (IOException ioe) {
        }
    }
    private static class KeyEntry {
        Date date; 
        byte[] protectedPrivKey;
        Certificate chain[];
        byte[] keyId;
        String alias;
    };
    private static class CertEntry {
        final X509Certificate cert;
        final byte[] keyId;
        final String alias;
        CertEntry(X509Certificate cert, byte[] keyId, String alias) {
            this.cert = cert;
            this.keyId = keyId;
            this.alias = alias;
        }
    }
    private Hashtable<String, KeyEntry> entries =
                                new Hashtable<String, KeyEntry>();
    private ArrayList<KeyEntry> keyList = new ArrayList<KeyEntry>();
    private LinkedHashMap<X500Principal, X509Certificate> certsMap =
            new LinkedHashMap<X500Principal, X509Certificate>();
    private ArrayList<CertEntry> certEntries = new ArrayList<CertEntry>();
    public Key engineGetKey(String alias, char[] password)
        throws NoSuchAlgorithmException, UnrecoverableKeyException
    {
        KeyEntry entry = entries.get(alias.toLowerCase());
        Key key = null;
        if (entry == null) {
            return null;
        }
        byte[] encrBytes = entry.protectedPrivKey;
        byte[] encryptedKey;
        AlgorithmParameters algParams;
        ObjectIdentifier algOid;
        try {
            EncryptedPrivateKeyInfo encrInfo =
                        new EncryptedPrivateKeyInfo(encrBytes);
            encryptedKey = encrInfo.getEncryptedData();
            DerValue val = new DerValue(encrInfo.getAlgorithm().encode());
            DerInputStream in = val.toDerInputStream();
            algOid = in.getOID();
            algParams = parseAlgParameters(in);
        } catch (IOException ioe) {
            UnrecoverableKeyException uke =
                new UnrecoverableKeyException("Private key not stored as "
                                 + "PKCS#8 EncryptedPrivateKeyInfo: " + ioe);
            uke.initCause(ioe);
            throw uke;
        }
        try {
            SecretKey skey = getPBEKey(password);
            Cipher cipher = Cipher.getInstance(algOid.toString());
            cipher.init(Cipher.DECRYPT_MODE, skey, algParams);
            byte[] privateKeyInfo = cipher.doFinal(encryptedKey);
            PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(privateKeyInfo);
            DerValue val = new DerValue(privateKeyInfo);
            DerInputStream in = val.toDerInputStream();
            int i = in.getInteger();
            DerValue[] value = in.getSequence(2);
            AlgorithmId algId = new AlgorithmId(value[0].getOID());
            String algName = algId.getName();
            KeyFactory kfac = KeyFactory.getInstance(algName);
            key =  kfac.generatePrivate(kspec);
        } catch (Exception e) {
            UnrecoverableKeyException uke =
                new UnrecoverableKeyException("Get Key failed: " +
                                        e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return key;
    }
    public Certificate[] engineGetCertificateChain(String alias) {
        KeyEntry entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry.chain == null) {
                return null;
            } else {
                return entry.chain.clone();
            }
        } else {
            return null;
        }
    }
    public Certificate engineGetCertificate(String alias) {
        KeyEntry entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            if (entry.chain == null) {
                return null;
            } else {
                return entry.chain[0];
            }
        } else {
            return null;
        }
    }
    public Date engineGetCreationDate(String alias) {
        KeyEntry entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            return new Date(entry.date.getTime());
        } else {
            return null;
        }
    }
    public synchronized void engineSetKeyEntry(String alias, Key key,
                        char[] password, Certificate[] chain)
        throws KeyStoreException
    {
        try {
            KeyEntry entry = new KeyEntry();
            entry.date = new Date();
            if (key instanceof PrivateKey) {
                if ((key.getFormat().equals("PKCS#8")) ||
                    (key.getFormat().equals("PKCS8"))) {
                    entry.protectedPrivKey =
                        encryptPrivateKey(key.getEncoded(), password);
                } else {
                    throw new KeyStoreException("Private key is not encoded" +
                                "as PKCS#8");
                }
            } else {
                throw new KeyStoreException("Key is not a PrivateKey");
            }
            if (chain != null) {
                if ((chain.length > 1) && (!validateChain(chain)))
                   throw new KeyStoreException("Certificate chain is " +
                                                "not validate");
                entry.chain = chain.clone();
            }
            entry.keyId = ("Time " + (entry.date).getTime()).getBytes("UTF8");
            entry.alias = alias.toLowerCase();
            entries.put(alias.toLowerCase(), entry);
        } catch (Exception nsae) {
            KeyStoreException ke = new KeyStoreException("Key protection " +
                                        " algorithm not found: " + nsae);
            ke.initCause(nsae);
            throw ke;
        }
    }
    public synchronized void engineSetKeyEntry(String alias, byte[] key,
                                  Certificate[] chain)
        throws KeyStoreException
    {
        try {
            new EncryptedPrivateKeyInfo(key);
        } catch (IOException ioe) {
            KeyStoreException ke = new KeyStoreException("Private key is not"
                        + " stored as PKCS#8 EncryptedPrivateKeyInfo: " + ioe);
            ke.initCause(ioe);
            throw ke;
        }
        KeyEntry entry = new KeyEntry();
        entry.date = new Date();
        try {
            entry.keyId = ("Time " + (entry.date).getTime()).getBytes("UTF8");
        } catch (UnsupportedEncodingException ex) {
        }
        entry.alias = alias.toLowerCase();
        entry.protectedPrivKey = key.clone();
        if (chain != null) {
           entry.chain = chain.clone();
        }
        entries.put(alias.toLowerCase(), entry);
    }
    private byte[] getSalt()
    {
        byte[] salt = new byte[SALT_LEN];
        if (random == null) {
           random = new SecureRandom();
        }
        random.nextBytes(salt);
        return salt;
    }
    private AlgorithmParameters getAlgorithmParameters(String algorithm)
        throws IOException
    {
        AlgorithmParameters algParams = null;
        PBEParameterSpec paramSpec =
                new PBEParameterSpec(getSalt(), iterationCount);
        try {
           algParams = AlgorithmParameters.getInstance(algorithm);
           algParams.init(paramSpec);
        } catch (Exception e) {
           IOException ioe =
                new IOException("getAlgorithmParameters failed: " +
                                e.getMessage());
           ioe.initCause(e);
           throw ioe;
        }
        return algParams;
    }
    private AlgorithmParameters parseAlgParameters(DerInputStream in)
        throws IOException
    {
        AlgorithmParameters algParams = null;
        try {
            DerValue params;
            if (in.available() == 0) {
                params = null;
            } else {
                params = in.getDerValue();
                if (params.tag == DerValue.tag_Null) {
                   params = null;
                }
            }
            if (params != null) {
                algParams = AlgorithmParameters.getInstance("PBE");
                algParams.init(params.toByteArray());
            }
        } catch (Exception e) {
           IOException ioe =
                new IOException("parseAlgParameters failed: " +
                                e.getMessage());
           ioe.initCause(e);
           throw ioe;
        }
        return algParams;
    }
    private SecretKey getPBEKey(char[] password) throws IOException
    {
        SecretKey skey = null;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password);
            SecretKeyFactory skFac = SecretKeyFactory.getInstance("PBE");
            skey = skFac.generateSecret(keySpec);
        } catch (Exception e) {
           IOException ioe = new IOException("getSecretKey failed: " +
                                        e.getMessage());
           ioe.initCause(e);
           throw ioe;
        }
        return skey;
    }
    private byte[] encryptPrivateKey(byte[] data, char[] password)
        throws IOException, NoSuchAlgorithmException, UnrecoverableKeyException
    {
        byte[] key = null;
        try {
            AlgorithmParameters algParams =
                getAlgorithmParameters("PBEWithSHA1AndDESede");
            SecretKey skey = getPBEKey(password);
            Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
            cipher.init(Cipher.ENCRYPT_MODE, skey, algParams);
            byte[] encryptedKey = cipher.doFinal(data);
            AlgorithmId algid =
                new AlgorithmId(pbeWithSHAAnd3KeyTripleDESCBC_OID, algParams);
            EncryptedPrivateKeyInfo encrInfo =
                new EncryptedPrivateKeyInfo(algid, encryptedKey);
            key = encrInfo.getEncoded();
        } catch (Exception e) {
            UnrecoverableKeyException uke =
                new UnrecoverableKeyException("Encrypt Private Key failed: "
                                                + e.getMessage());
            uke.initCause(e);
            throw uke;
        }
        return key;
    }
    public synchronized void engineSetCertificateEntry(String alias,
        Certificate cert) throws KeyStoreException
    {
        KeyEntry entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            throw new KeyStoreException("Cannot overwrite own certificate");
        } else
            throw new KeyStoreException("TrustedCertEntry not supported");
    }
    public synchronized void engineDeleteEntry(String alias)
        throws KeyStoreException
    {
        entries.remove(alias.toLowerCase());
    }
    public Enumeration<String> engineAliases() {
        return entries.keys();
    }
    public boolean engineContainsAlias(String alias) {
        return entries.containsKey(alias.toLowerCase());
    }
    public int engineSize() {
        return entries.size();
    }
    public boolean engineIsKeyEntry(String alias) {
        KeyEntry entry = entries.get(alias.toLowerCase());
        if (entry != null) {
            return true;
        } else {
            return false;
        }
    }
    public boolean engineIsCertificateEntry(String alias) {
        return false;
    }
    public String engineGetCertificateAlias(Certificate cert) {
        Certificate certElem = null;
        for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            KeyEntry entry = entries.get(alias);
            if (entry.chain != null) {
                certElem = entry.chain[0];
            }
            if (certElem.equals(cert)) {
                return alias;
            }
        }
        return null;
    }
    public synchronized void engineStore(OutputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        if (password == null) {
           throw new IllegalArgumentException("password can't be null");
        }
        DerOutputStream pfx = new DerOutputStream();
        DerOutputStream version = new DerOutputStream();
        version.putInteger(VERSION_3);
        byte[] pfxVersion = version.toByteArray();
        pfx.write(pfxVersion);
        DerOutputStream authSafe = new DerOutputStream();
        DerOutputStream authSafeContentInfo = new DerOutputStream();
        byte[] safeContentData = createSafeContent();
        ContentInfo dataContentInfo = new ContentInfo(safeContentData);
        dataContentInfo.encode(authSafeContentInfo);
        byte[] encrData = createEncryptedData(password);
        ContentInfo encrContentInfo =
                new ContentInfo(ContentInfo.ENCRYPTED_DATA_OID,
                                new DerValue(encrData));
        encrContentInfo.encode(authSafeContentInfo);
        DerOutputStream cInfo = new DerOutputStream();
        cInfo.write(DerValue.tag_SequenceOf, authSafeContentInfo);
        byte[] authenticatedSafe = cInfo.toByteArray();
        ContentInfo contentInfo = new ContentInfo(authenticatedSafe);
        contentInfo.encode(authSafe);
        byte[] authSafeData = authSafe.toByteArray();
        pfx.write(authSafeData);
        byte[] macData = calculateMac(password, authenticatedSafe);
        pfx.write(macData);
        DerOutputStream pfxout = new DerOutputStream();
        pfxout.write(DerValue.tag_Sequence, pfx);
        byte[] pfxData = pfxout.toByteArray();
        stream.write(pfxData);
        stream.flush();
    }
    private byte[] generateHash(byte[] data) throws IOException
    {
        byte[] digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(data);
            digest = md.digest();
        } catch (Exception e) {
            IOException ioe = new IOException("generateHash failed: " + e);
            ioe.initCause(e);
            throw ioe;
        }
        return digest;
    }
    private byte[] calculateMac(char[] passwd, byte[] data)
        throws IOException
    {
        byte[] mData = null;
        String algName = "SHA1";
        try {
            byte[] salt = getSalt();
            Mac m = Mac.getInstance("HmacPBESHA1");
            PBEParameterSpec params =
                        new PBEParameterSpec(salt, iterationCount);
            SecretKey key = getPBEKey(passwd);
            m.init(key, params);
            m.update(data);
            byte[] macResult = m.doFinal();
            MacData macData = new MacData(algName, macResult, salt,
                                                iterationCount);
            DerOutputStream bytes = new DerOutputStream();
            bytes.write(macData.getEncoded());
            mData = bytes.toByteArray();
        } catch (Exception e) {
            IOException ioe = new IOException("calculateMac failed: " + e);
            ioe.initCause(e);
            throw ioe;
        }
        return mData;
    }
    private boolean validateChain(Certificate[] certChain)
    {
        for (int i = 0; i < certChain.length-1; i++) {
            X500Principal issuerDN =
                ((X509Certificate)certChain[i]).getIssuerX500Principal();
            X500Principal subjectDN =
                ((X509Certificate)certChain[i+1]).getSubjectX500Principal();
            if (!(issuerDN.equals(subjectDN)))
                return false;
        }
        return true;
    }
    private byte[] getBagAttributes(String alias, byte[] keyId)
        throws IOException {
        byte[] localKeyID = null;
        byte[] friendlyName = null;
        if ((alias == null) && (keyId == null)) {
            return null;
        }
        DerOutputStream bagAttrs = new DerOutputStream();
        if (alias != null) {
            DerOutputStream bagAttr1 = new DerOutputStream();
            bagAttr1.putOID(PKCS9FriendlyName_OID);
            DerOutputStream bagAttrContent1 = new DerOutputStream();
            DerOutputStream bagAttrValue1 = new DerOutputStream();
            bagAttrContent1.putBMPString(alias);
            bagAttr1.write(DerValue.tag_Set, bagAttrContent1);
            bagAttrValue1.write(DerValue.tag_Sequence, bagAttr1);
            friendlyName = bagAttrValue1.toByteArray();
        }
        if (keyId != null) {
            DerOutputStream bagAttr2 = new DerOutputStream();
            bagAttr2.putOID(PKCS9LocalKeyId_OID);
            DerOutputStream bagAttrContent2 = new DerOutputStream();
            DerOutputStream bagAttrValue2 = new DerOutputStream();
            bagAttrContent2.putOctetString(keyId);
            bagAttr2.write(DerValue.tag_Set, bagAttrContent2);
            bagAttrValue2.write(DerValue.tag_Sequence, bagAttr2);
            localKeyID = bagAttrValue2.toByteArray();
        }
        DerOutputStream attrs = new DerOutputStream();
        if (friendlyName != null) {
            attrs.write(friendlyName);
        }
        if (localKeyID != null) {
            attrs.write(localKeyID);
        }
        bagAttrs.write(DerValue.tag_Set, attrs);
        return bagAttrs.toByteArray();
    }
    private byte[] createEncryptedData(char[] password)
        throws CertificateException, IOException
    {
        DerOutputStream out = new DerOutputStream();
        for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            KeyEntry entry = entries.get(alias);
            int chainLen;
            if (entry.chain == null) {
                chainLen = 0;
            } else {
                chainLen = entry.chain.length;
            }
            for (int i = 0; i < chainLen; i++) {
                DerOutputStream safeBag = new DerOutputStream();
                safeBag.putOID(CertBag_OID);
                DerOutputStream certBag = new DerOutputStream();
                certBag.putOID(PKCS9CertType_OID);
                DerOutputStream certValue = new DerOutputStream();
                X509Certificate cert = (X509Certificate)entry.chain[i];
                certValue.putOctetString(cert.getEncoded());
                certBag.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                        true, (byte) 0), certValue);
                DerOutputStream certout = new DerOutputStream();
                certout.write(DerValue.tag_Sequence, certBag);
                byte[] certBagValue = certout.toByteArray();
                DerOutputStream bagValue = new DerOutputStream();
                bagValue.write(certBagValue);
                safeBag.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                true, (byte) 0), bagValue);
                byte[] bagAttrs = null;
                if (i == 0) {
                    bagAttrs = getBagAttributes(entry.alias, entry.keyId);
                } else {
                    bagAttrs = getBagAttributes(
                            cert.getSubjectX500Principal().getName(), null);
                }
                if (bagAttrs != null) {
                    safeBag.write(bagAttrs);
                }
                out.write(DerValue.tag_Sequence, safeBag);
            } 
        }
        DerOutputStream safeBagValue = new DerOutputStream();
        safeBagValue.write(DerValue.tag_SequenceOf, out);
        byte[] safeBagData = safeBagValue.toByteArray();
        byte[] encrContentInfo = encryptContent(safeBagData, password);
        DerOutputStream encrData = new DerOutputStream();
        DerOutputStream encrDataContent = new DerOutputStream();
        encrData.putInteger(0);
        encrData.write(encrContentInfo);
        encrDataContent.write(DerValue.tag_Sequence, encrData);
        return encrDataContent.toByteArray();
    }
    private byte[] createSafeContent()
        throws CertificateException, IOException {
        DerOutputStream out = new DerOutputStream();
        for (Enumeration<String> e = entries.keys(); e.hasMoreElements(); ) {
            String alias = e.nextElement();
            KeyEntry entry = entries.get(alias);
            DerOutputStream safeBag = new DerOutputStream();
            safeBag.putOID(PKCS8ShroudedKeyBag_OID);
            byte[] encrBytes = entry.protectedPrivKey;
            EncryptedPrivateKeyInfo encrInfo = null;
            try {
                encrInfo = new EncryptedPrivateKeyInfo(encrBytes);
            } catch (IOException ioe) {
                throw new IOException("Private key not stored as "
                        + "PKCS#8 EncryptedPrivateKeyInfo" + ioe.getMessage());
            }
            DerOutputStream bagValue = new DerOutputStream();
            bagValue.write(encrInfo.getEncoded());
            safeBag.write(DerValue.createTag(DerValue.TAG_CONTEXT,
                                true, (byte) 0), bagValue);
            byte[] bagAttrs = getBagAttributes(alias, entry.keyId);
            safeBag.write(bagAttrs);
            out.write(DerValue.tag_Sequence, safeBag);
        }
        DerOutputStream safeBagValue = new DerOutputStream();
        safeBagValue.write(DerValue.tag_Sequence, out);
        return safeBagValue.toByteArray();
    }
    private byte[] encryptContent(byte[] data, char[] password)
        throws IOException {
        byte[] encryptedData = null;
        AlgorithmParameters algParams =
                getAlgorithmParameters("PBEWithSHA1AndRC2_40");
        DerOutputStream bytes = new DerOutputStream();
        AlgorithmId algId =
                new AlgorithmId(pbeWithSHAAnd40BitRC2CBC_OID, algParams);
        algId.encode(bytes);
        byte[] encodedAlgId = bytes.toByteArray();
        try {
            SecretKey skey = getPBEKey(password);
            Cipher cipher = Cipher.getInstance("PBEWithSHA1AndRC2_40");
            cipher.init(Cipher.ENCRYPT_MODE, skey, algParams);
            encryptedData = cipher.doFinal(data);
        } catch (Exception e) {
            IOException ioe = new IOException("Failed to encrypt" +
                                " safe contents entry: " + e);
            ioe.initCause(e);
            throw ioe;
        }
        DerOutputStream bytes2 = new DerOutputStream();
        bytes2.putOID(ContentInfo.DATA_OID);
        bytes2.write(encodedAlgId);
        DerOutputStream tmpout2 = new DerOutputStream();
        tmpout2.putOctetString(encryptedData);
        bytes2.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                        false, (byte)0), tmpout2);
        DerOutputStream out = new DerOutputStream();
        out.write(DerValue.tag_Sequence, bytes2);
        return out.toByteArray();
    }
    public synchronized void engineLoad(InputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        DataInputStream dis;
        CertificateFactory cf = null;
        ByteArrayInputStream bais = null;
        byte[] encoded = null;
        if (stream == null)
           return;
        counter = 0;
        DerValue val = new DerValue(stream);
        DerInputStream s = val.toDerInputStream();
        int version = s.getInteger();
        if (version != VERSION_3) {
           throw new IOException("PKCS12 keystore not in version 3 format");
        }
        entries.clear();
        byte[] authSafeData;
        ContentInfo authSafe = new ContentInfo(s);
        ObjectIdentifier contentType = authSafe.getContentType();
        if (contentType.equals(ContentInfo.DATA_OID)) {
           authSafeData = authSafe.getData();
        } else  {
           throw new IOException("public key protected PKCS12 not supported");
        }
        DerInputStream as = new DerInputStream(authSafeData);
        DerValue[] safeContentsArray = as.getSequence(2);
        int count = safeContentsArray.length;
        privateKeyCount = 0;
        for (int i = 0; i < count; i++) {
            byte[] safeContentsData;
            ContentInfo safeContents;
            DerInputStream sci;
            byte[] eAlgId = null;
            sci = new DerInputStream(safeContentsArray[i].toByteArray());
            safeContents = new ContentInfo(sci);
            contentType = safeContents.getContentType();
            safeContentsData = null;
            if (contentType.equals(ContentInfo.DATA_OID)) {
                safeContentsData = safeContents.getData();
            } else if (contentType.equals(ContentInfo.ENCRYPTED_DATA_OID)) {
                if (password == null) {
                   continue;
                }
                DerInputStream edi =
                                safeContents.getContent().toDerInputStream();
                int edVersion = edi.getInteger();
                DerValue[] seq = edi.getSequence(2);
                ObjectIdentifier edContentType = seq[0].getOID();
                eAlgId = seq[1].toByteArray();
                if (!seq[2].isContextSpecific((byte)0)) {
                   throw new IOException("encrypted content not present!");
                }
                byte newTag = DerValue.tag_OctetString;
                if (seq[2].isConstructed())
                   newTag |= 0x20;
                seq[2].resetTag(newTag);
                safeContentsData = seq[2].getOctetString();
                DerInputStream in = seq[1].toDerInputStream();
                ObjectIdentifier algOid = in.getOID();
                AlgorithmParameters algParams = parseAlgParameters(in);
                try {
                    SecretKey skey = getPBEKey(password);
                    Cipher cipher = Cipher.getInstance(algOid.toString());
                    cipher.init(Cipher.DECRYPT_MODE, skey, algParams);
                    safeContentsData = cipher.doFinal(safeContentsData);
                } catch (Exception e) {
                    IOException ioe = new IOException("failed to decrypt safe"
                                        + " contents entry: " + e);
                    ioe.initCause(e);
                    throw ioe;
                }
            } else {
                throw new IOException("public key protected PKCS12" +
                                        " not supported");
            }
            DerInputStream sc = new DerInputStream(safeContentsData);
            loadSafeContents(sc, password);
        }
        if (password != null && s.available() > 0) {
           MacData macData = new MacData(s);
           try {
                String algName = macData.getDigestAlgName().toUpperCase();
                if (algName.equals("SHA")  ||
                    algName.equals("SHA1") ||
                    algName.equals("SHA-1")) {
                    algName = "SHA1";
                }
                Mac m = Mac.getInstance("HmacPBE" + algName);
                PBEParameterSpec params =
                        new PBEParameterSpec(macData.getSalt(),
                                        macData.getIterations());
                SecretKey key = getPBEKey(password);
                m.init(key, params);
                m.update(authSafeData);
                byte[] macResult = m.doFinal();
                if (!Arrays.equals(macData.getDigest(), macResult)) {
                   throw new SecurityException("Failed PKCS12" +
                                        " integrity checking");
                }
           } catch (Exception e) {
                IOException ioe =
                        new IOException("Integrity check failed: " + e);
                ioe.initCause(e);
                throw ioe;
           }
        }
        KeyEntry[] list = keyList.toArray(new KeyEntry[keyList.size()]);
        for (int m = 0; m < list.length; m++) {
            KeyEntry entry = list[m];
            if (entry.keyId != null) {
                ArrayList<X509Certificate> chain =
                                new ArrayList<X509Certificate>();
                X509Certificate cert = findMatchedCertificate(entry);
                while (cert != null) {
                    chain.add(cert);
                    X500Principal issuerDN = cert.getIssuerX500Principal();
                    if (issuerDN.equals(cert.getSubjectX500Principal())) {
                        break;
                    }
                    cert = certsMap.get(issuerDN);
                }
                if (chain.size() > 0)
                    entry.chain = chain.toArray(new Certificate[chain.size()]);
            }
        }
        certEntries.clear();
        certsMap.clear();
        keyList.clear();
    }
    private X509Certificate findMatchedCertificate(KeyEntry entry) {
        CertEntry keyIdMatch = null;
        CertEntry aliasMatch = null;
        for (CertEntry ce: certEntries) {
            if (Arrays.equals(entry.keyId, ce.keyId)) {
                keyIdMatch = ce;
                if (entry.alias.equalsIgnoreCase(ce.alias)) {
                    return ce.cert;
                }
            } else if (entry.alias.equalsIgnoreCase(ce.alias)) {
                aliasMatch = ce;
            }
        }
        if (keyIdMatch != null) return keyIdMatch.cert;
        else if (aliasMatch != null) return aliasMatch.cert;
        else return null;
    }
    private void loadSafeContents(DerInputStream stream, char[] password)
        throws IOException, NoSuchAlgorithmException, CertificateException
    {
        DerValue[] safeBags = stream.getSequence(2);
        int count = safeBags.length;
        for (int i = 0; i < count; i++) {
            ObjectIdentifier bagId;
            DerInputStream sbi;
            DerValue bagValue;
            Object bagItem = null;
            sbi = safeBags[i].toDerInputStream();
            bagId = sbi.getOID();
            bagValue = sbi.getDerValue();
            if (!bagValue.isContextSpecific((byte)0)) {
                throw new IOException("unsupported PKCS12 bag value type "
                                        + bagValue.tag);
            }
            bagValue = bagValue.data.getDerValue();
            if (bagId.equals(PKCS8ShroudedKeyBag_OID)) {
                KeyEntry kEntry = new KeyEntry();
                kEntry.protectedPrivKey = bagValue.toByteArray();
                bagItem = kEntry;
                privateKeyCount++;
            } else if (bagId.equals(CertBag_OID)) {
                DerInputStream cs = new DerInputStream(bagValue.toByteArray());
                DerValue[] certValues = cs.getSequence(2);
                ObjectIdentifier certId = certValues[0].getOID();
                if (!certValues[1].isContextSpecific((byte)0)) {
                    throw new IOException("unsupported PKCS12 cert value type "
                                        + certValues[1].tag);
                }
                DerValue certValue = certValues[1].data.getDerValue();
                CertificateFactory cf = CertificateFactory.getInstance("X509");
                X509Certificate cert;
                cert = (X509Certificate)cf.generateCertificate
                        (new ByteArrayInputStream(certValue.getOctetString()));
                bagItem = cert;
            } else {
            }
            DerValue[] attrSet;
            try {
                attrSet = sbi.getSet(2);
            } catch (IOException e) {
                attrSet = null;
            }
            String alias = null;
            byte[] keyId = null;
            if (attrSet != null) {
                for (int j = 0; j < attrSet.length; j++) {
                    DerInputStream as =
                        new DerInputStream(attrSet[j].toByteArray());
                    DerValue[] attrSeq = as.getSequence(2);
                    ObjectIdentifier attrId = attrSeq[0].getOID();
                    DerInputStream vs =
                        new DerInputStream(attrSeq[1].toByteArray());
                    DerValue[] valSet;
                    try {
                        valSet = vs.getSet(1);
                    } catch (IOException e) {
                        throw new IOException("Attribute " + attrId +
                                " should have a value " + e.getMessage());
                    }
                    if (attrId.equals(PKCS9FriendlyName_OID)) {
                        alias = valSet[0].getBMPString();
                    } else if (attrId.equals(PKCS9LocalKeyId_OID)) {
                        keyId = valSet[0].getOctetString();
                    } else {
                    }
                }
            }
            if (bagItem instanceof KeyEntry) {
                KeyEntry entry = (KeyEntry)bagItem;
                if (keyId == null) {
                   if (privateKeyCount == 1) {
                        keyId = "01".getBytes("UTF8");
                   } else {
                        continue;
                   }
                }
                entry.keyId = keyId;
                String keyIdStr = new String(keyId, "UTF8");
                Date date = null;
                if (keyIdStr.startsWith("Time ")) {
                    try {
                        date = new Date(
                                Long.parseLong(keyIdStr.substring(5)));
                    } catch (Exception e) {
                        date = null;
                    }
                }
                if (date == null) {
                    date = new Date();
                }
                entry.date = date;
                keyList.add(entry);
                if (alias == null)
                   alias = getUnfriendlyName();
                entry.alias = alias;
                entries.put(alias.toLowerCase(), entry);
            } else if (bagItem instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate)bagItem;
                if ((keyId == null) && (privateKeyCount == 1)) {
                    if (i == 0) {
                        keyId = "01".getBytes("UTF8");
                    }
                }
                certEntries.add(new CertEntry(cert, keyId, alias));
                X500Principal subjectDN = cert.getSubjectX500Principal();
                if (subjectDN != null) {
                    if (!certsMap.containsKey(subjectDN)) {
                        certsMap.put(subjectDN, cert);
                    }
                }
            }
        }
    }
    private String getUnfriendlyName() {
        counter++;
        return (String.valueOf(counter));
    }
}
