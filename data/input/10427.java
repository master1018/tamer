public class HandshakeCompletedEvent extends EventObject
{
    private static final long serialVersionUID = 7914963744257769778L;
    private transient SSLSession session;
    public HandshakeCompletedEvent(SSLSocket sock, SSLSession s)
    {
        super(sock);
        session = s;
    }
    public SSLSession getSession()
    {
        return session;
    }
    public String getCipherSuite()
    {
        return session.getCipherSuite();
    }
    public java.security.cert.Certificate [] getLocalCertificates()
    {
        return session.getLocalCertificates();
    }
    public java.security.cert.Certificate [] getPeerCertificates()
            throws SSLPeerUnverifiedException
    {
        return session.getPeerCertificates();
    }
    public javax.security.cert.X509Certificate [] getPeerCertificateChain()
            throws SSLPeerUnverifiedException
    {
        return session.getPeerCertificateChain();
    }
    public Principal getPeerPrincipal()
            throws SSLPeerUnverifiedException
    {
        Principal principal;
        try {
            principal = session.getPeerPrincipal();
        } catch (AbstractMethodError e) {
            Certificate[] certs = getPeerCertificates();
            principal = (X500Principal)
                ((X509Certificate)certs[0]).getSubjectX500Principal();
        }
        return principal;
    }
    public Principal getLocalPrincipal()
    {
        Principal principal;
        try {
            principal = session.getLocalPrincipal();
        } catch (AbstractMethodError e) {
            principal = null;
            Certificate[] certs = getLocalCertificates();
            if (certs != null) {
                principal = (X500Principal)
                        ((X509Certificate)certs[0]).getSubjectX500Principal();
            }
        }
        return principal;
    }
    public SSLSocket getSocket()
    {
        return (SSLSocket) getSource();
    }
}
