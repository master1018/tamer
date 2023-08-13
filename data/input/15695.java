public class EmptyPath {
    public static void main(String [] args) throws Exception {
        File f = new File("");
        f.mkdir();
        try {
            f.createNewFile();
            throw new RuntimeException("Expected exception not thrown");
        } catch (IOException ioe) {
        }
        try {
            FileInputStream fis = new FileInputStream(f);
            fis.close();
            throw new RuntimeException("Expected exception not thrown");
        } catch (FileNotFoundException fnfe) {
        }
    }
}
