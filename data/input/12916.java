public class SecmodTest extends PKCS11Test {
    static String LIBPATH;
    static String DBDIR;
    static char[] password = "test12".toCharArray();
    static String keyAlias = "mykey";
    static boolean initSecmod() throws Exception {
        LIBPATH = getNSSLibDir();
        if (LIBPATH == null) {
            return false;
        }
        if (loadNSPR(LIBPATH) == false) {
            return false;
        }
        System.load(LIBPATH + System.mapLibraryName("softokn3"));
        System.load(LIBPATH + System.mapLibraryName("nssckbi"));
        DBDIR = System.getProperty("test.classes", ".") + SEP + "tmpdb";
        System.setProperty("pkcs11test.nss.db", DBDIR);
        File dbdirFile = new File(DBDIR);
        if (dbdirFile.exists() == false) {
            dbdirFile.mkdir();
        }
        copyFile("secmod.db", BASE, DBDIR);
        copyFile("key3.db", BASE, DBDIR);
        copyFile("cert8.db", BASE, DBDIR);
        return true;
    }
    private static void copyFile(String name, String srcDir, String dstDir) throws IOException {
        InputStream in = new FileInputStream(new File(srcDir, name));
        OutputStream out = new FileOutputStream(new File(dstDir, name));
        byte[] buf = new byte[2048];
        while (true) {
            int n = in.read(buf);
            if (n < 0) {
                break;
            }
            out.write(buf, 0, n);
        }
        in.close();
        out.close();
    }
    public void main(Provider p) throws Exception {
    }
}
