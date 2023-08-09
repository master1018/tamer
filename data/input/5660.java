public class ConfigShortPath {
    private static final String[] configNames = { "csp.cfg", "cspPlus.cfg" };
    public static void main(String[] args) throws Exception {
        Constructor cons = null;
        try {
            Class clazz = Class.forName("sun.security.pkcs11.SunPKCS11");
            cons = clazz.getConstructor(String.class);
        } catch (Exception ex) {
            System.out.println("Skipping test - no PKCS11 provider available");
            return;
        }
        String testSrc = System.getProperty("test.src", ".");
        for (int i = 0; i < configNames.length; i++) {
            String configFile = testSrc + File.separator + configNames[i];
            System.out.println("Testing against " + configFile);
            try {
                Object obj = cons.newInstance(configFile);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof ProviderException) {
                    String causeMsg = cause.getCause().getMessage();
                    if (causeMsg.indexOf("Unexpected token") != -1) {
                        throw (ProviderException) cause;
                    }
                }
            }
        }
    }
}
