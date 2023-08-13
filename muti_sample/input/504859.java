public class X509CertFactoryImpl extends CertificateFactorySpi {
    private static int CERT_CACHE_SEED_LENGTH = 28;
    private static Cache CERT_CACHE = new Cache(CERT_CACHE_SEED_LENGTH);
    private static int CRL_CACHE_SEED_LENGTH = 24;
    private static Cache CRL_CACHE = new Cache(CRL_CACHE_SEED_LENGTH);
    public X509CertFactoryImpl() { }
    public Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(Messages.getString("security.153")); 
        }
        try {
            if (!inStream.markSupported()) {
                inStream = new RestoringInputStream(inStream);
            }
            inStream.mark(1);
            if (inStream.read() == '-') {
                return getCertificate(decodePEM(inStream, CERT_BOUND_SUFFIX));
            } else {
                inStream.reset();
                return getCertificate(inStream);
            }
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }
    public Collection<? extends Certificate>
            engineGenerateCertificates(InputStream inStream)
                throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(Messages.getString("security.153")); 
        }
        ArrayList result = new ArrayList();
        try {
            if (!inStream.markSupported()) {
                inStream = new RestoringInputStream(inStream);
            }
            byte[] encoding = null;
            int second_asn1_tag = -1;
            inStream.mark(1);
            int ch;
            while ((ch = inStream.read()) != -1) {
                if (ch == '-') { 
                    encoding = decodePEM(inStream, FREE_BOUND_SUFFIX);
                } else if (ch == 0x30) { 
                    encoding = null;
                    inStream.reset();
                    inStream.mark(CERT_CACHE_SEED_LENGTH);
                } else { 
                    if (result.size() == 0) {
                        throw new CertificateException(
                                Messages.getString("security.15F")); 
                    } else {
                        inStream.reset();
                        return result;
                    }
                }
                BerInputStream in = (encoding == null)
                                        ? new BerInputStream(inStream)
                                        : new BerInputStream(encoding);
                second_asn1_tag = in.next(); 
                if (encoding == null) {
                    inStream.reset();
                }
                if (second_asn1_tag != ASN1Constants.TAG_C_SEQUENCE) {
                    if (result.size() == 0) {
                        break;
                    } else {
                        return result;
                    }
                } else {
                    if (encoding == null) {
                        result.add(getCertificate(inStream));
                    } else {
                        result.add(getCertificate(encoding));
                    }
                }
                inStream.mark(1);
            }
            if (result.size() != 0) {
                return result;
            } else if (ch == -1) {
                throw new CertificateException(
                        Messages.getString("security.155")); 
            }
            if (second_asn1_tag == ASN1Constants.TAG_OID) {
                ContentInfo info = (ContentInfo) 
                    ((encoding != null)
                        ? ContentInfo.ASN1.decode(encoding)
                        : ContentInfo.ASN1.decode(inStream));
                SignedData data = info.getSignedData();
                if (data == null) {
                    throw new CertificateException(
                            Messages.getString("security.154")); 
                }
                List certs = data.getCertificates();
                if (certs != null) {
                    for (int i = 0; i < certs.size(); i++) {
                        result.add(new X509CertImpl(
                            (org.apache.harmony.security.x509.Certificate)
                                certs.get(i)));
                    }
                }
                return result;
            }
            throw new CertificateException(
                            Messages.getString("security.15F")); 
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }
    public CRL engineGenerateCRL(InputStream inStream)
            throws CRLException {
        if (inStream == null) {
            throw new CRLException(Messages.getString("security.153")); 
        }
        try {
            if (!inStream.markSupported()) {
                inStream = new RestoringInputStream(inStream);
            }
            inStream.mark(1);
            if (inStream.read() == '-') {
                return getCRL(decodePEM(inStream, FREE_BOUND_SUFFIX));
            } else {
                inStream.reset();
                return getCRL(inStream);
            }
        } catch (IOException e) {
            throw new CRLException(e);
        }
    }
    public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream)
            throws CRLException {
        if (inStream == null) {
            throw new CRLException(Messages.getString("security.153")); 
        }
        ArrayList result = new ArrayList();
        try {
            if (!inStream.markSupported()) {
                inStream = new RestoringInputStream(inStream);
            }
            byte[] encoding = null;
            int second_asn1_tag = -1;
            inStream.mark(1);
            int ch;
            while ((ch = inStream.read()) != -1) {
                if (ch == '-') { 
                    encoding = decodePEM(inStream, FREE_BOUND_SUFFIX);
                } else if (ch == 0x30) { 
                    encoding = null;
                    inStream.reset();
                    inStream.mark(CRL_CACHE_SEED_LENGTH);
                } else { 
                    if (result.size() == 0) {
                        throw new CRLException(
                                Messages.getString("security.15F")); 
                    } else {
                        inStream.reset();
                        return result;
                    }
                }
                BerInputStream in = (encoding == null)
                                        ? new BerInputStream(inStream)
                                        : new BerInputStream(encoding);
                second_asn1_tag = in.next();
                if (encoding == null) {
                    inStream.reset();
                }
                if (second_asn1_tag != ASN1Constants.TAG_C_SEQUENCE) {
                    if (result.size() == 0) {
                        break;
                    } else {
                        return result;
                    }
                } else {
                    if (encoding == null) {
                        result.add(getCRL(inStream));
                    } else {
                        result.add(getCRL(encoding));
                    }
                }
                inStream.mark(1);
            }
            if (result.size() != 0) {
                return result;
            } else if (ch == -1) {
                throw new CRLException(
                        Messages.getString("security.155")); 
            }
            if (second_asn1_tag == ASN1Constants.TAG_OID) {
                ContentInfo info = (ContentInfo) 
                    ((encoding != null)
                        ? ContentInfo.ASN1.decode(encoding)
                        : ContentInfo.ASN1.decode(inStream));
                SignedData data = info.getSignedData();
                if (data == null) {
                    throw new CRLException(
                            Messages.getString("security.154")); 
                }
                List crls = data.getCRLs();
                if (crls != null) {
                    for (int i = 0; i < crls.size(); i++) {
                        result.add(new X509CRLImpl(
                            (CertificateList) crls.get(i)));
                    }
                }
                return result;
            }
            throw new CRLException(
                        Messages.getString("security.15F")); 
        } catch (IOException e) {
            throw new CRLException(e);
        }
    }
    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(
                    Messages.getString("security.153")); 
        }
        return engineGenerateCertPath(inStream, "PkiPath"); 
    }
    public CertPath engineGenerateCertPath(
            InputStream inStream, String encoding) throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(
                    Messages.getString("security.153")); 
        }
        if (!inStream.markSupported()) {
            inStream = new RestoringInputStream(inStream);
        }
        try {
            inStream.mark(1);
            int ch;
            if ((ch = inStream.read()) == '-') {
                return X509CertPathImpl.getInstance(
                        decodePEM(inStream, FREE_BOUND_SUFFIX), encoding);
            } else if (ch == 0x30) { 
                inStream.reset();
                return X509CertPathImpl.getInstance(inStream, encoding);
            } else {
                throw new CertificateException(
                            Messages.getString("security.15F")); 
            }
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }
    public CertPath engineGenerateCertPath(List certificates)
            throws CertificateException {
        return new X509CertPathImpl(certificates);
    }
    public Iterator<String> engineGetCertPathEncodings() {
        return X509CertPathImpl.encodings.iterator();
    }
    private static byte[] pemBegin;
    private static byte[] pemClose;
    private static byte[] FREE_BOUND_SUFFIX = null;
    private static byte[] CERT_BOUND_SUFFIX;
    static {
        try {
            pemBegin = "-----BEGIN".getBytes("UTF-8"); 
            pemClose = "-----END".getBytes("UTF-8"); 
            CERT_BOUND_SUFFIX = " CERTIFICATE-----".getBytes("UTF-8"); 
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private byte[] decodePEM(InputStream inStream, byte[] boundary_suffix) 
                                                        throws IOException {
        int ch; 
        for (int i=1; i<pemBegin.length; i++) {
            if (pemBegin[i] != (ch = inStream.read())) {
                throw new IOException(
                    "Incorrect PEM encoding: '-----BEGIN"
                    + ((boundary_suffix == null) 
                        ? "" : new String(boundary_suffix))
                    + "' is expected as opening delimiter boundary.");
            }
        }
        if (boundary_suffix == null) {
            while ((ch = inStream.read()) != '\n') {
                if (ch == -1) {
                    throw new IOException(
                        Messages.getString("security.156")); 
                }
            }
        } else {
            for (int i=0; i<boundary_suffix.length; i++) {
                if (boundary_suffix[i] != inStream.read()) {
                    throw new IOException(
                        Messages.getString("security.15B", 
                            new String(boundary_suffix))); 
                }
            }
            if ((ch = inStream.read()) == '\r') {
                ch = inStream.read();
            }
            if (ch != '\n') {
                throw new IOException(
                    Messages.getString("security.15B2")); 
            }
        }
        int size = 1024; 
        byte[] buff = new byte[size];
        int index = 0;
        while ((ch = inStream.read()) != '-') {
            if (ch == -1) {
                throw new IOException(
                        Messages.getString("security.157")); 
            }
            buff[index++] = (byte) ch;
            if (index == size) {
                byte[] newbuff = new byte[size+1024];
                System.arraycopy(buff, 0, newbuff, 0, size);
                buff = newbuff;
                size += 1024;
            }
        }
        if (buff[index-1] != '\n') {
            throw new IOException(
                Messages.getString("security.158")); 
        }
        for (int i=1; i<pemClose.length; i++) {
            if (pemClose[i] != inStream.read()) {
                throw new IOException(
                    Messages.getString("security.15B1", 
                        ((boundary_suffix == null) 
                            ? "" 
                            : new String(boundary_suffix)))); 
            }
        }
        if (boundary_suffix == null) {
            while (((ch = inStream.read()) != -1)
                    && (ch != '\n') && (ch != '\r')) {
            }
        } else {
            for (int i=0; i<boundary_suffix.length; i++) {
                if (boundary_suffix[i] != inStream.read()) {
                    throw new IOException(
                        Messages.getString("security.15B1", 
                            new String(boundary_suffix))); 
                }
            }
        }
        inStream.mark(1);
        while (((ch = inStream.read()) != -1) && (ch == '\n' || ch == '\r')) {
            inStream.mark(1);
        }
        inStream.reset();
        buff = Base64.decode(buff, index);
        if (buff == null) {
            throw new IOException(Messages.getString("security.159")); 
        }
        return buff;
    };
    private static byte[] readBytes(InputStream source, int length) 
                                                            throws IOException {
        byte[] result = new byte[length];
        for (int i=0; i<length; i++) {
            int bytik = source.read();
            if (bytik == -1) {
                return null;
            }
            result[i] = (byte) bytik;
        }
        return result;
    }
    private static Certificate getCertificate(byte[] encoding) 
                                    throws CertificateException, IOException {
        if (encoding.length < CERT_CACHE_SEED_LENGTH) {
            throw new CertificateException(
                    Messages.getString("security.152")); 
        }
        synchronized (CERT_CACHE) {
            long hash = CERT_CACHE.getHash(encoding);
            if (CERT_CACHE.contains(hash)) {
                Certificate res = 
                    (Certificate) CERT_CACHE.get(hash, encoding);
                if (res != null) {
                    return res;
                }
            }
            Certificate res = new X509CertImpl(encoding);
            CERT_CACHE.put(hash, encoding, res);
            return res;
        }
    }
    private static Certificate getCertificate(InputStream inStream) 
                                    throws CertificateException, IOException {
        synchronized (CERT_CACHE) {
            inStream.mark(CERT_CACHE_SEED_LENGTH);
            byte[] buff = readBytes(inStream, CERT_CACHE_SEED_LENGTH);
            inStream.reset();
            if (buff == null) {
                throw new CertificateException(
                        Messages.getString("security.152")); 
            }
            long hash = CERT_CACHE.getHash(buff);
            if (CERT_CACHE.contains(hash)) {
                byte[] encoding = new byte[BerInputStream.getLength(buff)];
                if (encoding.length < CERT_CACHE_SEED_LENGTH) {
                    throw new CertificateException(
                        Messages.getString("security.15B3")); 
                }
                inStream.read(encoding);
                Certificate res = (Certificate) CERT_CACHE.get(hash, encoding);
                if (res != null) {
                    return res;
                }
                res = new X509CertImpl(encoding);
                CERT_CACHE.put(hash, encoding, res);
                return res;
            } else {
                inStream.reset();
                Certificate res = new X509CertImpl(inStream);
                CERT_CACHE.put(hash, res.getEncoded(), res);
                return res;
            }
        }
    }
    private static CRL getCRL(byte[] encoding) 
                                            throws CRLException, IOException {
        if (encoding.length < CRL_CACHE_SEED_LENGTH) {
            throw new CRLException(
                    Messages.getString("security.152")); 
        }
        synchronized (CRL_CACHE) {
            long hash = CRL_CACHE.getHash(encoding);
            if (CRL_CACHE.contains(hash)) {
                X509CRL res = (X509CRL) CRL_CACHE.get(hash, encoding);
                if (res != null) {
                    return res;
                }
            }
            X509CRL res = new X509CRLImpl(encoding);
            CRL_CACHE.put(hash, encoding, res);
            return res;
        }
    }
    private static CRL getCRL(InputStream inStream) 
                                            throws CRLException, IOException {
        synchronized (CRL_CACHE) {
            inStream.mark(CRL_CACHE_SEED_LENGTH);
            byte[] buff = readBytes(inStream, CRL_CACHE_SEED_LENGTH);
            inStream.reset();
            if (buff == null) {
                throw new CRLException(
                        Messages.getString("security.152")); 
            }
            long hash = CRL_CACHE.getHash(buff);
            if (CRL_CACHE.contains(hash)) {
                byte[] encoding = new byte[BerInputStream.getLength(buff)];
                if (encoding.length < CRL_CACHE_SEED_LENGTH) {
                    throw new CRLException(
                        Messages.getString("security.15B4")); 
                }
                inStream.read(encoding);
                CRL res = (CRL) CRL_CACHE.get(hash, encoding);
                if (res != null) {
                    return res;
                }
                res = new X509CRLImpl(encoding);
                CRL_CACHE.put(hash, encoding, res);
                return res;
            } else {
                X509CRL res = new X509CRLImpl(inStream);
                CRL_CACHE.put(hash, res.getEncoded(), res);
                return res;
            }
        }
    }
    private static class RestoringInputStream extends InputStream {
        private final InputStream inStream;
        private static final int BUFF_SIZE = 32;
        private final int[] buff = new int[BUFF_SIZE*2];
        private int pos = -1;
        private int bar = 0;
        private int end = 0;
        public RestoringInputStream(InputStream inStream) {
            this.inStream = inStream;
        }
        @Override
        public int available() throws IOException {
            return (bar - pos) + inStream.available();
        }
        @Override
        public void close() throws IOException {
            inStream.close();
        }
        @Override
        public void mark(int readlimit) {
            if (pos < 0) {
                pos = 0;
                bar = 0;
                end = BUFF_SIZE - 1;
            } else {
                end = (pos + BUFF_SIZE - 1) % BUFF_SIZE;
            }
        }
        @Override
        public boolean markSupported() {
            return true;
        }
        public int read() throws IOException {
            if (pos >= 0) {
                int cur = pos % BUFF_SIZE;
                if (cur < bar) {
                    pos++;
                    return buff[cur];
                }
                if (cur != end) {
                    buff[cur] = inStream.read();
                    bar = cur+1;
                    pos++;
                    return buff[cur];
                } else {
                    pos = -1;
                }
            }
            return inStream.read();
        }
        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read_b;
            int i;
            for (i=0; i<len; i++) {
                if ((read_b = read()) == -1) {
                    return (i == 0) ? -1 : i;
                }
                b[off+i] = (byte) read_b;
            }
            return i;
        }
        @Override
        public void reset() throws IOException {
            if (pos >= 0) {
                pos = (end + 1) % BUFF_SIZE;
            } else {
                throw new IOException(
                        Messages.getString("security.15A")); 
            }
        }
        @Override
        public long skip(long n) throws IOException {
            if (pos >= 0) {
                long i = 0;
                int av = available();
                if (av < n) {
                    n = av;
                }
                while ((i < n) && (read() != -1)) {
                    i++;
                }
                return i;
            } else {
                return inStream.skip(n);
            }
        }
    }
}
