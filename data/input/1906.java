public class IllegalURL {
    public static void main(String[] args) throws Exception {
        FileOutputStream fos = new FileOutputStream("x.conf");
        fos.close();
        use("file:" + System.getProperty("user.dir") + "/x.conf");
        use("file:x.conf");
        System.out.println("Test passed");
    }
    static void use(String f) throws Exception {
        System.out.println("Testing " + f  + "...");
        System.setProperty("java.security.auth.login.config", f);
        try {
            new FileInputStream(new URL(f).getFile().replace('/', File.separatorChar));
        } catch (Exception e) {
            System.out.println("Even old implementation does not support it. Ignored.");
            return;
        }
        new ConfigFile();
    }
}
