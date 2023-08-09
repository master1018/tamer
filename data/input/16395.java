public class FileChannelFDTest {
    static byte data[] = new byte[] {48, 49, 50, 51, 52, 53, 54, 55, 56, 57,};
    static String inFileName = "fd-in-test.txt";
    static String outFileName = "fd-out-test.txt";
    static File inFile;
    static File outFile;
    private static void writeToInFile() throws IOException {
        FileOutputStream out = new FileOutputStream(inFile);
        out.write(data);
        out.close();
    }
    public static void main(String[] args)
                throws Exception {
        inFile= new File(System.getProperty("test.dir", "."),
                        inFileName);
        inFile.createNewFile();
        inFile.deleteOnExit();
        writeToInFile();
        outFile  = new File(System.getProperty("test.dir", "."),
                        outFileName);
        outFile.createNewFile();
        outFile.deleteOnExit();
        doFileChannel();
    }
     private static void doFileChannel() throws Exception {
        FileInputStream fis = new FileInputStream(inFile);
        FileDescriptor fd = fis.getFD();
        FileChannel fc = fis.getChannel();
        System.out.println("Created fis:" + fis);
        fis = null;
        fc = null;
        System.gc();
        Thread.sleep(500);
        if (fd.valid()) {
            throw new Exception("Finalizer either didn't run --" +
                "try increasing the Thread's sleep time after System.gc();" +
                "or the finalizer didn't close the file");
        }
        System.out.println("File Closed successfully");
        System.out.println();
  }
}
