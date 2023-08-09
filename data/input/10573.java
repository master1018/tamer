public class BlockIsDirectory {
    public static void main( String args[] ) throws Exception {
        String osname = System.getProperty("os.name");
        if (osname.equals("SunOS")) {
            File dir = new File("/dev/dsk");
            String dirList[] = dir.list();
            File aFile = new File( "/dev/dsk/" + dirList[0] );
            boolean result = aFile.isDirectory();
            if (result == true)
                throw new RuntimeException(
                    "IsDirectory returns true for block device.");
        }
        if (osname.equals("Linux")) {
            File dir = new File("/dev/ide0");
            if (dir.exists()) {
                boolean result = dir.isDirectory();
                if (result == true)
                    throw new RuntimeException(
                        "IsDirectory returns true for block device.");
            }
            dir = new File("/dev/scd0");
            if (dir.exists()) {
                boolean result = dir.isDirectory();
                if (result == true)
                    throw new RuntimeException(
                        "IsDirectory returns true for block device.");
            }
        }
    }
}
