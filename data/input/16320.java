public class FileOpenPos {
    public static void main( String[] args)
        throws IOException {
        File f = new File(args[0]);
        FileOutputStream fs = new FileOutputStream(f);
        fs.write(1);
        fs.close();
        System.out.println("Can Write ?" + f.canWrite());
        System.out.println("The File was successfully opened");
    }
}
