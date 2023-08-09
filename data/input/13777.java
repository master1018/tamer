public class MaxPathLength {
    private static String sep = File.separator;
    private static String pathComponent = sep +
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
    private static String fileName =
                 "areallylongfilenamethatsforsur";
    private static boolean isWindows = false;
    private static long totalSpace;
    private static long freeSpace;
    private static long usableSpace;
    private static long ONEMEGA = 1024*1024;
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows")) {
            isWindows = true;
            if (osName.startsWith("Windows 9") ||
                osName.startsWith("Windows Me"))
            return; 
        }
        if (osName.startsWith("SunOS")) {
            return; 
        }
        if (isWindows) {
            File f = new File(".");
            totalSpace = f.getTotalSpace()/ONEMEGA;
            freeSpace = f.getFreeSpace()/ONEMEGA;
            usableSpace = f.getUsableSpace()/ONEMEGA;
        }
        for (int i = 4; i < 7; i++) {
            String name = fileName;
            while (name.length() < 256) {
                testLongPath (i, name, false);
                testLongPath (i, name, true);
                name += "A";
            }
        }
        if (args.length == 0 ||
            !"-extra".equals(args[0]) ||
            !isWindows)
            return;
        for (int i = 20; i < 21; i++) {
            String name = fileName;
            while (name.length() < 256) {
                testLongPath (i, name, false);
                testLongPath (i, name, true);
                name += "A";
            }
        }
    }
    private static int lastMax = 0;
    static void testLongPath(int max, String fn,
                             boolean tryAbsolute) throws Exception {
        String[] created = new String[max];
        String pathString = ".";
        for (int i = 0; i < max -1; i++) {
            pathString = pathString + pathComponent;
            created[max - 1 -i] = pathString;
        }
        File dirFile = new File(pathString);
        File f = new File(pathString + sep + fn);
        String tPath = f.getPath();
        if (tryAbsolute) {
            tPath = f.getCanonicalPath();
        }
        created[0] = tPath;
        File fu = new File(pathString + sep + fn.toUpperCase());
        if (dirFile.exists()) {
            System.err.println("Warning: Test directory structure exists already!");
            return;
        }
        boolean couldMakeTestDirectory = dirFile.mkdirs();
        if (!couldMakeTestDirectory) {
            throw new RuntimeException ("Could not create test directory structure");
        }
        try {
            if (tryAbsolute)
                dirFile = new File(dirFile.getCanonicalPath());
            if (!dirFile.isDirectory())
                throw new RuntimeException ("File.isDirectory() failed");
            if (isWindows && lastMax != max) {
                long diff = totalSpace - dirFile.getTotalSpace()/ONEMEGA;
                if (diff < -5 || diff > 5)
                    throw new RuntimeException ("File.getTotalSpace() failed");
                diff = freeSpace - dirFile.getFreeSpace()/ONEMEGA;
                if (diff < -5 || diff > 5)
                    throw new RuntimeException ("File.getFreeSpace() failed");
                diff = usableSpace - dirFile.getUsableSpace()/ONEMEGA;
                if (diff < -5 || diff > 5)
                    throw new RuntimeException ("File.getUsableSpace() failed");
                lastMax = max;
            }
            f = new File(tPath);
            if (!f.createNewFile()) {
                throw new RuntimeException ("File.createNewFile() failed");
            }
            if (!f.exists())
                throw new RuntimeException ("File.exists() failed");
            if (!f.isFile())
                throw new RuntimeException ("File.isFile() failed");
            if (!f.canRead())
                throw new RuntimeException ("File.canRead() failed");
            if (!f.canWrite())
                throw new RuntimeException ("File.canWrite() failed");
            if (!f.delete())
                throw new RuntimeException ("File.delete() failed");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(1);
            fos.close();
            if (f.length() != 1)
                throw new RuntimeException ("File.length() failed");
            long time = System.currentTimeMillis();
            if (!f.setLastModified(time))
                throw new RuntimeException ("File.setLastModified() failed");
            if (f.lastModified() == 0) {
                throw new RuntimeException ("File.lastModified() failed");
            }
            String[] list = dirFile.list();
            if (list == null || !fn.equals(list[0])) {
                throw new RuntimeException ("File.list() failed");
            }
            File[] flist = dirFile.listFiles();
            if (flist == null || !fn.equals(flist[0].getName()))
                throw new RuntimeException ("File.listFiles() failed");
            if (isWindows &&
                !fu.getCanonicalPath().equals(f.getCanonicalPath()))
                throw new RuntimeException ("getCanonicalPath() failed");
            char[] cc = tPath.toCharArray();
            cc[cc.length-1] = 'B';
            File nf = new File(new String(cc));
            if (!f.renameTo(nf)) {
                String abPath = f.getAbsolutePath();
                if (!abPath.startsWith("\\\\") ||
                    abPath.length() < 1093) {
                    throw new RuntimeException ("File.renameTo() failed for lenth="
                                                + abPath.length());
                }
                return;
            }
            if (!nf.canRead())
                throw new RuntimeException ("Renamed file is not readable");
            if (!nf.canWrite())
                throw new RuntimeException ("Renamed file is not writable");
            if (nf.length() != 1)
                throw new RuntimeException ("Renamed file's size is not correct");
            nf.renameTo(f);
        } finally {
            for (int i = 0; i < max; i++) {
                pathString = created[i];
                File df = new File(pathString);
                pathString = df.getCanonicalPath();
                df = new File(pathString);
                if (!df.delete())
                    System.out.printf("Delete failed->%s\n", pathString);
            }
        }
    }
}
