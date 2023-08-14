public class X509Factory extends CertificateFactorySpi {
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    private static final int ENC_MAX_LENGTH = 4096 * 1024; 
    private static final Cache certCache = Cache.newSoftMemoryCache(750);
    private static final Cache crlCache = Cache.newSoftMemoryCache(750);
    public Certificate engineGenerateCertificate(InputStream is)
        throws CertificateException
    {
        if (is == null) {
            certCache.clear();
            X509CertificatePair.clearCache();
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(is);
            if (encoding != null) {
                X509CertImpl cert = (X509CertImpl)getFromCache(certCache, encoding);
                if (cert != null) {
                    return cert;
                }
                cert = new X509CertImpl(encoding);
                addToCache(certCache, cert.getEncodedInternal(), cert);
                return cert;
            } else {
                throw new IOException("Empty input");
            }
        } catch (IOException ioe) {
            throw (CertificateException)new CertificateException
            ("Could not parse certificate: " + ioe.toString()).initCause(ioe);
        }
    }
    private static int readFully(InputStream in, ByteArrayOutputStream bout,
            int length) throws IOException {
        int read = 0;
        byte[] buffer = new byte[2048];
        while (length > 0) {
            int n = in.read(buffer, 0, length<2048?length:2048);
            if (n <= 0) {
                break;
            }
            bout.write(buffer, 0, n);
            read += n;
            length -= n;
        }
        return read;
    }
    public static synchronized X509CertImpl intern(X509Certificate c)
            throws CertificateException {
        if (c == null) {
            return null;
        }
        boolean isImpl = c instanceof X509CertImpl;
        byte[] encoding;
        if (isImpl) {
            encoding = ((X509CertImpl)c).getEncodedInternal();
        } else {
            encoding = c.getEncoded();
        }
        X509CertImpl newC = (X509CertImpl)getFromCache(certCache, encoding);
        if (newC != null) {
            return newC;
        }
        if (isImpl) {
            newC = (X509CertImpl)c;
        } else {
            newC = new X509CertImpl(encoding);
            encoding = newC.getEncodedInternal();
        }
        addToCache(certCache, encoding, newC);
        return newC;
    }
    public static synchronized X509CRLImpl intern(X509CRL c)
            throws CRLException {
        if (c == null) {
            return null;
        }
        boolean isImpl = c instanceof X509CRLImpl;
        byte[] encoding;
        if (isImpl) {
            encoding = ((X509CRLImpl)c).getEncodedInternal();
        } else {
            encoding = c.getEncoded();
        }
        X509CRLImpl newC = (X509CRLImpl)getFromCache(crlCache, encoding);
        if (newC != null) {
            return newC;
        }
        if (isImpl) {
            newC = (X509CRLImpl)c;
        } else {
            newC = new X509CRLImpl(encoding);
            encoding = newC.getEncodedInternal();
        }
        addToCache(crlCache, encoding, newC);
        return newC;
    }
    private static synchronized Object getFromCache(Cache cache,
            byte[] encoding) {
        Object key = new Cache.EqualByteArray(encoding);
        Object value = cache.get(key);
        return value;
    }
    private static synchronized void addToCache(Cache cache, byte[] encoding,
            Object value) {
        if (encoding.length > ENC_MAX_LENGTH) {
            return;
        }
        Object key = new Cache.EqualByteArray(encoding);
        cache.put(key, value);
    }
    public CertPath engineGenerateCertPath(InputStream inStream)
        throws CertificateException
    {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(inStream);
            if (encoding != null) {
                return new X509CertPath(new ByteArrayInputStream(encoding));
            } else {
                throw new IOException("Empty input");
            }
        } catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }
    public CertPath engineGenerateCertPath(InputStream inStream,
        String encoding) throws CertificateException
    {
        if (inStream == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            byte[] data = readOneBlock(inStream);
            if (data != null) {
                return new X509CertPath(new ByteArrayInputStream(data), encoding);
            } else {
                throw new IOException("Empty input");
            }
        } catch (IOException ioe) {
            throw new CertificateException(ioe.getMessage());
        }
    }
    public CertPath
        engineGenerateCertPath(List<? extends Certificate> certificates)
        throws CertificateException
    {
        return(new X509CertPath(certificates));
    }
    public Iterator<String> engineGetCertPathEncodings() {
        return(X509CertPath.getEncodingsStatic());
    }
    public Collection<? extends java.security.cert.Certificate>
            engineGenerateCertificates(InputStream is)
            throws CertificateException {
        if (is == null) {
            throw new CertificateException("Missing input stream");
        }
        try {
            return parseX509orPKCS7Cert(is);
        } catch (IOException ioe) {
            throw new CertificateException(ioe);
        }
    }
    public CRL engineGenerateCRL(InputStream is)
        throws CRLException
    {
        if (is == null) {
            crlCache.clear();
            throw new CRLException("Missing input stream");
        }
        try {
            byte[] encoding = readOneBlock(is);
            if (encoding != null) {
                X509CRLImpl crl = (X509CRLImpl)getFromCache(crlCache, encoding);
                if (crl != null) {
                    return crl;
                }
                crl = new X509CRLImpl(encoding);
                addToCache(crlCache, crl.getEncodedInternal(), crl);
                return crl;
            } else {
                throw new IOException("Empty input");
            }
        } catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }
    public Collection<? extends java.security.cert.CRL> engineGenerateCRLs(
            InputStream is) throws CRLException
    {
        if (is == null) {
            throw new CRLException("Missing input stream");
        }
        try {
            return parseX509orPKCS7CRL(is);
        } catch (IOException ioe) {
            throw new CRLException(ioe.getMessage());
        }
    }
    private Collection<? extends java.security.cert.Certificate>
        parseX509orPKCS7Cert(InputStream is)
        throws CertificateException, IOException
    {
        Collection<X509CertImpl> coll = new ArrayList<>();
        byte[] data = readOneBlock(is);
        if (data == null) {
            return new ArrayList<>(0);
        }
        try {
            PKCS7 pkcs7 = new PKCS7(data);
            X509Certificate[] certs = pkcs7.getCertificates();
            if (certs != null) {
                return Arrays.asList(certs);
            } else {
                return new ArrayList<>(0);
            }
        } catch (ParsingException e) {
            while (data != null) {
                coll.add(new X509CertImpl(data));
                data = readOneBlock(is);
            }
        }
        return coll;
    }
    private Collection<? extends java.security.cert.CRL>
        parseX509orPKCS7CRL(InputStream is)
        throws CRLException, IOException
    {
        Collection<X509CRLImpl> coll = new ArrayList<>();
        byte[] data = readOneBlock(is);
        if (data == null) {
            return new ArrayList<>(0);
        }
        try {
            PKCS7 pkcs7 = new PKCS7(data);
            X509CRL[] crls = pkcs7.getCRLs();
            if (crls != null) {
                return Arrays.asList(crls);
            } else {
                return new ArrayList<>(0);
            }
        } catch (ParsingException e) {
            while (data != null) {
                coll.add(new X509CRLImpl(data));
                data = readOneBlock(is);
            }
        }
        return coll;
    }
    private static byte[] readOneBlock(InputStream is) throws IOException {
        int c = is.read();
        if (c == -1) {
            return null;
        }
        if (c == DerValue.tag_Sequence) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(2048);
            bout.write(c);
            readBERInternal(is, bout, c);
            return bout.toByteArray();
        } else {
            char[] data = new char[2048];
            int pos = 0;
            int hyphen = (c=='-') ? 1: 0;   
            int last = (c=='-') ? -1: c;    
            while (true) {
                int next = is.read();
                if (next == -1) {
                    return null;
                }
                if (next == '-') {
                    hyphen++;
                } else {
                    hyphen = 0;
                    last = next;
                }
                if (hyphen == 5 && (last==-1 || last=='\r' || last=='\n')) {
                    break;
                }
            }
            int end;
            StringBuffer header = new StringBuffer("-----");
            while (true) {
                int next = is.read();
                if (next == -1) {
                    throw new IOException("Incomplete data");
                }
                if (next == '\n') {
                    end = '\n';
                    break;
                }
                if (next == '\r') {
                    next = is.read();
                    if (next == -1) {
                        throw new IOException("Incomplete data");
                    }
                    if (next == '\n') {
                        end = '\n';
                    } else {
                        end = '\r';
                        data[pos++] = (char)next;
                    }
                    break;
                }
                header.append((char)next);
            }
            while (true) {
                int next = is.read();
                if (next == -1) {
                    throw new IOException("Incomplete data");
                }
                if (next != '-') {
                    data[pos++] = (char)next;
                    if (pos >= data.length) {
                        data = Arrays.copyOf(data, data.length+1024);
                    }
                } else {
                    break;
                }
            }
            StringBuffer footer = new StringBuffer("-");
            while (true) {
                int next = is.read();
                if (next == -1 || next == end || next == '\n') {
                    break;
                }
                if (next != '\r') footer.append((char)next);
            }
            checkHeaderFooter(header.toString(), footer.toString());
            BASE64Decoder decoder = new BASE64Decoder();
            return decoder.decodeBuffer(new String(data, 0, pos));
        }
    }
    private static void checkHeaderFooter(String header,
            String footer) throws IOException {
        if (header.length() < 16 || !header.startsWith("-----BEGIN ") ||
                !header.endsWith("-----")) {
            throw new IOException("Illegal header: " + header);
        }
        if (footer.length() < 14 || !footer.startsWith("-----END ") ||
                !footer.endsWith("-----")) {
            throw new IOException("Illegal footer: " + footer);
        }
        String headerType = header.substring(11, header.length()-5);
        String footerType = footer.substring(9, footer.length()-5);
        if (!headerType.equals(footerType)) {
            throw new IOException("Header and footer do not match: " +
                    header + " " + footer);
        }
    }
    private static int readBERInternal(InputStream is,
            ByteArrayOutputStream bout, int tag) throws IOException {
        if (tag == -1) {        
            tag = is.read();
            if (tag == -1) {
                throw new IOException("BER/DER tag info absent");
            }
            if ((tag & 0x1f) == 0x1f) {
                throw new IOException("Multi octets tag not supported");
            }
            bout.write(tag);
        }
        int n = is.read();
        if (n == -1) {
            throw new IOException("BER/DER length info ansent");
        }
        bout.write(n);
        int length;
        if (n == 0x80) {        
            if ((tag & 0x20) != 0x20) {
                throw new IOException(
                        "Non constructed encoding must have definite length");
            }
            while (true) {
                int subTag = readBERInternal(is, bout, -1);
                if (subTag == 0) {   
                    break;
                }
            }
        } else {
            if (n < 0x80) {
                length = n;
            } else if (n == 0x81) {
                length = is.read();
                if (length == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(length);
            } else if (n == 0x82) {
                int highByte = is.read();
                int lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(lowByte);
                length = (highByte << 8) | lowByte;
            } else if (n == 0x83) {
                int highByte = is.read();
                int midByte = is.read();
                int lowByte = is.read();
                if (lowByte == -1) {
                    throw new IOException("Incomplete BER/DER length info");
                }
                bout.write(highByte);
                bout.write(midByte);
                bout.write(lowByte);
                length = (highByte << 16) | (midByte << 8) | lowByte;
            } else { 
                throw new IOException("Invalid BER/DER data (too huge?)");
            }
            if (readFully(is, bout, length) != length) {
                throw new IOException("Incomplete BER/DER data");
            }
        }
        return tag;
    }
}
