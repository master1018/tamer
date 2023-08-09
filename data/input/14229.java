public class TestBug4523159 extends JarTest
{
    public void run(String[] args) throws Exception {
        if (args.length == 0 ) {  
            System.out.println("Test: " + getClass().getName());
            Process process = Runtime.getRuntime().exec(javaCmd + " TestBug4523159 -test");
            BufferedReader isReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader esReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Redirector outRedirector = new Redirector(isReader, System.out);
            Redirector errRedirector = new Redirector(esReader, System.err);
            (new Thread(outRedirector)).start();
            (new Thread(errRedirector)).start();
            process.waitFor();
            File testDir = new File(tmpdir + File.separator + getClass().getName());
            deleteRecursively(testDir);
            if (outRedirector.getHasReadData() || errRedirector.getHasReadData())
                throw new RuntimeException("Failed: No output should have been received from the process");
        } else { 
            File tmp = createTempDir();
            try {
                File dir = new File(tmp, "dir!name");
                dir.mkdir();
                File testFile = copyResource(dir, "jar1.jar");
                URL url = new URL("jar:" + testFile.toURL() + "!/res1.txt");
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                JarFile file = conn.getJarFile();
                JarEntry entry = conn.getJarEntry();
                byte[] buffer = readFully(file.getInputStream(entry));
                String str = new String(buffer);
                if (!str.equals("This is jar 1\n")) {
                    throw(new Exception("resource content invalid"));
                }
                URL[] urls = new URL[1];
                urls[0] = new URL("jar:" + testFile.toURL() + "!/");
                URLClassLoader loader = new URLClassLoader(urls);
                loader.loadClass("jar1.GetResource").newInstance();
            } finally {
                deleteRecursively(tmp);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        new TestBug4523159().run(args);
    }
}
