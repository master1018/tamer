public class OpenDir {
    public static void main(String args[]) throws Exception {
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(".");
            throw new
                Exception("FileInputStream.open should not work on dirs");
        } catch (IOException e) {
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(".");
            throw new
                Exception("FileOutputStream.open should'nt work on dirs");
        } catch (IOException e) {
        }
        RandomAccessFile ras = null;
        try {
            ras = new RandomAccessFile(".","r");
            throw new
                Exception("RandomAccessFile.open should'nt work on dirs");
        } catch (IOException e) {
        }
    }
}
