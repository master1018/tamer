public class FileOpenNeg {
    public static void main( String[] args) throws Exception {
        boolean openForWrite = true;
        File f = new File(args[0]);
        try {
            FileOutputStream fs = new FileOutputStream(f);
            fs.write(1);
            fs.close();
        } catch( IOException e ) {
            System.out.println("Caught the Exception as expected");
            e.printStackTrace(System.out);
            openForWrite = false;
        }
        if (openForWrite && !f.canWrite()) {
            throw new Exception("Able to open READ-ONLY file for WRITING!");
        }
    }
}
