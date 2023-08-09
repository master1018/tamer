public class CheckSSLContextExport extends Provider {
    private static String info = "test provider for JSSE pluggability";
    public CheckSSLContextExport(String protocols[]) {
        super("TestJSSEPluggability", 1.0, info);
        for (int i=0; i<protocols.length; i++) {
            put("SSLContext." + protocols[i], "MySSLContextImpl");
        }
    }
    public static void test(String protocol) throws Exception {
        SSLContext mySSLContext = SSLContext.getInstance(protocol);
        String providerName = mySSLContext.getProvider().getName();
        if (!providerName.equals("TestJSSEPluggability")) {
            System.out.println(providerName + "'s SSLContext is used");
            throw new Exception("...used the wrong provider: " + providerName);
        }
        for (int i = 0; i < 2; i++) {
            boolean standardCiphers = true;
            switch (i) {
            case 0:
                standardCiphers = false;
                MySSLContextImpl.useCustomCipherSuites();
                break;
            case 1:
                standardCiphers = true;
                MySSLContextImpl.useStandardCipherSuites();
                break;
            default:
                throw new Exception("Internal Test Error!");
            }
            System.out.println("Testing with " + (standardCiphers ? "standard" : "custom") +
                               " cipher suites");
            for (int j = 0; j < 4; j++) {
                String clsName = null;
                try {
                    switch (j) {
                    case 0:
                        SSLSocketFactory sf = mySSLContext.getSocketFactory();
                        clsName = sf.getClass().getName();
                        break;
                    case 1:
                        SSLServerSocketFactory ssf =
                            mySSLContext.getServerSocketFactory();
                        clsName = ssf.getClass().getName();
                        break;
                    case 2:
                        SSLEngine se = mySSLContext.createSSLEngine();
                        clsName = se.getClass().getName();
                        break;
                    case 3:
                        SSLEngine se2 = mySSLContext.createSSLEngine(null, 0);
                        clsName = se2.getClass().getName();
                        break;
                    default:
                        throw new Exception("Internal Test Error!");
                    }
                    if (!clsName.startsWith("MySSL")) {
                        throw new Exception("test#" + j +
                                             ": wrong impl is used");
                    } else {
                        System.out.println("test#" + j +
                                           ": accepted valid impl");
                    }
                } catch (RuntimeException re) {
                    throw re;
                }
            }
        }
    }
    public static void main(String[] argv) throws Exception {
        String protocols[] = { "SSL", "TLS" };
        Security.insertProviderAt(new CheckSSLContextExport(protocols), 1);
        for (int i = 0; i < protocols.length; i++) {
            System.out.println("Testing " + protocols[i] + "'s SSLContext");
            test(protocols[i]);
        }
        System.out.println("Test Passed");
    }
}
