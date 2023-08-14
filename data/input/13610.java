public class Absolute {
    public static void main(String[] args) throws Exception {
        Constructor cons;
        try {
            Class clazz = Class.forName("sun.security.pkcs11.SunPKCS11");
            cons = clazz.getConstructor(new Class[] {String.class});
        } catch (Exception ex) {
            System.out.println("Skipping test - no PKCS11 provider available");
            return;
        }
        String config =
            System.getProperty("test.src", ".") + "/Absolute.cfg";
        try {
            Object obj = cons.newInstance(new Object[] {config});
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause instanceof ProviderException) {
                Throwable cause2 = cause.getCause();
                if ((cause2 == null) ||
                    !cause2.getMessage().startsWith(
                         "Absolute path required for library value:")) {
                    throw (ProviderException) cause;
                }
                System.out.println("Caught expected Exception: \n" + cause2);
            }
        }
    }
}
