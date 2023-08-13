public class FileBuilder {
    private static void usageError() {
        System.err.println("Usage: FileBuilder filetype filename filesize");
        System.err.println("");
        System.err.println("Makes a file named FILENAME of size FILESIZE.");
        System.err.println("If FILETYPE is \"MostlyEmpty\", the file contents is mostly null bytes");
        System.err.println("(which might occupy no disk space if the right OS support exists).");
        System.err.println("If FILETYPE is \"SlightlyCompressible\", the file contents are");
        System.err.println("approximately 90% random data.");
        System.exit(1);
    }
    public static void main (String[] args) throws IOException {
        if (args.length != 3)
            usageError();
        String filetype = args[0];
        String filename = args[1];
        long   filesize = Long.parseLong(args[2]);
        if (! (filetype.equals("MostlyEmpty") ||
               filetype.equals("SlightlyCompressible")))
            usageError();
        try (RandomAccessFile raf = new RandomAccessFile(filename, "rw")) {
            if (filetype.equals("SlightlyCompressible")) {
                byte[] randomBytes = new byte[16384];
                byte[] nullBytes   = new byte[randomBytes.length/10];
                Random rand = new Random();
                for (int i = 0; raf.length() < filesize; ++i) {
                    rand.nextBytes(randomBytes);
                    raf.write(nullBytes);
                    raf.write(randomBytes);
                }
            }
            byte[] filenameBytes = filename.getBytes("UTF8");
            raf.seek(filesize-filenameBytes.length);
            raf.write(filenameBytes);
            raf.setLength(filesize);
        }
    }
}
