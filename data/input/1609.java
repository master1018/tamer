final class CipherBox {
    final static CipherBox NULL = new CipherBox();
    private static final Debug debug = Debug.getInstance("ssl");
    private final ProtocolVersion protocolVersion;
    private final Cipher cipher;
    private int blockSize;
    private SecureRandom random;
    private static Hashtable<Integer, IvParameterSpec> masks;
    private CipherBox() {
        this.protocolVersion = ProtocolVersion.DEFAULT;
        this.cipher = null;
    }
    private CipherBox(ProtocolVersion protocolVersion, BulkCipher bulkCipher,
            SecretKey key, IvParameterSpec iv, SecureRandom random,
            boolean encrypt) throws NoSuchAlgorithmException {
        try {
            this.protocolVersion = protocolVersion;
            this.cipher = JsseJce.getCipher(bulkCipher.transformation);
            int mode = encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
            if (random == null) {
                random = JsseJce.getSecureRandom();
            }
            this.random = random;
            if (iv == null && bulkCipher.ivSize != 0 &&
                    mode == Cipher.DECRYPT_MODE &&
                    protocolVersion.v >= ProtocolVersion.TLS11.v) {
                iv = getFixedMask(bulkCipher.ivSize);
            }
            cipher.init(mode, key, iv, random);
            blockSize = cipher.getBlockSize();
            if (blockSize == 1) {
                blockSize = 0;
            }
        } catch (NoSuchAlgorithmException e) {
            throw e;
        } catch (Exception e) {
            throw new NoSuchAlgorithmException
                    ("Could not create cipher " + bulkCipher, e);
        } catch (ExceptionInInitializerError e) {
            throw new NoSuchAlgorithmException
                    ("Could not create cipher " + bulkCipher, e);
        }
    }
    static CipherBox newCipherBox(ProtocolVersion version, BulkCipher cipher,
            SecretKey key, IvParameterSpec iv, SecureRandom random,
            boolean encrypt) throws NoSuchAlgorithmException {
        if (cipher.allowed == false) {
            throw new NoSuchAlgorithmException("Unsupported cipher " + cipher);
        }
        if (cipher == B_NULL) {
            return NULL;
        } else {
            return new CipherBox(version, cipher, key, iv, random, encrypt);
        }
    }
    private static IvParameterSpec getFixedMask(int ivSize) {
        if (masks == null) {
            masks = new Hashtable<Integer, IvParameterSpec>(5);
        }
        IvParameterSpec iv = masks.get(ivSize);
        if (iv == null) {
            iv = new IvParameterSpec(new byte[ivSize]);
            masks.put(ivSize, iv);
        }
        return iv;
    }
    int encrypt(byte[] buf, int offset, int len) {
        if (cipher == null) {
            return len;
        }
        try {
            if (blockSize != 0) {
                if (protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    byte[] prefix = new byte[blockSize];
                    random.nextBytes(prefix);
                    System.arraycopy(buf, offset,
                                     buf, offset + prefix.length, len);
                    System.arraycopy(prefix, 0,
                                     buf, offset, prefix.length);
                    len += prefix.length;
                }
                len = addPadding(buf, offset, len, blockSize);
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println(
                        "Padded plaintext before ENCRYPTION:  len = "
                        + len);
                    hd.encodeBuffer(
                        new ByteArrayInputStream(buf, offset, len),
                        System.out);
                } catch (IOException e) { }
            }
            int newLen = cipher.update(buf, offset, len, buf, offset);
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error " +
                    "in JCE provider " + cipher.getProvider().getName());
            }
            return newLen;
        } catch (ShortBufferException e) {
            throw new ArrayIndexOutOfBoundsException(e.toString());
        }
    }
    int encrypt(ByteBuffer bb) {
        int len = bb.remaining();
        if (cipher == null) {
            bb.position(bb.limit());
            return len;
        }
        try {
            int pos = bb.position();
            if (blockSize != 0) {
                if (protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    byte[] prefix = new byte[blockSize];
                    random.nextBytes(prefix);
                    byte[] buf = null;
                    int limit = bb.limit();
                    if (bb.hasArray()) {
                        buf = bb.array();
                        System.arraycopy(buf, pos,
                                buf, pos + prefix.length, limit - pos);
                        bb.limit(limit + prefix.length);
                    } else {
                        buf = new byte[limit - pos];
                        bb.get(buf, 0, limit - pos);
                        bb.position(pos + prefix.length);
                        bb.limit(limit + prefix.length);
                        bb.put(buf);
                    }
                    bb.position(pos);
                    bb.put(prefix);
                    bb.position(pos);
                }
                len = addPadding(bb, blockSize);
                bb.position(pos);
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println(
                        "Padded plaintext before ENCRYPTION:  len = "
                        + len);
                    hd.encodeBuffer(bb, System.out);
                } catch (IOException e) { }
                bb.position(pos);
            }
            ByteBuffer dup = bb.duplicate();
            int newLen = cipher.update(dup, bb);
            if (bb.position() != dup.position()) {
                throw new RuntimeException("bytebuffer padding error");
            }
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error " +
                    "in JCE provider " + cipher.getProvider().getName());
            }
            return newLen;
        } catch (ShortBufferException e) {
            RuntimeException exc = new RuntimeException(e.toString());
            exc.initCause(e);
            throw exc;
        }
    }
    int decrypt(byte[] buf, int offset, int len) throws BadPaddingException {
        if (cipher == null) {
            return len;
        }
        try {
            int newLen = cipher.update(buf, offset, len, buf, offset);
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error " +
                    "in JCE provider " + cipher.getProvider().getName());
            }
            if (debug != null && Debug.isOn("plaintext")) {
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println(
                        "Padded plaintext after DECRYPTION:  len = "
                        + newLen);
                    hd.encodeBuffer(
                        new ByteArrayInputStream(buf, offset, newLen),
                        System.out);
                } catch (IOException e) { }
            }
            if (blockSize != 0) {
                newLen = removePadding(buf, offset, newLen,
                             blockSize, protocolVersion);
                if (protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    if (newLen < blockSize) {
                        throw new BadPaddingException("invalid explicit IV");
                    }
                    System.arraycopy(buf, offset + blockSize,
                                     buf, offset, newLen - blockSize);
                    newLen -= blockSize;
                }
            }
            return newLen;
        } catch (ShortBufferException e) {
            throw new ArrayIndexOutOfBoundsException(e.toString());
        }
    }
    int decrypt(ByteBuffer bb) throws BadPaddingException {
        int len = bb.remaining();
        if (cipher == null) {
            bb.position(bb.limit());
            return len;
        }
        try {
            int pos = bb.position();
            ByteBuffer dup = bb.duplicate();
            int newLen = cipher.update(dup, bb);
            if (newLen != len) {
                throw new RuntimeException("Cipher buffering error " +
                    "in JCE provider " + cipher.getProvider().getName());
            }
            if (debug != null && Debug.isOn("plaintext")) {
                bb.position(pos);
                try {
                    HexDumpEncoder hd = new HexDumpEncoder();
                    System.out.println(
                        "Padded plaintext after DECRYPTION:  len = "
                        + newLen);
                    hd.encodeBuffer(bb, System.out);
                } catch (IOException e) { }
            }
            if (blockSize != 0) {
                bb.position(pos);
                newLen = removePadding(bb, blockSize, protocolVersion);
                if (protocolVersion.v >= ProtocolVersion.TLS11.v) {
                    if (newLen < blockSize) {
                        throw new BadPaddingException("invalid explicit IV");
                    }
                    byte[] buf = null;
                    int limit = bb.limit();
                    if (bb.hasArray()) {
                        buf = bb.array();
                        System.arraycopy(buf, pos + blockSize,
                                         buf, pos, limit - pos - blockSize);
                        bb.limit(limit - blockSize);
                    } else {
                        buf = new byte[limit - pos - blockSize];
                        bb.position(pos + blockSize);
                        bb.get(buf);
                        bb.position(pos);
                        bb.put(buf);
                        bb.limit(limit - blockSize);
                    }
                    limit = bb.limit();
                    bb.position(limit);
                }
            }
            return newLen;
        } catch (ShortBufferException e) {
            RuntimeException exc = new RuntimeException(e.toString());
            exc.initCause(e);
            throw exc;
        }
    }
    private static int addPadding(byte[] buf, int offset, int len,
            int blockSize) {
        int     newlen = len + 1;
        byte    pad;
        int     i;
        if ((newlen % blockSize) != 0) {
            newlen += blockSize - 1;
            newlen -= newlen % blockSize;
        }
        pad = (byte) (newlen - len);
        if (buf.length < (newlen + offset)) {
            throw new IllegalArgumentException("no space to pad buffer");
        }
        for (i = 0, offset += len; i < pad; i++) {
            buf [offset++] = (byte) (pad - 1);
        }
        return newlen;
    }
    private static int addPadding(ByteBuffer bb, int blockSize) {
        int     len = bb.remaining();
        int     offset = bb.position();
        int     newlen = len + 1;
        byte    pad;
        int     i;
        if ((newlen % blockSize) != 0) {
            newlen += blockSize - 1;
            newlen -= newlen % blockSize;
        }
        pad = (byte) (newlen - len);
        bb.limit(newlen + offset);
        for (i = 0, offset += len; i < pad; i++) {
            bb.put(offset++, (byte) (pad - 1));
        }
        bb.position(offset);
        bb.limit(offset);
        return newlen;
    }
    private static int removePadding(byte[] buf, int offset, int len,
            int blockSize, ProtocolVersion protocolVersion)
            throws BadPaddingException {
        int padOffset = offset + len - 1;
        int pad = buf[padOffset] & 0x0ff;
        int newlen = len - (pad + 1);
        if (newlen < 0) {
            throw new BadPaddingException("Padding length invalid: " + pad);
        }
        if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
            for (int i = 1; i <= pad; i++) {
                int val = buf[padOffset - i] & 0xff;
                if (val != pad) {
                    throw new BadPaddingException
                                        ("Invalid TLS padding: " + val);
                }
            }
        } else { 
            if (pad > blockSize) {
                throw new BadPaddingException("Invalid SSLv3 padding: " + pad);
            }
        }
        return newlen;
    }
    private static int removePadding(ByteBuffer bb,
            int blockSize, ProtocolVersion protocolVersion)
            throws BadPaddingException {
        int len = bb.remaining();
        int offset = bb.position();
        int padOffset = offset + len - 1;
        int pad = bb.get(padOffset) & 0x0ff;
        int newlen = len - (pad + 1);
        if (newlen < 0) {
            throw new BadPaddingException("Padding length invalid: " + pad);
        }
        if (protocolVersion.v >= ProtocolVersion.TLS10.v) {
            bb.put(padOffset, (byte)0);         
            for (int i = 1; i <= pad; i++) {
                int val = bb.get(padOffset - i) & 0xff;
                if (val != pad) {
                    throw new BadPaddingException
                                        ("Invalid TLS padding: " + val);
                }
            }
        } else { 
            if (pad > blockSize) {
                throw new BadPaddingException("Invalid SSLv3 padding: " + pad);
            }
        }
        bb.position(offset + newlen);
        bb.limit(offset + newlen);
        return newlen;
    }
    void dispose() {
        try {
            if (cipher != null) {
                cipher.doFinal();
            }
        } catch (GeneralSecurityException e) {
        }
    }
}
