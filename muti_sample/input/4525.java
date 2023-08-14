public class Finalize {
    static byte data[] = new byte[] {48, 49, 50, 51, 52, 53, 54, 55, 56, 57,};
    static String inFileName = "fd-in-test.txt";
    static String outFileName = "fd-out-test.txt";
    static File inFile;
    static File outFile;
    public static void main(String[] args)
                throws Exception {
        Thread.sleep(5000);
        inFile= new File(System.getProperty("test.dir", "."),
                        inFileName);
        inFile.createNewFile();
        inFile.deleteOnExit();
        writeToInFile();
        doFileInputStream();
        outFile  = new File(System.getProperty("test.dir", "."),
                        outFileName);
        outFile.createNewFile();
        outFile.deleteOnExit();
        doFileOutputStream();
        doRandomAccessFile();
        doFileChannel();
    }
    private static void doFileInputStream() throws Exception {
        FileInputStream fis1 = new FileInputStream(inFile);
       FileDescriptor fd = fis1.getFD();
        FileInputStream fis2 = new FileInputStream(fd);
        fis1 = null;
        int ret = 0;
        System.gc();
        Thread.sleep(200);
        while((ret = fis2.read()) != -1 ) {
            System.out.println("read from fis2:" + ret);
        }
        fis2.close();
  }
    private static void writeToInFile() throws IOException {
        FileOutputStream out = new FileOutputStream(inFile);
        out.write(data);
        out.close();
    }
    private static void doFileOutputStream()
                throws Exception {
        System.out.println("--------FileOutputStream Test Started----------");
        FileOutputStream fos1 = new FileOutputStream(outFile);
        FileDescriptor fd = fos1.getFD();
        FileOutputStream fos2 = new FileOutputStream(fd);
        fos1 = null;
        System.gc();
        Thread.sleep(200);
        fos2.write(data);
        System.out.println("wrote:" + data.length + " bytes to fos2");
        fos2.close();
        System.out.println("--------FileOutputStream Test Over----------");
        System.out.println();
    }
    private static void doRandomAccessFile()
                throws Exception {
        System.out.println("--------RandomAccessFile Read Test Started----------");
        RandomAccessFile raf = new RandomAccessFile(inFile, "r");
        FileDescriptor fd = raf.getFD();
        FileInputStream fis = new FileInputStream(fd);
        fis = null;
        int ret = 0;
        System.gc();
        Thread.sleep(50);
        while((ret = raf.read()) != -1 ) {
            System.out.println("read from raf:" + ret);
        }
        raf.close();
        Thread.sleep(200);
        System.out.println("--------RandomAccessFile Write Test Started----------");
        System.out.println();
        raf = new RandomAccessFile(outFile, "rw");
        fd = raf.getFD();
        FileOutputStream fos = new FileOutputStream(fd);
        fos = null;
        System.gc();
        Thread.sleep(200);
        raf.write(data);
        System.out.println("wrote:" + data.length + " bytes to raf");
        raf.close();
        System.out.println("--------RandomAccessFile Write Test Over----------");
        System.out.println();
    }
     private static void doFileChannel() throws Exception {
        System.out.println("--------FileChannel Read Test Started----------");
        System.out.println();
        FileInputStream fis1 = new FileInputStream(inFile);
        FileDescriptor fd = fis1.getFD();
        FileInputStream fis2 = new FileInputStream(fd);
        FileChannel fc2 = fis2.getChannel();
        fis1 = null;
        System.gc();
        Thread.sleep(200);
        int ret = 1;
        ByteBuffer bb = ByteBuffer.allocateDirect(1);
        ret = fc2.read(bb);
        System.out.println("read " + ret + " bytes from fc2:");
        fc2.close();
        System.out.println("--------FileChannel Read Test Over----------");
        System.out.println();
        System.out.println("--------FileChannel Write Test Started----------");
        FileOutputStream fos1 = new FileOutputStream(outFile);
        fd = fos1.getFD();
        FileOutputStream fos2 = new FileOutputStream(fd);
        fc2 = fos2.getChannel();
        fos1 = null;
        System.gc();
        Thread.sleep(200);
        bb = ByteBuffer.allocateDirect(data.length);
        bb = bb.put(data);
        bb = (ByteBuffer) bb.flip();
        ret = fc2.write(bb);
        System.out.println("Wrote:" +  ret + " bytes to fc2");
        fc2.close();
        System.out.println("--------Channel Write Test Over----------");
    }
}
