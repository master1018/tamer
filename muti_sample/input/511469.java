public class CodeSource implements Serializable {
    private static final long serialVersionUID = 4977541819976013951L;
    private URL location;
    private transient java.security.cert.Certificate[] certs;
    private transient CodeSigner[] signers;
    private transient SocketPermission sp;
    private transient CertificateFactory factory;
    public CodeSource(URL location, Certificate[] certs) {
        this.location = location;
        if (certs != null) {
            this.certs = new Certificate[certs.length];
            System.arraycopy(certs, 0, this.certs, 0, certs.length);
        }
    }
    public CodeSource(URL location, CodeSigner[] signers) {
        this.location = location;
        if (signers != null) {
            this.signers = new CodeSigner[signers.length];
            System.arraycopy(signers, 0, this.signers, 0, signers.length);
        }
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CodeSource)) {
            return false;
        }
        CodeSource that = (CodeSource) obj;
        if (this.location != null) {
            if (that.location == null) {
                return false;
            }
            if (!this.location.equals(that.location)) {
                return false;
            }
        } else if (that.location != null) {
            return false;
        }
        Certificate[] thizCerts = getCertificatesNoClone();
        Certificate[] thatCerts = that.getCertificatesNoClone();
        if (!PolicyUtils.matchSubset(thizCerts, thatCerts)) {
            return false;
        }
        if (!PolicyUtils.matchSubset(thatCerts, thizCerts)) {
            return false;
        }
        return true;
    }
    public final Certificate[] getCertificates() {
        getCertificatesNoClone();
        if (certs == null) {
            return null;
        }
        Certificate[] tmp = new Certificate[certs.length];
        System.arraycopy(certs, 0, tmp, 0, certs.length);
        return tmp;
    }
    private Certificate[] getCertificatesNoClone() {
        if (certs != null) {
            return certs;
        }
        if (signers == null) {
            return null;
        }
        ArrayList<Certificate> v = new ArrayList<Certificate>();
        for (int i = 0; i < signers.length; i++) {
            v.addAll(signers[i].getSignerCertPath().getCertificates());
        }
        certs = v.toArray(new Certificate[v.size()]);
        return certs;
    }
    public final CodeSigner[] getCodeSigners() {
        if (signers != null) {
            CodeSigner[] tmp = new CodeSigner[signers.length];
            System.arraycopy(signers, 0, tmp, 0, tmp.length);
            return tmp;
        }
        if(certs == null || factory != null){
            return null;
        }
        X500Principal prevIssuer = null;
        ArrayList<Certificate> list = new ArrayList<Certificate>(certs.length);
        ArrayList<CodeSigner> asigners = new ArrayList<CodeSigner>();
        for (int i = 0; i < certs.length; i++) {
            if (!(certs[i] instanceof X509Certificate)) {
                continue;
            }
            X509Certificate x509 = (X509Certificate) certs[i];
            if (prevIssuer == null) {
                prevIssuer = x509.getIssuerX500Principal();
                list.add(x509);
            } else {
                X500Principal subj = x509.getSubjectX500Principal();
                if (!prevIssuer.equals(subj)) {
                    CertPath cpath = makeCertPath(list);
                    if (cpath != null) {
                        asigners.add(new CodeSigner(cpath, null));
                    }
                    list.clear();
                }
                prevIssuer = x509.getSubjectX500Principal();
                list.add(x509);
            }
        }
        if (!list.isEmpty()) {
            CertPath cpath = makeCertPath(list);
            if (cpath != null) {
                asigners.add(new CodeSigner(cpath, null));
            }
        }
        if (asigners.isEmpty()) {
            return null;
        }
        signers = new CodeSigner[asigners.size()];
        asigners.toArray(signers);
        CodeSigner[] tmp = new CodeSigner[asigners.size()];
        System.arraycopy(signers, 0, tmp, 0, tmp.length);
        return tmp;
    }
    private CertPath makeCertPath(List<? extends Certificate> list) {
        if (factory == null) {
            try {
                factory = CertificateFactory.getInstance("X.509"); 
            } catch (CertificateException ex) {
                return null;
            }
        }
        try {
            return factory.generateCertPath(list);
        } catch (CertificateException ex) {
        }
        return null;
    }
    public final URL getLocation() {
        return location;
    }
    @Override
    public int hashCode() {
        return location == null ? 0 : location.hashCode();
    }
    public boolean implies(CodeSource cs) {
        if (cs == null) {
            return false;
        }
        Certificate[] thizCerts = getCertificatesNoClone();
        if (thizCerts != null) {
            Certificate[] thatCerts = cs.getCertificatesNoClone();
            if (thatCerts == null
                    || !PolicyUtils.matchSubset(thizCerts, thatCerts)) {
                return false;
            }
        }
        if (this.location != null) {
            if (cs.location == null) {
                return false;
            }
            if (this.location.equals(cs.location)) {
                return true;
            }
            if (!this.location.getProtocol().equals(cs.location.getProtocol())) {
                return false;
            }
            String thisHost = this.location.getHost();
            if (thisHost != null) {
                String thatHost = cs.location.getHost();
                if (thatHost == null) {
                    return false;
                }
                if (!((thisHost.length() == 0 || "localhost".equals(thisHost)) && (thatHost 
                        .length() == 0 || "localhost".equals(thatHost))) 
                        && !thisHost.equals(thatHost)) {
                    if (this.sp == null) {
                        this.sp = new SocketPermission(thisHost, "resolve"); 
                    }
                    if (cs.sp == null) {
                        cs.sp = new SocketPermission(thatHost, "resolve"); 
                    }
                    if (!this.sp.implies(cs.sp)) {
                        return false;
                    }
                } 
            } 
            if (this.location.getPort() != -1) {
                if (this.location.getPort() != cs.location.getPort()) {
                    return false;
                }
            }
            String thisFile = this.location.getFile();
            String thatFile = cs.location.getFile();
            if (thisFile.endsWith("/-")) { 
                if (!thatFile.startsWith(thisFile.substring(0, thisFile
                        .length() - 2))) {
                    return false;
                }
            } else if (thisFile.endsWith("
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("CodeSource, url="); 
        buf.append(location == null ? "<null>" : location.toString()); 
        if (certs == null) {
            buf.append(", <no certificates>"); 
        } else {
            buf.append("\nCertificates [\n"); 
            for (int i = 0; i < certs.length; i++) {
                buf.append(i + 1).append(") ").append(certs[i]).append("\n"); 
            }
            buf.append("]\n"); 
        }
        if (signers != null) {
            buf.append("\nCodeSigners [\n"); 
            for (int i = 0; i < signers.length; i++) {
                buf.append(i + 1).append(") ").append(signers[i]).append("\n"); 
            }
            buf.append("]\n"); 
        }
        return buf.toString();
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        if (certs == null || certs.length == 0) {
            oos.writeInt(0);
        } else {
            oos.writeInt(certs.length);
            for (int i = 0; i < certs.length; i++) {
                try {
                    oos.writeUTF(certs[i].getType());
                    byte[] data = certs[i].getEncoded();
                    oos.writeInt(data.length);
                    oos.write(data);
                } catch (CertificateEncodingException ex) {
                    throw (IOException) new IOException(
                            Messages.getString("security.18")).initCause(ex); 
                }
            }
        }
        if (signers != null && signers.length != 0) {
            oos.writeObject(signers);
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        int certsCount = ois.readInt();
        certs = null;
        if (certsCount != 0) {
            certs = new Certificate[certsCount];
            for (int i = 0; i < certsCount; i++) {
                String type = ois.readUTF();
                CertificateFactory factory;
                try {
                    factory = CertificateFactory.getInstance(type);
                } catch (CertificateException ex) {
                    throw new ClassNotFoundException(
                            Messages.getString("security.19", type), 
                            ex);
                }
                int dataLen = ois.readInt();
                byte[] data = new byte[dataLen];
                ois.readFully(data);
                ByteArrayInputStream bais = new ByteArrayInputStream(data);
                try {
                    certs[i] = factory.generateCertificate(bais);
                } catch (CertificateException ex) {
                    throw (IOException) new IOException(
                            Messages.getString("security.1A")).initCause(ex); 
                }
            }
        }
        try {
            signers = (CodeSigner[]) ois.readObject();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw ex;
            }
        }
    }
}
