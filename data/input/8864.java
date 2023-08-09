public class DriveOnly {
    public static void main(String[] args) throws Exception {
        if (File.separatorChar != '\\') return;
        File f = new File("").getCanonicalFile();
        while (f.getParent() != null) f = f.getParentFile();
        String p = f.getPath().substring(0, 2);
        if (!(Character.isLetter(p.charAt(0)) && (p.charAt(1) == ':'))) {
            System.err.println("No current drive, cannot run test");
            return;
        }
        f = new File(p);
        if (!f.isDirectory())
            throw new Exception("\"" + f + "\" is not a directory");
        if (f.lastModified() == 0)
            throw new Exception("\"" + f + "\" has no last-modified time");
    }
}
