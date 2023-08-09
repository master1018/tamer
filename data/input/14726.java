public class SSLContextVersion {
    static enum ContextVersion {
        TLS_CV_01("SSL", "TLSv1", "TLSv1.2"),
        TLS_CV_02("TLS", "TLSv1", "TLSv1.2"),
        TLS_CV_03("SSLv3", "TLSv1", "TLSv1.2"),
        TLS_CV_04("TLSv1", "TLSv1", "TLSv1.2"),
        TLS_CV_05("TLSv1.1", "TLSv1.1", "TLSv1.2"),
        TLS_CV_06("TLSv1.2", "TLSv1.2", "TLSv1.2"),
        TLS_CV_07("Default", "TLSv1", "TLSv1.2");
        final String contextVersion;
        final String defaultProtocolVersion;
        final String supportedProtocolVersion;
        ContextVersion(String contextVersion, String defaultProtocolVersion,
                String supportedProtocolVersion) {
            this.contextVersion = contextVersion;
            this.defaultProtocolVersion = defaultProtocolVersion;
            this.supportedProtocolVersion = supportedProtocolVersion;
        }
    }
    public static void main(String[] args) throws Exception {
        for (ContextVersion cv : ContextVersion.values()) {
            System.out.println("Checking SSLContext of " + cv.contextVersion);
            SSLContext context = SSLContext.getInstance(cv.contextVersion);
            if (!cv.contextVersion.equals("Default")) {
                context.init((KeyManager[])null, (TrustManager[])null, null);
            }
            SSLParameters parameters = context.getDefaultSSLParameters();
            String[] protocols = parameters.getProtocols();
            String[] ciphers = parameters.getCipherSuites();
            if (protocols.length == 0 || ciphers.length == 0) {
                throw new Exception("No default protocols or cipher suites");
            }
            boolean isMatch = false;
            for (String protocol : protocols) {
                System.out.println("\tdefault protocol version " + protocol);
                if (protocol.equals(cv.defaultProtocolVersion)) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                throw new Exception("No matched default protocol");
            }
            parameters = context.getSupportedSSLParameters();
            protocols = parameters.getProtocols();
            ciphers = parameters.getCipherSuites();
            if (protocols.length == 0 || ciphers.length == 0) {
                throw new Exception("No default protocols or cipher suites");
            }
            isMatch = false;
            for (String protocol : protocols) {
                System.out.println("\tsupported protocol version " + protocol);
                if (protocol.equals(cv.supportedProtocolVersion)) {
                    isMatch = true;
                    break;
                }
            }
            if (!isMatch) {
                throw new Exception("No matched default protocol");
            }
            System.out.println("\t... Success");
        }
    }
}
