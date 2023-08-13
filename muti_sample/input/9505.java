public class JarURL {
    public static void main(String[] args) throws Exception {
        String userDir = System.getProperty("user.dir");
        String jarURL = "jar:file:" + userDir + File.separator + "foo.jar!/";
        URL codeSourceURL = new URL(jarURL);
        CodeSource cs = new CodeSource(codeSourceURL, new Certificate[0]);
        PermissionCollection perms = Policy.getPolicy().getPermissions(cs);
        if (!perms.implies(new AllPermission()))
            throw new Exception("FAILED: " + codeSourceURL
                                + " not granted AllPermission");
    }
}
