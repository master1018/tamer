public class UseGetURLs {
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4137605\n");
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        System.err.println("Security manager: " +
                           System.getSecurityManager().getClass().getName());
        URL codebase1 = null;
        URL codebase2 = null;
        try {
            codebase1 = TestLibrary.installClassInCodebase("Dummy",
                                                           "codebase1");
            File cb2file =
                new File(TestLibrary.getProperty("user.dir", "."),
                         "codebase2");
            codebase2 = cb2file.toURL();
        } catch (MalformedURLException e) {
            TestLibrary.bomb("failed to install test classes", e);
        }
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                new URL[] { codebase1, codebase2 });
            System.err.println(
                "URLs for class loader: " +
                Arrays.asList(((URLClassLoader) loader).getURLs()));
            System.err.println("Expecting annotation: \"" +
                codebase1 + " " + codebase2 + "\"");
            System.err.println("First URL:");
            dumpURL(codebase1);
            System.err.println("Second URL:");
            dumpURL(codebase2);
            Class cl = loader.loadClass("Dummy");
            String annotation = RMIClassLoader.getClassAnnotation(cl);
            System.err.println("Received annotation:  \"" +
                annotation + "\"");
            if (annotation == null) {
                throw new RuntimeException("annotation was null");
            }
            URL[] urls = pathToURLs(annotation);
            System.err.println(
                "URLs from annotation: " + Arrays.asList(urls));
            if (urls.length != 2) {
                throw new RuntimeException(
                    "wrong number of elements in annotation");
            }
            if (!urls[0].equals(codebase1)) {
                System.err.println("First URL in annotation is incorrect:");
                dumpURL(urls[0]);
                throw new RuntimeException(
                    "first URL in annotation is incorrect");
            }
            if (!urls[1].equals(codebase2)) {
                System.err.println("Second URL in annotation is incorrect:");
                dumpURL(urls[1]);
                throw new RuntimeException(
                    "second URL in annotation is incorrect");
            }
            System.err.println("TEST PASSED: annotation matched codebase");
        } catch (Exception e) {
            TestLibrary.bomb(e.getMessage(), e);
        }
    }
    private static URL[] pathToURLs(String path)
        throws MalformedURLException
    {
        StringTokenizer st = new StringTokenizer(path); 
        URL[] urls = new URL[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            urls[i] = new URL(st.nextToken());
        }
        return urls;
    }
    private static void dumpURL(URL u) {
        System.err.println("\tprotocol:  " + u.getProtocol());
        System.err.println("\tauthority: " + u.getAuthority());
        System.err.println("\tuser info: " + u.getUserInfo());
        System.err.println("\thost:      " + u.getHost());
        System.err.println("\tport:      " + u.getPort());
        System.err.println("\tpath:      " + u.getPath());
        System.err.println("\tfile:      " + u.getFile());
        System.err.println("\tquery:     " + u.getQuery());
        System.err.println("\tref:       " + u.getRef());
    }
}
