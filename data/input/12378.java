public class WrongPassword {
    private final static String BASE = System.getProperty("test.src", ".");
    public static void main(String[] args) throws Exception {
        File ksFile = new File(BASE, "pw.jks");
        char[] pw = "test12".toCharArray();
        char[] wrongPW = "foobar".toCharArray();
        String alias = "mykey";
        String otherAlias = "foo";
        KeyStore ks;
        InputStream in;
        Entry entry;
        ks = KeyStore.getInstance("JKS");
        in = new FileInputStream(ksFile);
        ks.load(in, null);
        in.close();
        System.out.println(Collections.list(ks.aliases()));
        ks = KeyStore.getInstance("JKS");
        in = new FileInputStream(ksFile);
        try {
            ks.load(in, wrongPW);
        } catch (IOException e) {
            e.printStackTrace();
            Throwable cause = e.getCause();
            if (cause instanceof UnrecoverableKeyException == false) {
                throw new Exception("not an UnrecoverableKeyException: " + cause);
            }
        }
        in.close();
        ks = KeyStore.getInstance("JKS");
        in = new FileInputStream(ksFile);
        ks.load(in, pw);
        in.close();
        System.out.println(Collections.list(ks.aliases()));
        try {
            entry = ks.getEntry(alias, null);
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        try {
            entry = ks.getEntry(alias, new PasswordProtection(wrongPW));
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        try {
            entry = ks.getEntry(alias, new PasswordProtection(new char[0]));
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        entry = ks.getEntry(alias, new PasswordProtection(pw));
        System.out.println(entry.toString().split("\n")[0]);
        try {
            ks.getKey(alias, null);
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        try {
            ks.getKey(alias, wrongPW);
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        try {
            ks.getKey(alias, new char[0]);
            throw new Exception("no exception");
        } catch (UnrecoverableKeyException e) {
            System.out.println(e);
        }
        Key k = ks.getKey(alias, pw);
        System.out.println(k.toString().split("\n")[0]);
        System.out.println(ks.getEntry(otherAlias, null));
        System.out.println(ks.getEntry(otherAlias, new PasswordProtection(wrongPW)));
        System.out.println(ks.getKey(otherAlias, null));
        System.out.println(ks.getKey(otherAlias, wrongPW));
        System.out.println("OK");
    }
}
