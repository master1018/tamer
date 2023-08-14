public class DeleteOnExit  {
    static String tmpdir = System.getProperty("java.io.tmpdir");
    static String java = System.getProperty("java.home") + File.separator +
                         "bin" + File.separator + "java";
    static File file1 = new File(tmpdir + "deletedOnExit1");
    static File file2 = new File(tmpdir + "deletedOnExit2");
    static File file3 = new File(tmpdir + "deletedOnExit3");
    static File dir = new File(tmpdir + "deletedOnExitDir");
    static File file4 = new File(dir + File.separator + "deletedOnExit4");
    static File file5 = new File(dir + File.separator + "dxnsdnguidfgejngognrogn");
    static File file6 = new File(dir + File.separator + "mmmmmmsdmfgmdsmfgmdsfgm");
    static File file7 = new File(dir + File.separator + "12345566777");
    public static void main (String args[]) throws Exception{
        if (args.length == 0) {
            String cmd = java + " -classpath " + System.getProperty("test.classes")
                + " DeleteOnExit -test";
            Runtime.getRuntime().exec(cmd).waitFor();
            if (file1.exists() || file2.exists() || file3.exists() ||
                dir.exists()   || file4.exists() || file5.exists() ||
                file6.exists() || file7.exists())  {
                System.out.println(file1 + ", exists = " + file1.exists());
                System.out.println(file2 + ", exists = " + file2.exists());
                System.out.println(file3 + ", exists = " + file3.exists());
                System.out.println(dir + ", exists = " + dir.exists());
                System.out.println(file4 + ", exists = " + file4.exists());
                System.out.println(file5 + ", exists = " + file5.exists());
                System.out.println(file6 + ", exists = " + file6.exists());
                System.out.println(file7 + ", exists = " + file7.exists());
                dir.delete();
                throw new Exception("File exists");
            }
        } else {
            file1.createNewFile();
            file2.createNewFile();
            file3.createNewFile();
            file1.deleteOnExit();
            file2.deleteOnExit();
            file3.deleteOnExit();
            file3.delete();
            file2.deleteOnExit();
            file2.deleteOnExit();
            file2.deleteOnExit();
            if (dir.mkdir()) {
                dir.deleteOnExit();
                file4.createNewFile();
                file5.createNewFile();
                file6.createNewFile();
                file7.createNewFile();
                file4.deleteOnExit();
                file5.deleteOnExit();
                file6.deleteOnExit();
                file7.deleteOnExit();
            }
        }
    }
}
