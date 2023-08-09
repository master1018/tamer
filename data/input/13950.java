public class ReadLongZipFileName {
    private static String entryName = "testFile.txt";;
    public static void realMain(String args[]) {
        String longDirName = "abcdefghijklmnopqrstuvwx"; 
        String jarFileName = "areallylargejarfilename.jar";    
        File file = null;
        File myJarFile = null;
        int currentFileLength = 0;
        int minRequiredLength = 600; 
        Stack<File> directories = new Stack<File>();
        String filename = "." + File.separator;
        try {
            do {
                filename = filename + longDirName + File.separator;
                file = new File(filename);
                file.mkdir();
                currentFileLength = file.getCanonicalPath().length();
                directories.push(file);
            } while (currentFileLength < (minRequiredLength - jarFileName.length()));
            filename = filename + jarFileName;
            JarOutputStream out = new JarOutputStream(
                new BufferedOutputStream(
                    new FileOutputStream(filename.toString())));
            out.putNextEntry(new JarEntry(entryName));
            out.write(1);
            out.close();
            myJarFile = new File(filename.toString());
            currentFileLength = myJarFile.getCanonicalPath().length();
            if (!myJarFile.exists()) {
                fail("Jar file does not exist.");
            }
        } catch (IOException e) {
            unexpected(e, "Problem creating the Jar file.");
        }
        try {
            JarFile readJarFile = new JarFile(myJarFile);
            JarEntry je = readJarFile.getJarEntry(entryName);
            check(je != null);
            DataInputStream dis = new DataInputStream(
                readJarFile.getInputStream(je));
            byte val = dis.readByte();
            check(val == 1);
            try {
                dis.readByte();
                fail("Read past expected EOF");
            } catch (IOException e) {
                pass();
            }
            readJarFile.close();
            pass("Opened Jar file for reading with a name " + currentFileLength
                 + " characters long");
        } catch (IOException e) {
            unexpected(e, "Test failed - problem reading the Jar file back in.");
        }
        if (myJarFile != null) {
            check(myJarFile.delete());
        }
        while (! directories.empty()) {
            File f = directories.pop();
            check(f.delete());
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void pass(String msg) {System.out.println(msg); passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void unexpected(Throwable t, String msg) {
        System.out.println(msg); failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
